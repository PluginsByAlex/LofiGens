package com.lofigens;

import com.lofigens.commands.*;
import com.lofigens.listeners.*;
import com.lofigens.managers.*;
import com.lofigens.placeholders.LofiGensPlaceholders;
import com.lofigens.utils.ConfigManager;
import com.lofigens.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class LofiGens extends JavaPlugin {
    
    private static LofiGens instance;
    private Logger logger;
    
    // Managers
    private ConfigManager configManager;
    private GeneratorManager generatorManager;
    private PlayerDataManager playerDataManager;
    private EventManager eventManager;
    private JackpotManager jackpotManager;
    private HologramManager hologramManager;
    
    // Integration flags
    private boolean plotSquaredEnabled = false;
    private boolean placeholderAPIEnabled = false;
    private boolean headDatabaseEnabled = false;
    
    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        
        logger.info("LofiGens is starting up...");
        
        // Initialize configuration
        configManager = new ConfigManager(this);
        configManager.loadConfigs();
        
        // Initialize message utility
        MessageUtil.init(this);
        
        // Check for integrations
        checkIntegrations();
        
        // Initialize managers
        initializeManagers();
        
        // Register commands
        registerCommands();
        
        // Register listeners
        registerListeners();
        
        // Register placeholders if PlaceholderAPI is enabled
        if (placeholderAPIEnabled) {
            new LofiGensPlaceholders(this).register();
            logger.info("PlaceholderAPI support enabled!");
        }
        
        // Start tasks
        startTasks();
        
        logger.info("LofiGens has been enabled successfully!");
    }
    
    @Override
    public void onDisable() {
        logger.info("LofiGens is shutting down...");
        
        // Save all data
        if (playerDataManager != null) {
            playerDataManager.saveAllData();
        }
        
        if (jackpotManager != null) {
            jackpotManager.saveData();
        }
        
        // Cancel all tasks
        Bukkit.getScheduler().cancelTasks(this);
        
        logger.info("LofiGens has been disabled.");
    }
    
    private void checkIntegrations() {
        // Check for PlotSquared
        if (Bukkit.getPluginManager().getPlugin("PlotSquared") != null) {
            plotSquaredEnabled = true;
            logger.info("PlotSquared integration enabled!");
        }
        
        // Check for PlaceholderAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderAPIEnabled = true;
            logger.info("PlaceholderAPI integration enabled!");
        }
        
        // Check for HeadDatabase
        if (Bukkit.getPluginManager().getPlugin("HeadDatabase") != null) {
            headDatabaseEnabled = true;
            logger.info("HeadDatabase integration enabled!");
        }
    }
    
    private void initializeManagers() {
        playerDataManager = new PlayerDataManager(this);
        generatorManager = new GeneratorManager(this);
        eventManager = new EventManager(this);
        jackpotManager = new JackpotManager(this);
        hologramManager = new HologramManager(this);
        
        logger.info("All managers initialized successfully!");
    }
    
    private void registerCommands() {
        // Main generator commands
        getCommand("generators").setExecutor(new GeneratorsCommand(this));
        getCommand("generator").setExecutor(new GeneratorCommand(this));
        
        // Jackpot commands
        getCommand("jackpot").setExecutor(new JackpotCommand(this));
        
        // Event items commands
        getCommand("eventitems").setExecutor(new EventItemsCommand(this));
        
        // Event force commands
        getCommand("event").setExecutor(new EventCommand(this));
        
        logger.info("Commands registered successfully!");
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new GeneratorListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new EventItemListener(this), this);
        getServer().getPluginManager().registerEvents(new JackpotListener(this), this);
        
        logger.info("Event listeners registered successfully!");
    }
    
    private void startTasks() {
        // Start generator tasks
        generatorManager.startGeneratorTasks();
        
        // Start jackpot task
        jackpotManager.startJackpotTask();
        
        // Start event cleanup task
        eventManager.startCleanupTask();
        
        logger.info("Background tasks started successfully!");
    }
    
    // Getters for managers and utilities
    public static LofiGens getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public GeneratorManager getGeneratorManager() {
        return generatorManager;
    }
    
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
    
    public EventManager getEventManager() {
        return eventManager;
    }
    
    public JackpotManager getJackpotManager() {
        return jackpotManager;
    }
    
    public HologramManager getHologramManager() {
        return hologramManager;
    }
    
    // Integration checks
    public boolean isPlotSquaredEnabled() {
        return plotSquaredEnabled && configManager.getConfig().getBoolean("enable-plotsquared-integration", true);
    }
    
    public boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }
    
    public boolean isHeadDatabaseEnabled() {
        return headDatabaseEnabled;
    }
    
    public void reload() {
        logger.info("Reloading LofiGens...");
        
        // Save current data
        playerDataManager.saveAllData();
        jackpotManager.saveData();
        
        // Reload configs
        configManager.reloadConfigs();
        
        // Restart managers
        generatorManager.reload();
        eventManager.reload();
        jackpotManager.reload();
        hologramManager.reload();
        
        logger.info("LofiGens reloaded successfully!");
    }
} 