package com.lofigens.listeners;

import com.lofigens.LofiGens;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class EventItemListener implements Listener {
    
    private final LofiGens plugin;
    
    public EventItemListener(LofiGens plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Handle event item usage (Time Warp, Double Items, etc.)
    }
} 