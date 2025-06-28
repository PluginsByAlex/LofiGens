package com.lofigens.models;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
    
    private UUID playerUUID;
    private String playerName;
    private int generatorSlots;
    private int maxGeneratorSlots;
    private boolean hologramsEnabled;
    private Map<String, Integer> generatorCounts;
    private long totalItemsGenerated;
    private long totalExpGenerated;
    private int jackpotContributions;
    
    public PlayerData(UUID playerUUID, String playerName, int defaultSlots, int maxSlots) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.generatorSlots = defaultSlots;
        this.maxGeneratorSlots = maxSlots;
        this.hologramsEnabled = true;
        this.generatorCounts = new HashMap<>();
        this.totalItemsGenerated = 0;
        this.totalExpGenerated = 0;
        this.jackpotContributions = 0;
    }
    
    /**
     * Check if player can place more generators
     */
    public boolean canPlaceGenerator() {
        int totalGenerators = generatorCounts.values().stream().mapToInt(Integer::intValue).sum();
        return totalGenerators < generatorSlots;
    }
    
    /**
     * Get current number of placed generators
     */
    public int getPlacedGenerators() {
        return generatorCounts.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    /**
     * Add a generator to the count
     */
    public void addGenerator(String generatorId) {
        generatorCounts.put(generatorId, generatorCounts.getOrDefault(generatorId, 0) + 1);
    }
    
    /**
     * Remove a generator from the count
     */
    public void removeGenerator(String generatorId) {
        int current = generatorCounts.getOrDefault(generatorId, 0);
        if (current > 1) {
            generatorCounts.put(generatorId, current - 1);
        } else {
            generatorCounts.remove(generatorId);
        }
    }
    
    /**
     * Get count of specific generator type
     */
    public int getGeneratorCount(String generatorId) {
        return generatorCounts.getOrDefault(generatorId, 0);
    }
    
    /**
     * Add generator slots to player
     */
    public void addSlots(int amount) {
        this.generatorSlots = Math.min(maxGeneratorSlots, this.generatorSlots + amount);
    }
    
    /**
     * Remove generator slots from player
     */
    public void removeSlots(int amount) {
        this.generatorSlots = Math.max(0, this.generatorSlots - amount);
    }
    
    /**
     * Add to total items generated
     */
    public void addItemsGenerated(int amount) {
        this.totalItemsGenerated += amount;
    }
    
    /**
     * Add to total exp generated
     */
    public void addExpGenerated(int amount) {
        this.totalExpGenerated += amount;
    }
    
    /**
     * Increment jackpot contributions
     */
    public void addJackpotContribution() {
        this.jackpotContributions++;
    }
    
    // Getters and setters
    public UUID getPlayerUUID() { return playerUUID; }
    public String getPlayerName() { return playerName; }
    public int getGeneratorSlots() { return generatorSlots; }
    public int getMaxGeneratorSlots() { return maxGeneratorSlots; }
    public boolean isHologramsEnabled() { return hologramsEnabled; }
    public Map<String, Integer> getGeneratorCounts() { return generatorCounts; }
    public long getTotalItemsGenerated() { return totalItemsGenerated; }
    public long getTotalExpGenerated() { return totalExpGenerated; }
    public int getJackpotContributions() { return jackpotContributions; }
    
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public void setGeneratorSlots(int generatorSlots) { 
        this.generatorSlots = Math.min(maxGeneratorSlots, Math.max(0, generatorSlots)); 
    }
    public void setMaxGeneratorSlots(int maxGeneratorSlots) { this.maxGeneratorSlots = maxGeneratorSlots; }
    public void setHologramsEnabled(boolean hologramsEnabled) { this.hologramsEnabled = hologramsEnabled; }
    public void setGeneratorCounts(Map<String, Integer> generatorCounts) { this.generatorCounts = generatorCounts; }
    public void setTotalItemsGenerated(long totalItemsGenerated) { this.totalItemsGenerated = totalItemsGenerated; }
    public void setTotalExpGenerated(long totalExpGenerated) { this.totalExpGenerated = totalExpGenerated; }
    public void setJackpotContributions(int jackpotContributions) { this.jackpotContributions = jackpotContributions; }
    
    /**
     * Reset all generator data
     */
    public void reset() {
        this.generatorCounts.clear();
        this.totalItemsGenerated = 0;
        this.totalExpGenerated = 0;
        this.jackpotContributions = 0;
    }
} 