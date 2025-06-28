package com.lofigens.models;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.UUID;

public class Generator {
    
    private String id;                      // Generator configuration ID
    private UUID ownerUUID;                 // Player who owns this generator
    private Location location;              // Where the generator is placed
    private GeneratorType type;             // Type of generator
    private String name;                    // Display name
    private Material blockType;             // Block material when working
    private int spawnInterval;              // Seconds between generations
    private long lastGeneration;            // Last time this generator produced something
    private boolean isWorking;              // Whether the generator is currently working
    
    // Type-specific properties
    private Material itemType;              // For ITEM generators
    private String command;                 // For COMMAND generators
    private int expAmount;                  // For EXP generators
    private double breakChance;             // For UNSTABLE generators
    private int repairCost;                 // For UNSTABLE generators
    private Material brokenBlockType;       // For UNSTABLE generators when broken
    private List<Material> overlockedItems; // For OVERCLOCKED generators
    private int maxGenerations;             // For OVERCLOCKED generators
    private int currentGenerations;         // For OVERCLOCKED generators current count
    private int minJackpotAmount;           // For JACKPOT generators
    private int maxJackpotAmount;           // For JACKPOT generators
    
    public Generator(String id, UUID ownerUUID, Location location, ConfigurationSection config) {
        this.id = id;
        this.ownerUUID = ownerUUID;
        this.location = location;
        this.isWorking = true;
        this.lastGeneration = System.currentTimeMillis();
        this.currentGenerations = 0;
        
        loadFromConfig(config);
    }
    
    private void loadFromConfig(ConfigurationSection config) {
        this.name = config.getString("name", "Unknown Generator");
        this.type = GeneratorType.valueOf(config.getString("type", "ITEM"));
        this.spawnInterval = config.getInt("spawn_interval", 5);
        
        String blockTypeName = config.getString("block_type", "STONE");
        try {
            this.blockType = Material.valueOf(blockTypeName);
        } catch (IllegalArgumentException e) {
            this.blockType = Material.STONE;
        }
        
        // Load type-specific properties
        switch (type) {
            case ITEM:
            case UNSTABLE:
                String itemName = config.getString("item", "STONE");
                try {
                    this.itemType = Material.valueOf(itemName);
                } catch (IllegalArgumentException e) {
                    this.itemType = Material.STONE;
                }
                
                if (type == GeneratorType.UNSTABLE) {
                    this.breakChance = config.getDouble("break_chance", 0.1);
                    this.repairCost = config.getInt("repair_cost", 5);
                    String brokenBlockName = config.getString("block_type_broken", "COBBLESTONE");
                    try {
                        this.brokenBlockType = Material.valueOf(brokenBlockName);
                    } catch (IllegalArgumentException e) {
                        this.brokenBlockType = Material.COBBLESTONE;
                    }
                }
                break;
                
            case COMMAND:
                this.command = config.getString("command", "say Hello!");
                break;
                
            case EXP:
                this.expAmount = config.getInt("amount_of_EXP", 10);
                break;
                
            case OVERCLOCKED:
                this.maxGenerations = config.getInt("amount", 200);
                List<String> itemNames = config.getStringList("items");
                this.overlockedItems = itemNames.stream()
                    .map(name -> {
                        try {
                            return Material.valueOf(name);
                        } catch (IllegalArgumentException e) {
                            return Material.STONE;
                        }
                    })
                    .toList();
                break;
                
            case JACKPOT:
                this.minJackpotAmount = config.getInt("min_generate_amount", 100);
                this.maxJackpotAmount = config.getInt("max_generate_amount", 1000);
                break;
        }
    }
    
    /**
     * Check if this generator is ready to generate something
     */
    public boolean isReadyToGenerate() {
        if (!isWorking) return false;
        
        long currentTime = System.currentTimeMillis();
        long timeSinceLastGen = (currentTime - lastGeneration) / 1000;
        
        return timeSinceLastGen >= spawnInterval;
    }
    
    /**
     * Mark that this generator just generated something
     */
    public void markGenerated() {
        this.lastGeneration = System.currentTimeMillis();
        
        // For overclocked generators, increment the counter
        if (type == GeneratorType.OVERCLOCKED) {
            currentGenerations++;
            
            // Check if it should break
            if (currentGenerations >= maxGenerations) {
                setWorking(false);
            }
        }
        
        // For unstable generators, check if it should break
        if (type == GeneratorType.UNSTABLE && isWorking) {
            if (Math.random() < breakChance) {
                setWorking(false);
            }
        }
    }
    
    /**
     * Get the next item for overclocked generators
     */
    public Material getNextOverclockedItem() {
        if (type != GeneratorType.OVERCLOCKED || overlockedItems.isEmpty()) {
            return Material.STONE;
        }
        
        int index = currentGenerations % overlockedItems.size();
        return overlockedItems.get(index);
    }
    
    /**
     * Repair this generator (for unstable generators)
     */
    public void repair() {
        if (type == GeneratorType.UNSTABLE) {
            setWorking(true);
        }
    }
    
    /**
     * Reset overclocked generator
     */
    public void resetOverclocked() {
        if (type == GeneratorType.OVERCLOCKED) {
            currentGenerations = 0;
            setWorking(true);
        }
    }
    
    // Getters and setters
    public String getId() { return id; }
    public UUID getOwnerUUID() { return ownerUUID; }
    public Location getLocation() { return location; }
    public GeneratorType getType() { return type; }
    public String getName() { return name; }
    public Material getBlockType() { return blockType; }
    public int getSpawnInterval() { return spawnInterval; }
    public long getLastGeneration() { return lastGeneration; }
    public boolean isWorking() { return isWorking; }
    
    public Material getItemType() { return itemType; }
    public String getCommand() { return command; }
    public int getExpAmount() { return expAmount; }
    public double getBreakChance() { return breakChance; }
    public int getRepairCost() { return repairCost; }
    public Material getBrokenBlockType() { return brokenBlockType; }
    public List<Material> getOverlockedItems() { return overlockedItems; }
    public int getMaxGenerations() { return maxGenerations; }
    public int getCurrentGenerations() { return currentGenerations; }
    public int getMinJackpotAmount() { return minJackpotAmount; }
    public int getMaxJackpotAmount() { return maxJackpotAmount; }
    
    public void setWorking(boolean working) {
        this.isWorking = working;
        
        // Update the block type based on working status
        if (type == GeneratorType.UNSTABLE) {
            Material targetBlock = working ? blockType : brokenBlockType;
            if (location.getBlock().getType() != targetBlock) {
                location.getBlock().setType(targetBlock);
            }
        }
    }
    
    public void setCurrentGenerations(int currentGenerations) {
        this.currentGenerations = currentGenerations;
    }
    
    /**
     * Get time until next generation in seconds
     */
    public int getTimeUntilNextGeneration() {
        if (!isWorking) return -1;
        
        long currentTime = System.currentTimeMillis();
        long timeSinceLastGen = (currentTime - lastGeneration) / 1000;
        
        return Math.max(0, spawnInterval - (int) timeSinceLastGen);
    }
    
    /**
     * Get progress percentage (0.0 to 1.0)
     */
    public double getProgress() {
        if (!isWorking) return 0.0;
        
        long currentTime = System.currentTimeMillis();
        long timeSinceLastGen = (currentTime - lastGeneration) / 1000;
        
        return Math.min(1.0, (double) timeSinceLastGen / spawnInterval);
    }
} 