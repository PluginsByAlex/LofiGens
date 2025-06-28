package com.lofigens.utils;

import com.lofigens.LofiGens;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class ConfigManager {
    
    private final LofiGens plugin;
    private FileConfiguration config;
    private FileConfiguration playersConfig;
    private FileConfiguration jackpotConfig;
    
    private File configFile;
    private File playersFile;
    private File jackpotFile;
    
    public ConfigManager(LofiGens plugin) {
        this.plugin = plugin;
    }
    
    public void loadConfigs() {
        createConfigFiles();
        loadMainConfig();
        loadPlayersConfig();
        loadJackpotConfig();
    }
    
    private void createConfigFiles() {
        // Create plugin data folder if it doesn't exist
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        // Main config file
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }
        
        // Players data file
        playersFile = new File(plugin.getDataFolder(), "players.yml");
        if (!playersFile.exists()) {
            try {
                playersFile.createNewFile();
                playersConfig = YamlConfiguration.loadConfiguration(playersFile);
                playersConfig.save(playersFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create players.yml!", e);
            }
        }
        
        // Jackpot data file
        jackpotFile = new File(plugin.getDataFolder(), "jackpot.yml");
        if (!jackpotFile.exists()) {
            try {
                jackpotFile.createNewFile();
                jackpotConfig = YamlConfiguration.loadConfiguration(jackpotFile);
                jackpotConfig.set("current_amount", 0);
                jackpotConfig.set("last_winner", "");
                jackpotConfig.set("last_amount", 0);
                jackpotConfig.save(jackpotFile);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create jackpot.yml!", e);
            }
        }
    }
    
    private void loadMainConfig() {
        config = plugin.getConfig();
        
        // Set default values if they don't exist
        if (!config.contains("enable-plotsquared-integration")) {
            config.set("enable-plotsquared-integration", true);
        }
        
        if (!config.contains("default-generator-slots")) {
            config.set("default-generator-slots", 5);
        }
        
        if (!config.contains("max-generator-slots")) {
            config.set("max-generator-slots", 50);
        }
        
        saveMainConfig();
    }
    
    private void loadPlayersConfig() {
        playersConfig = YamlConfiguration.loadConfiguration(playersFile);
    }
    
    private void loadJackpotConfig() {
        jackpotConfig = YamlConfiguration.loadConfiguration(jackpotFile);
    }
    
    public void reloadConfigs() {
        plugin.reloadConfig();
        loadMainConfig();
        loadPlayersConfig();
        loadJackpotConfig();
    }
    
    public void saveMainConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config.yml!", e);
        }
    }
    
    public void savePlayersConfig() {
        try {
            playersConfig.save(playersFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save players.yml!", e);
        }
    }
    
    public void saveJackpotConfig() {
        try {
            jackpotConfig.save(jackpotFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save jackpot.yml!", e);
        }
    }
    
    // Getters
    public FileConfiguration getConfig() {
        return config;
    }
    
    public FileConfiguration getPlayersConfig() {
        return playersConfig;
    }
    
    public FileConfiguration getJackpotConfig() {
        return jackpotConfig;
    }
    
    // Helper methods
    public String getMessage(String key) {
        return MessageUtil.colorize(config.getString("messages." + key, "&cMessage not found: " + key));
    }
    
    public String getMessage(String key, String... placeholders) {
        String message = getMessage(key);
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace(placeholders[i], placeholders[i + 1]);
            }
        }
        return message;
    }
    
    public ConfigurationSection getGeneratorsSection() {
        return config.getConfigurationSection("generators");
    }
    
    public ConfigurationSection getEventItemsSection() {
        return config.getConfigurationSection("event_items");
    }
    
    public ConfigurationSection getJackpotSection() {
        return config.getConfigurationSection("jackpot");
    }
    
    public boolean isSoundsEnabled() {
        return config.getBoolean("sounds.enabled", true);
    }
    
    public boolean isParticlesEnabled() {
        return config.getBoolean("particles.enabled", true);
    }
    
    public boolean isHologramsEnabled() {
        return config.getBoolean("holograms.enabled", true);
    }
    
    public int getDefaultGeneratorSlots() {
        return config.getInt("default-generator-slots", 5);
    }
    
    public int getMaxGeneratorSlots() {
        return config.getInt("max-generator-slots", 50);
    }
} 