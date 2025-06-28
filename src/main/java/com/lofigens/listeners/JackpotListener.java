package com.lofigens.listeners;

import com.lofigens.LofiGens;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class JackpotListener implements Listener {
    
    private final LofiGens plugin;
    
    public JackpotListener(LofiGens plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Handle jackpot generator right-clicks
    }
} 