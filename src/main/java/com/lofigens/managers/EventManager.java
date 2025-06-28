package com.lofigens.managers;

import com.lofigens.LofiGens;
import com.lofigens.models.PlayerData;
import com.lofigens.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EventManager {
    
    private final LofiGens plugin;
    private boolean globalDoubleItemsActive = false;
    private long globalDoubleItemsEndTime = 0;
    private final Map<UUID, Long> playerDoubleItemsEndTime = new HashMap<>();
    private BukkitTask cleanupTask;
    
    public EventManager(LofiGens plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Start the cleanup task for expired events
     */
    public void startCleanupTask() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
        }
        
        cleanupTask = new BukkitRunnable() {
            @Override
            public void run() {
                cleanupExpiredEvents();
            }
        }.runTaskTimer(plugin, 20L, 20L); // Run every second
    }
    
    /**
     * Clean up expired events
     */
    private void cleanupExpiredEvents() {
        long currentTime = System.currentTimeMillis();
        
        // Check global double items
        if (globalDoubleItemsActive && currentTime >= globalDoubleItemsEndTime) {
            globalDoubleItemsActive = false;
            MessageUtil.broadcast("&eGlobal Double Items event has ended!");
        }
        
        // Check player-specific double items
        playerDoubleItemsEndTime.entrySet().removeIf(entry -> {
            if (currentTime >= entry.getValue()) {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null && player.isOnline()) {
                    MessageUtil.sendMessage(player, "&eYour Double Items boost has ended!");
                }
                return true;
            }
            return false;
        });
    }
    
    /**
     * Activate global double items event
     */
    public void activateGlobalDoubleItems(int durationSeconds) {
        globalDoubleItemsActive = true;
        globalDoubleItemsEndTime = System.currentTimeMillis() + (durationSeconds * 1000L);
        
        String timeString = MessageUtil.formatTime(durationSeconds);
        MessageUtil.broadcast("&eGlobal Double Items event activated for " + timeString + "!");
        
        // Announce
        if (plugin.getConfigManager().getConfig().getBoolean("announcements.enabled", true)) {
            String announcement = plugin.getConfigManager().getConfig().getString("announcements.double_item_token_used");
            if (announcement != null) {
                MessageUtil.broadcast(announcement
                    .replace("%player_name%", "Server")
                    .replace("%duration%", timeString));
            }
        }
    }
    
    /**
     * Activate player-specific double items event
     */
    public void activatePlayerDoubleItems(Player player, int durationSeconds) {
        // Don't allow during global event
        if (globalDoubleItemsActive) {
            MessageUtil.sendMessage(player, "&cYou cannot use Double Items during a global event!");
            return;
        }
        
        UUID playerUUID = player.getUniqueId();
        long endTime = System.currentTimeMillis() + (durationSeconds * 1000L);
        playerDoubleItemsEndTime.put(playerUUID, endTime);
        
        String timeString = MessageUtil.formatTime(durationSeconds);
        MessageUtil.sendMessage(player, "&eYour Double Items boost is now active for " + timeString + "!");
        
        // Announce
        if (plugin.getConfigManager().getConfig().getBoolean("announcements.enabled", true)) {
            String announcement = plugin.getConfigManager().getConfig().getString("announcements.double_item_token_used");
            if (announcement != null) {
                MessageUtil.broadcast(announcement
                    .replace("%player_name%", player.getName())
                    .replace("%duration%", timeString));
            }
        }
    }
    
    /**
     * Check if double items is active (global or player-specific)
     */
    public boolean isDoubleItemsActive() {
        return globalDoubleItemsActive;
    }
    
    /**
     * Check if double items is active for a specific player
     */
    public boolean isDoubleItemsActive(UUID playerUUID) {
        if (globalDoubleItemsActive) {
            return true;
        }
        
        Long endTime = playerDoubleItemsEndTime.get(playerUUID);
        return endTime != null && System.currentTimeMillis() < endTime;
    }
    
    /**
     * Use time warp item
     */
    public void useTimeWarp(Player player, int durationSeconds) {
        // Check if global double items is active
        if (globalDoubleItemsActive) {
            MessageUtil.sendMessage(player, plugin.getConfigManager().getMessage("warp_not_allowed"));
            return;
        }
        
        // Open time warp GUI
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            openTimeWarpGUI(player, durationSeconds);
        });
    }
    
    /**
     * Open time warp GUI
     */
    private void openTimeWarpGUI(Player player, int warpSeconds) {
        // This will be implemented with the GUI system
        // For now, just process the time warp directly
        processTimeWarp(player, warpSeconds);
    }
    
    /**
     * Process time warp for all player's generators
     */
    private void processTimeWarp(Player player, int warpSeconds) {
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        
        // Get all player's generators
        var generators = plugin.getGeneratorManager().getPlayerGenerators(player.getUniqueId());
        
        if (generators.isEmpty()) {
            MessageUtil.sendMessage(player, "&cYou don't have any generators to warp!");
            return;
        }
        
        int totalItemsGenerated = 0;
        
        // Process each generator
        for (var generator : generators) {
            if (!generator.isWorking()) continue;
            
            int generationsInTime = warpSeconds / generator.getSpawnInterval();
            if (generationsInTime <= 0) continue;
            
            switch (generator.getType()) {
                case ITEM:
                case UNSTABLE:
                    if (generator.getItemType() != null) {
                        int totalAmount = generationsInTime;
                        
                        // Add items to inventory
                        while (totalAmount > 0) {
                            int stackSize = Math.min(totalAmount, generator.getItemType().getMaxStackSize());
                            var item = new org.bukkit.inventory.ItemStack(generator.getItemType(), stackSize);
                            
                            var leftover = player.getInventory().addItem(item);
                            if (!leftover.isEmpty()) {
                                // Drop excess items
                                for (var drop : leftover.values()) {
                                    player.getWorld().dropItem(player.getLocation(), drop);
                                }
                            }
                            
                            totalAmount -= stackSize;
                        }
                        
                        totalItemsGenerated += generationsInTime;
                        playerData.addItemsGenerated(generationsInTime);
                    }
                    break;
                    
                case EXP:
                    int totalExp = generationsInTime * generator.getExpAmount();
                    player.giveExp(totalExp);
                    playerData.addExpGenerated(totalExp);
                    break;
                    
                case OVERCLOCKED:
                    // Process overclocked generation
                    for (int i = 0; i < generationsInTime && generator.isWorking(); i++) {
                        var itemType = generator.getNextOverclockedItem();
                        if (itemType != null) {
                            var item = new org.bukkit.inventory.ItemStack(itemType, 1);
                            var leftover = player.getInventory().addItem(item);
                            if (!leftover.isEmpty()) {
                                player.getWorld().dropItem(player.getLocation(), leftover.values().iterator().next());
                            }
                            
                            generator.markGenerated(); // This handles the counter
                            totalItemsGenerated++;
                            playerData.addItemsGenerated(1);
                        }
                    }
                    break;
                    
                case JACKPOT:
                    // Add to jackpot multiple times
                    for (int i = 0; i < generationsInTime; i++) {
                        int min = generator.getMinJackpotAmount();
                        int max = generator.getMaxJackpotAmount();
                        int amount = min + (int) (Math.random() * (max - min + 1));
                        
                        plugin.getJackpotManager().addToJackpot(amount);
                        playerData.addJackpotContribution();
                    }
                    break;
            }
        }
        
        MessageUtil.sendMessage(player, "&aTime warp complete! Generated " + totalItemsGenerated + " items from " + warpSeconds + " seconds of time!");
    }
    
    /**
     * Force an event (admin command)
     */
    public void forceEvent(String eventType, int duration, boolean global, Player target) {
        switch (eventType.toLowerCase()) {
            case "double_items":
            case "doubleitems":
                if (global) {
                    activateGlobalDoubleItems(duration);
                } else if (target != null) {
                    activatePlayerDoubleItems(target, duration);
                }
                break;
                
            default:
                // Invalid event type
                break;
        }
    }
    
    /**
     * Get remaining time for double items (in seconds)
     */
    public int getDoubleItemsRemainingTime(UUID playerUUID) {
        if (globalDoubleItemsActive) {
            return (int) Math.max(0, (globalDoubleItemsEndTime - System.currentTimeMillis()) / 1000);
        }
        
        Long endTime = playerDoubleItemsEndTime.get(playerUUID);
        if (endTime != null) {
            return (int) Math.max(0, (endTime - System.currentTimeMillis()) / 1000);
        }
        
        return 0;
    }
    
    /**
     * Check if global double items is active
     */
    public boolean isGlobalDoubleItemsActive() {
        return globalDoubleItemsActive;
    }
    
    /**
     * Reload the event manager
     */
    public void reload() {
        // Nothing specific to reload for now
    }
} 