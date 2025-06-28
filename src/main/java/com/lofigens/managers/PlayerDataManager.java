package com.lofigens.managers;

import com.lofigens.LofiGens;
import com.lofigens.models.PlayerData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {
    
    private final LofiGens plugin;
    private final Map<UUID, PlayerData> playerDataMap;
    
    public PlayerDataManager(LofiGens plugin) {
        this.plugin = plugin;
        this.playerDataMap = new ConcurrentHashMap<>();
        loadAllPlayerData();
    }
    
    /**
     * Load all player data from configuration file
     */
    private void loadAllPlayerData() {
        FileConfiguration config = plugin.getConfigManager().getPlayersConfig();
        
        if (config.getConfigurationSection("players") == null) {
            return;
        }
        
        ConfigurationSection playersSection = config.getConfigurationSection("players");
        
        for (String uuidString : playersSection.getKeys(false)) {
            try {
                UUID playerUUID = UUID.fromString(uuidString);
                ConfigurationSection playerSection = playersSection.getConfigurationSection(uuidString);
                
                if (playerSection != null) {
                    PlayerData playerData = loadPlayerDataFromConfig(playerUUID, playerSection);
                    playerDataMap.put(playerUUID, playerData);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in players.yml: " + uuidString);
            }
        }
        
        plugin.getLogger().info("Loaded data for " + playerDataMap.size() + " players");
    }
    
    /**
     * Load player data from configuration section
     */
    private PlayerData loadPlayerDataFromConfig(UUID playerUUID, ConfigurationSection section) {
        String playerName = section.getString("name", "Unknown");
        int generatorSlots = section.getInt("generator_slots", plugin.getConfigManager().getDefaultGeneratorSlots());
        int maxSlots = plugin.getConfigManager().getMaxGeneratorSlots();
        
        PlayerData playerData = new PlayerData(playerUUID, playerName, generatorSlots, maxSlots);
        
        // Load other data
        playerData.setHologramsEnabled(section.getBoolean("holograms_enabled", true));
        playerData.setTotalItemsGenerated(section.getLong("total_items_generated", 0));
        playerData.setTotalExpGenerated(section.getLong("total_exp_generated", 0));
        playerData.setJackpotContributions(section.getInt("jackpot_contributions", 0));
        
        // Load generator counts
        ConfigurationSection generatorCountsSection = section.getConfigurationSection("generator_counts");
        if (generatorCountsSection != null) {
            Map<String, Integer> generatorCounts = new HashMap<>();
            for (String generatorId : generatorCountsSection.getKeys(false)) {
                generatorCounts.put(generatorId, generatorCountsSection.getInt(generatorId));
            }
            playerData.setGeneratorCounts(generatorCounts);
        }
        
        return playerData;
    }
    
    /**
     * Get or create player data
     */
    public PlayerData getPlayerData(UUID playerUUID) {
        return playerDataMap.computeIfAbsent(playerUUID, uuid -> {
            // Create new player data with default values
            return new PlayerData(
                uuid, 
                "Unknown", 
                plugin.getConfigManager().getDefaultGeneratorSlots(),
                plugin.getConfigManager().getMaxGeneratorSlots()
            );
        });
    }
    
    /**
     * Get or create player data by player object
     */
    public PlayerData getPlayerData(Player player) {
        PlayerData data = getPlayerData(player.getUniqueId());
        data.setPlayerName(player.getName()); // Update name in case it changed
        return data;
    }
    
    /**
     * Save player data to configuration
     */
    public void savePlayerData(UUID playerUUID) {
        PlayerData playerData = playerDataMap.get(playerUUID);
        if (playerData == null) return;
        
        FileConfiguration config = plugin.getConfigManager().getPlayersConfig();
        String path = "players." + playerUUID.toString();
        
        config.set(path + ".name", playerData.getPlayerName());
        config.set(path + ".generator_slots", playerData.getGeneratorSlots());
        config.set(path + ".holograms_enabled", playerData.isHologramsEnabled());
        config.set(path + ".total_items_generated", playerData.getTotalItemsGenerated());
        config.set(path + ".total_exp_generated", playerData.getTotalExpGenerated());
        config.set(path + ".jackpot_contributions", playerData.getJackpotContributions());
        
        // Save generator counts
        config.set(path + ".generator_counts", null); // Clear existing
        if (!playerData.getGeneratorCounts().isEmpty()) {
            for (Map.Entry<String, Integer> entry : playerData.getGeneratorCounts().entrySet()) {
                config.set(path + ".generator_counts." + entry.getKey(), entry.getValue());
            }
        }
        
        plugin.getConfigManager().savePlayersConfig();
    }
    
    /**
     * Save all player data
     */
    public void saveAllData() {
        for (UUID playerUUID : playerDataMap.keySet()) {
            savePlayerData(playerUUID);
        }
        plugin.getLogger().info("Saved data for " + playerDataMap.size() + " players");
    }
    
    /**
     * Get top players by items generated
     */
    public List<PlayerData> getTopPlayersByItems(int limit) {
        return playerDataMap.values().stream()
            .sorted((a, b) -> Long.compare(b.getTotalItemsGenerated(), a.getTotalItemsGenerated()))
            .limit(limit)
            .toList();
    }
    
    /**
     * Get top players by exp generated
     */
    public List<PlayerData> getTopPlayersByExp(int limit) {
        return playerDataMap.values().stream()
            .sorted((a, b) -> Long.compare(b.getTotalExpGenerated(), a.getTotalExpGenerated()))
            .limit(limit)
            .toList();
    }
    
    /**
     * Get top players by jackpot contributions
     */
    public List<PlayerData> getTopPlayersByJackpot(int limit) {
        return playerDataMap.values().stream()
            .sorted((a, b) -> Integer.compare(b.getJackpotContributions(), a.getJackpotContributions()))
            .limit(limit)
            .toList();
    }
    
    /**
     * Reset all player data
     */
    public void resetAllData() {
        playerDataMap.clear();
        
        FileConfiguration config = plugin.getConfigManager().getPlayersConfig();
        config.set("players", null);
        plugin.getConfigManager().savePlayersConfig();
        
        plugin.getLogger().info("Reset all player data");
    }
    
    /**
     * Remove player data
     */
    public void removePlayerData(UUID playerUUID) {
        playerDataMap.remove(playerUUID);
        
        FileConfiguration config = plugin.getConfigManager().getPlayersConfig();
        config.set("players." + playerUUID.toString(), null);
        plugin.getConfigManager().savePlayersConfig();
    }
    
    /**
     * Get all loaded player data
     */
    public Collection<PlayerData> getAllPlayerData() {
        return playerDataMap.values();
    }
    
    /**
     * Check if player data is loaded
     */
    public boolean isPlayerDataLoaded(UUID playerUUID) {
        return playerDataMap.containsKey(playerUUID);
    }
    
    /**
     * Get total number of players with data
     */
    public int getTotalPlayers() {
        return playerDataMap.size();
    }
} 