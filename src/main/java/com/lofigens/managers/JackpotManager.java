package com.lofigens.managers;

import com.lofigens.LofiGens;
import com.lofigens.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class JackpotManager {
    
    private final LofiGens plugin;
    private long currentJackpotAmount;
    private String lastWinner;
    private long lastWinAmount;
    private BukkitTask jackpotTask;
    private final Random random = new Random();
    
    public JackpotManager(LofiGens plugin) {
        this.plugin = plugin;
        loadJackpotData();
    }
    
    /**
     * Load jackpot data from configuration
     */
    private void loadJackpotData() {
        FileConfiguration config = plugin.getConfigManager().getJackpotConfig();
        
        this.currentJackpotAmount = config.getLong("current_amount", 0);
        this.lastWinner = config.getString("last_winner", "");
        this.lastWinAmount = config.getLong("last_amount", 0);
    }
    
    /**
     * Save jackpot data to configuration
     */
    public void saveData() {
        FileConfiguration config = plugin.getConfigManager().getJackpotConfig();
        
        config.set("current_amount", currentJackpotAmount);
        config.set("last_winner", lastWinner);
        config.set("last_amount", lastWinAmount);
        
        plugin.getConfigManager().saveJackpotConfig();
    }
    
    /**
     * Start the jackpot draw task
     */
    public void startJackpotTask() {
        if (!plugin.getConfigManager().getConfig().getBoolean("jackpot.enabled", true)) {
            return;
        }
        
        if (jackpotTask != null) {
            jackpotTask.cancel();
        }
        
        int drawTime = plugin.getConfigManager().getConfig().getInt("jackpot.draw_time", 3600);
        
        jackpotTask = new BukkitRunnable() {
            @Override
            public void run() {
                drawJackpot();
            }
        }.runTaskTimer(plugin, drawTime * 20L, drawTime * 20L);
        
        plugin.getLogger().info("Jackpot task started with draw time: " + drawTime + " seconds");
    }
    
    /**
     * Add amount to the jackpot
     */
    public void addToJackpot(long amount) {
        currentJackpotAmount += amount;
    }
    
    /**
     * Draw the jackpot
     */
    public void drawJackpot() {
        if (currentJackpotAmount <= 0) {
            plugin.getLogger().info("Jackpot draw skipped - no money in jackpot");
            return;
        }
        
        // Get eligible players (those who have contributed)
        List<Player> eligiblePlayers = getEligiblePlayers();
        
        if (eligiblePlayers.isEmpty()) {
            plugin.getLogger().info("Jackpot draw skipped - no eligible players");
            return;
        }
        
        // Pick random winner
        Player winner = eligiblePlayers.get(random.nextInt(eligiblePlayers.size()));
        
        // Award jackpot
        awardJackpot(winner);
    }
    
    /**
     * Force draw the jackpot
     */
    public void forceDrawJackpot() {
        drawJackpot();
    }
    
    /**
     * Get eligible players for jackpot
     */
    private List<Player> getEligiblePlayers() {
        List<Player> eligible = new ArrayList<>();
        List<String> revokedPlayers = plugin.getConfigManager().getConfig().getStringList("jackpot.revoked_players");
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Check if player is revoked
            if (revokedPlayers.contains(player.getName()) || revokedPlayers.contains(player.getUniqueId().toString())) {
                continue;
            }
            
            // Check if player has made contributions
            var playerData = plugin.getPlayerDataManager().getPlayerData(player);
            if (playerData.getJackpotContributions() > 0) {
                eligible.add(player);
            }
        }
        
        return eligible;
    }
    
    /**
     * Award jackpot to winner
     */
    private void awardJackpot(Player winner) {
        long winAmount = currentJackpotAmount;
        
        // Update last winner data
        lastWinner = winner.getName();
        lastWinAmount = winAmount;
        
        // Reset jackpot
        currentJackpotAmount = 0;
        
        // Save data
        saveData();
        
        // Send messages
        String formattedAmount = MessageUtil.formatNumber(winAmount);
        
        MessageUtil.sendMessage(winner, plugin.getConfigManager().getMessage("jackpot_winner")
            .replace("%jackpot_amount%", formattedAmount));
        
        MessageUtil.broadcast(plugin.getConfigManager().getConfig().getString("jackpot.messages.jackpot_announcement", "&eJackpot Winner: %winner%. They have won %jackpot_amount%!")
            .replace("%winner%", winner.getName())
            .replace("%jackpot_amount%", formattedAmount));
        
        // Play effects
        playJackpotEffects(winner);
        
        // Execute economy command (if you have an economy plugin)
        executeJackpotReward(winner, winAmount);
    }
    
    /**
     * Play jackpot win effects
     */
    private void playJackpotEffects(Player winner) {
        if (plugin.getConfigManager().getConfig().getBoolean("jackpot.sounds.enabled", true)) {
            try {
                String soundName = plugin.getConfigManager().getConfig().getString("jackpot.sounds.sound_on_win", "ENTITY_PLAYER_LEVELUP");
                var sound = org.bukkit.Sound.valueOf(soundName);
                winner.playSound(winner.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                // Invalid sound name
            }
        }
        
        if (plugin.getConfigManager().getConfig().getBoolean("jackpot.particles.enabled", true)) {
            try {
                String particleName = plugin.getConfigManager().getConfig().getString("jackpot.particles.particle_on_win", "HEART");
                var particle = org.bukkit.Particle.valueOf(particleName);
                int amount = plugin.getConfigManager().getConfig().getInt("jackpot.particles.amount", 10);
                
                winner.getWorld().spawnParticle(particle, winner.getLocation().add(0, 1, 0), amount, 1, 1, 1, 0);
            } catch (IllegalArgumentException e) {
                // Invalid particle name
            }
        }
    }
    
    /**
     * Execute jackpot reward (economy integration)
     */
    private void executeJackpotReward(Player winner, long amount) {
        // For now, just give them items as a reward or execute a command
        // This would typically integrate with an economy plugin
        
        // Example: Give diamond items as reward
        int diamonds = (int) Math.min(amount / 100, 64); // 1 diamond per 100 jackpot points, max 64
        if (diamonds > 0) {
            var diamondStack = new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND, diamonds);
            var leftover = winner.getInventory().addItem(diamondStack);
            if (!leftover.isEmpty()) {
                winner.getWorld().dropItem(winner.getLocation(), leftover.values().iterator().next());
            }
        }
        
        // Or execute a custom command
        String command = "give " + winner.getName() + " diamond " + diamonds;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
    
    /**
     * Revoke player from jackpot eligibility
     */
    public void revokePlayer(String playerName) {
        List<String> revokedPlayers = plugin.getConfigManager().getConfig().getStringList("jackpot.revoked_players");
        if (!revokedPlayers.contains(playerName)) {
            revokedPlayers.add(playerName);
            plugin.getConfigManager().getConfig().set("jackpot.revoked_players", revokedPlayers);
            plugin.getConfigManager().saveMainConfig();
        }
    }
    
    /**
     * Remove player from revoked list
     */
    public void unrevokePlayer(String playerName) {
        List<String> revokedPlayers = plugin.getConfigManager().getConfig().getStringList("jackpot.revoked_players");
        if (revokedPlayers.remove(playerName)) {
            plugin.getConfigManager().getConfig().set("jackpot.revoked_players", revokedPlayers);
            plugin.getConfigManager().saveMainConfig();
        }
    }
    
    // Getters
    public long getCurrentJackpotAmount() { return currentJackpotAmount; }
    public String getLastWinner() { return lastWinner; }
    public long getLastWinAmount() { return lastWinAmount; }
    
    /**
     * Reload the jackpot manager
     */
    public void reload() {
        loadJackpotData();
        
        if (jackpotTask != null) {
            jackpotTask.cancel();
        }
        startJackpotTask();
    }
} 