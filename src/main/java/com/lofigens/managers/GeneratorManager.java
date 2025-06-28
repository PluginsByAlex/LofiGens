package com.lofigens.managers;

import com.lofigens.LofiGens;
import com.lofigens.models.Generator;
import com.lofigens.models.GeneratorType;
import com.lofigens.models.PlayerData;
import com.lofigens.utils.MessageUtil;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GeneratorManager {
    
    private final LofiGens plugin;
    private final Map<Location, Generator> activeGenerators;
    private final Map<String, ConfigurationSection> generatorConfigs;
    private BukkitTask generatorTask;
    
    public GeneratorManager(LofiGens plugin) {
        this.plugin = plugin;
        this.activeGenerators = new ConcurrentHashMap<>();
        this.generatorConfigs = new HashMap<>();
        loadGeneratorConfigs();
    }
    
    /**
     * Load generator configurations from config.yml
     */
    private void loadGeneratorConfigs() {
        generatorConfigs.clear();
        
        ConfigurationSection section = plugin.getConfigManager().getGeneratorsSection();
        if (section != null) {
            for (String generatorId : section.getKeys(false)) {
                ConfigurationSection generatorSection = section.getConfigurationSection(generatorId);
                if (generatorSection != null) {
                    generatorConfigs.put(generatorId, generatorSection);
                }
            }
        }
        
        plugin.getLogger().info("Loaded " + generatorConfigs.size() + " generator configurations");
    }
    
    /**
     * Start the generator task that handles all generation
     */
    public void startGeneratorTasks() {
        if (generatorTask != null) {
            generatorTask.cancel();
        }
        
        generatorTask = new BukkitRunnable() {
            @Override
            public void run() {
                processAllGenerators();
            }
        }.runTaskTimer(plugin, 20L, 20L); // Run every second
        
        plugin.getLogger().info("Generator task started");
    }
    
    /**
     * Process all active generators
     */
    private void processAllGenerators() {
        List<Location> toRemove = new ArrayList<>();
        
        for (Map.Entry<Location, Generator> entry : activeGenerators.entrySet()) {
            Location location = entry.getKey();
            Generator generator = entry.getValue();
            
            // Check if the block still exists
            if (location.getBlock().getType() == Material.AIR) {
                toRemove.add(location);
                continue;
            }
            
            // Check if generator is ready to generate
            if (generator.isReadyToGenerate()) {
                processGeneration(generator);
            }
        }
        
        // Remove generators that no longer exist
        for (Location location : toRemove) {
            removeGenerator(location);
        }
    }
    
    /**
     * Process generation for a specific generator
     */
    private void processGeneration(Generator generator) {
        Player owner = Bukkit.getPlayer(generator.getOwnerUUID());
        if (owner == null || !owner.isOnline()) {
            return; // Owner is offline
        }
        
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(owner);
        Location location = generator.getLocation();
        
        // Check for double items event
        boolean doubleItems = plugin.getEventManager().isDoubleItemsActive();
        int multiplier = doubleItems ? 2 : 1;
        
        switch (generator.getType()) {
            case ITEM:
            case UNSTABLE:
                generateItem(generator, owner, playerData, multiplier);
                break;
                
            case COMMAND:
                executeCommand(generator, owner);
                break;
                
            case EXP:
                generateExp(generator, owner, playerData, multiplier);
                break;
                
            case OVERCLOCKED:
                generateOverclockedItem(generator, owner, playerData, multiplier);
                break;
                
            case JACKPOT:
                contributeToJackpot(generator, owner, playerData);
                break;
        }
        
        // Mark generator as generated
        generator.markGenerated();
        
        // Play generation effects
        playGenerationEffects(location, generator);
        
        // Update hologram if enabled
        if (plugin.getConfigManager().isHologramsEnabled() && playerData.isHologramsEnabled()) {
            plugin.getHologramManager().updateHologram(generator);
        }
    }
    
    /**
     * Generate an item from a generator
     */
    private void generateItem(Generator generator, Player owner, PlayerData playerData, int multiplier) {
        Material itemType = generator.getItemType();
        if (itemType == null) return;
        
        ItemStack item = new ItemStack(itemType, multiplier);
        
        // Try to add to inventory, drop if full
        HashMap<Integer, ItemStack> leftover = owner.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            for (ItemStack drop : leftover.values()) {
                owner.getWorld().dropItem(generator.getLocation().add(0, 1, 0), drop);
            }
        }
        
        // Update player stats
        playerData.addItemsGenerated(multiplier);
        
        // Send message
        String itemName = itemType.name().toLowerCase().replace("_", " ");
        MessageUtil.sendMessage(owner, plugin.getConfigManager().getMessage("item_generated", "%item%", itemName));
    }
    
    /**
     * Execute a command from a command generator
     */
    private void executeCommand(Generator generator, Player owner) {
        String command = generator.getCommand();
        if (command == null || command.isEmpty()) return;
        
        // Replace placeholders
        command = command.replace("%player_name%", owner.getName());
        command = command.replace("%player_uuid%", owner.getUniqueId().toString());
        
        // Execute command as console
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
    
    /**
     * Generate experience from an exp generator
     */
    private void generateExp(Generator generator, Player owner, PlayerData playerData, int multiplier) {
        int expAmount = generator.getExpAmount() * multiplier;
        
        // Spawn experience orb at generator location
        ExperienceOrb orb = owner.getWorld().spawn(
            generator.getLocation().add(0.5, 1, 0.5), 
            ExperienceOrb.class
        );
        orb.setExperience(expAmount);
        
        // Update player stats
        playerData.addExpGenerated(expAmount);
    }
    
    /**
     * Generate item from overclocked generator
     */
    private void generateOverclockedItem(Generator generator, Player owner, PlayerData playerData, int multiplier) {
        Material itemType = generator.getNextOverclockedItem();
        if (itemType == null) return;
        
        ItemStack item = new ItemStack(itemType, multiplier);
        
        // Try to add to inventory, drop if full
        HashMap<Integer, ItemStack> leftover = owner.getInventory().addItem(item);
        if (!leftover.isEmpty()) {
            for (ItemStack drop : leftover.values()) {
                owner.getWorld().dropItem(generator.getLocation().add(0, 1, 0), drop);
            }
        }
        
        // Update player stats
        playerData.addItemsGenerated(multiplier);
        
        // Check if generator should break
        if (generator.getCurrentGenerations() >= generator.getMaxGenerations()) {
            MessageUtil.sendMessage(owner, "&cYour overclocked generator has broken after reaching its limit!");
            
            // Announce if enabled
            if (plugin.getConfigManager().getConfig().getBoolean("announcements.enabled", true)) {
                String announcement = plugin.getConfigManager().getConfig().getString("announcements.overclocked_finish");
                if (announcement != null) {
                    MessageUtil.broadcast(announcement.replace("%player_name%", owner.getName()));
                }
            }
        }
        
        // Send message
        String itemName = itemType.name().toLowerCase().replace("_", " ");
        MessageUtil.sendMessage(owner, plugin.getConfigManager().getMessage("item_generated", "%item%", itemName));
    }
    
    /**
     * Contribute to jackpot from jackpot generator
     */
    private void contributeToJackpot(Generator generator, Player owner, PlayerData playerData) {
        int min = generator.getMinJackpotAmount();
        int max = generator.getMaxJackpotAmount();
        int amount = min + (int) (Math.random() * (max - min + 1));
        
        plugin.getJackpotManager().addToJackpot(amount);
        playerData.addJackpotContribution();
        
        MessageUtil.sendMessage(owner, plugin.getConfigManager().getMessage("jackpot_contributed", "%amount%", String.valueOf(amount)));
    }
    
    /**
     * Play generation effects
     */
    private void playGenerationEffects(Location location, Generator generator) {
        World world = location.getWorld();
        if (world == null) return;
        
        // Play sound
        if (plugin.getConfigManager().isSoundsEnabled()) {
            try {
                Sound sound = Sound.valueOf(plugin.getConfigManager().getConfig().getString("sounds.item_generate", "ENTITY_EXPERIENCE_ORB_PICKUP"));
                world.playSound(location, sound, 0.5f, 1.0f);
            } catch (IllegalArgumentException e) {
                // Invalid sound name, ignore
            }
        }
        
        // Spawn particles
        if (plugin.getConfigManager().isParticlesEnabled()) {
            try {
                Particle particle = Particle.valueOf(plugin.getConfigManager().getConfig().getString("particles.generator_working", "HEART"));
                world.spawnParticle(particle, location.add(0.5, 1, 0.5), 3, 0.2, 0.2, 0.2, 0);
            } catch (IllegalArgumentException e) {
                // Invalid particle name, ignore
            }
        }
    }
    
    /**
     * Place a generator at a location
     */
    public boolean placeGenerator(Player player, String generatorId, Location location) {
        // Check if generator configuration exists
        ConfigurationSection config = generatorConfigs.get(generatorId);
        if (config == null) {
            MessageUtil.sendMessage(player, plugin.getConfigManager().getMessage("invalid_generator"));
            return false;
        }
        
        // Check if player can place more generators
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        if (!playerData.canPlaceGenerator()) {
            MessageUtil.sendMessage(player, plugin.getConfigManager().getMessage("max_generators_reached"));
            return false;
        }
        
        // Check PlotSquared permissions if enabled
        if (plugin.isPlotSquaredEnabled()) {
            if (!checkPlotSquaredPermission(player, location)) {
                MessageUtil.sendMessage(player, "&cYou can only place generators in your own plot!");
                return false;
            }
        }
        
        // Create and place generator
        Generator generator = new Generator(generatorId, player.getUniqueId(), location, config);
        
        // Set the block type
        location.getBlock().setType(generator.getBlockType());
        
        // Add to active generators
        activeGenerators.put(location, generator);
        
        // Update player data
        playerData.addGenerator(generatorId);
        
        // Send placement message
        MessageUtil.sendMessage(player, plugin.getConfigManager().getMessage("generator_placed",
            "%num_generators%", String.valueOf(playerData.getPlacedGenerators()),
            "%max_generators%", String.valueOf(playerData.getGeneratorSlots())));
        
        // Create hologram if enabled
        if (plugin.getConfigManager().isHologramsEnabled() && playerData.isHologramsEnabled()) {
            plugin.getHologramManager().createHologram(generator);
        }
        
        // Play placement effects
        playPlacementEffects(location);
        
        // Announce overclocked start if applicable
        if (generator.getType() == GeneratorType.OVERCLOCKED) {
            if (plugin.getConfigManager().getConfig().getBoolean("announcements.enabled", true)) {
                String announcement = plugin.getConfigManager().getConfig().getString("announcements.overclocked_start");
                if (announcement != null) {
                    MessageUtil.broadcast(announcement.replace("%player_name%", player.getName()));
                }
            }
        }
        
        return true;
    }
    
    /**
     * Remove a generator at a location
     */
    public boolean removeGenerator(Location location) {
        Generator generator = activeGenerators.remove(location);
        if (generator == null) return false;
        
        // Update player data
        Player owner = Bukkit.getPlayer(generator.getOwnerUUID());
        if (owner != null) {
            PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(owner);
            playerData.removeGenerator(generator.getId());
        }
        
        // Remove hologram
        plugin.getHologramManager().removeHologram(location);
        
        return true;
    }
    
    /**
     * Get generator at location
     */
    public Generator getGenerator(Location location) {
        return activeGenerators.get(location);
    }
    
    /**
     * Get all generators owned by a player
     */
    public List<Generator> getPlayerGenerators(UUID playerUUID) {
        return activeGenerators.values().stream()
            .filter(gen -> gen.getOwnerUUID().equals(playerUUID))
            .toList();
    }
    
    /**
     * Create generator item
     */
    public ItemStack createGeneratorItem(String generatorId, int amount) {
        ConfigurationSection config = generatorConfigs.get(generatorId);
        if (config == null) return null;
        
        String blockTypeName = config.getString("block_type", "STONE");
        Material blockType;
        try {
            blockType = Material.valueOf(blockTypeName);
        } catch (IllegalArgumentException e) {
            blockType = Material.STONE;
        }
        
        ItemStack item = new ItemStack(blockType, amount);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(MessageUtil.colorize(config.getString("name", "Generator")));
            
            List<String> lore = new ArrayList<>();
            lore.add(MessageUtil.colorize("&7Type: &f" + config.getString("type", "ITEM")));
            lore.add(MessageUtil.colorize("&7Interval: &f" + config.getInt("spawn_interval", 5) + "s"));
            lore.add("");
            lore.add(MessageUtil.colorize("&ePlace this block to create a generator!"));
            
            meta.setLore(lore);
            
            // Add custom model data or NBT to identify as generator
            meta.setCustomModelData(generatorId.hashCode());
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Check PlotSquared permission
     */
    private boolean checkPlotSquaredPermission(Player player, Location location) {
        // TODO: Implement PlotSquared integration
        // For now, return true to allow placement anywhere
        return true;
    }
    
    /**
     * Play placement effects
     */
    private void playPlacementEffects(Location location) {
        World world = location.getWorld();
        if (world == null) return;
        
        if (plugin.getConfigManager().isSoundsEnabled()) {
            try {
                Sound sound = Sound.valueOf(plugin.getConfigManager().getConfig().getString("sounds.generator_place", "BLOCK_STONE_PLACE"));
                world.playSound(location, sound, 0.7f, 1.0f);
            } catch (IllegalArgumentException e) {
                // Invalid sound name, ignore
            }
        }
    }
    
    /**
     * Reload the generator manager
     */
    public void reload() {
        loadGeneratorConfigs();
        
        if (generatorTask != null) {
            generatorTask.cancel();
        }
        startGeneratorTasks();
    }
    
    /**
     * Get all generator IDs
     */
    public Set<String> getGeneratorIds() {
        return generatorConfigs.keySet();
    }
    
    /**
     * Check if generator ID exists
     */
    public boolean isValidGeneratorId(String generatorId) {
        return generatorConfigs.containsKey(generatorId);
    }
    
    /**
     * Get total number of active generators
     */
    public int getTotalActiveGenerators() {
        return activeGenerators.size();
    }
    
    /**
     * Sync generators (recalculate generation times)
     */
    public void syncGenerators(UUID playerUUID) {
        long currentTime = System.currentTimeMillis();
        
        for (Generator generator : getPlayerGenerators(playerUUID)) {
            // Reset last generation time to sync all generators
            generator.markGenerated();
        }
    }
} 