package com.lofigens.listeners;

import com.lofigens.LofiGens;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class GeneratorListener implements Listener {
    
    private final LofiGens plugin;
    
    public GeneratorListener(LofiGens plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // Handle generator placement logic here
        // This would check if the item being placed is a generator
        // and call GeneratorManager.placeGenerator()
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        // Handle generator breaking logic here
        // This would check if the block being broken is a generator
        // and call GeneratorManager.removeGenerator()
    }
} 