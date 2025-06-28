package com.lofigens.listeners;

import com.lofigens.LofiGens;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    
    private final LofiGens plugin;
    
    public PlayerListener(LofiGens plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Initialize player data
        plugin.getPlayerDataManager().getPlayerData(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Save player data
        plugin.getPlayerDataManager().savePlayerData(event.getPlayer().getUniqueId());
    }
} 