package com.lofigens.managers;

import com.lofigens.LofiGens;
import com.lofigens.models.Generator;
import com.lofigens.utils.MessageUtil;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

public class HologramManager {
    
    private final LofiGens plugin;
    private final Map<Location, ArmorStand> holograms;
    
    public HologramManager(LofiGens plugin) {
        this.plugin = plugin;
        this.holograms = new HashMap<>();
    }
    
    /**
     * Create a hologram for a generator
     */
    public void createHologram(Generator generator) {
        if (!plugin.getConfigManager().isHologramsEnabled()) {
            return;
        }
        
        Location location = generator.getLocation();
        removeHologram(location); // Remove existing hologram if any
        
        // Create hologram location (above the generator block)
        double heightOffset = plugin.getConfigManager().getConfig().getDouble("holograms.height-offset", 1.5);
        Location hologramLocation = location.clone().add(0.5, heightOffset, 0.5);
        
        // Create armor stand as hologram
        ArmorStand hologram = (ArmorStand) location.getWorld().spawnEntity(hologramLocation, EntityType.ARMOR_STAND);
        
        // Configure armor stand
        hologram.setVisible(false);
        hologram.setGravity(false);
        hologram.setCanPickupItems(false);
        hologram.setCustomNameVisible(true);
        hologram.setMarker(true); // Makes it non-collidable
        hologram.setInvulnerable(true);
        
        // Set initial name
        updateHologramText(hologram, generator);
        
        // Store the hologram
        holograms.put(location, hologram);
    }
    
    /**
     * Update hologram for a generator
     */
    public void updateHologram(Generator generator) {
        if (!plugin.getConfigManager().isHologramsEnabled()) {
            return;
        }
        
        ArmorStand hologram = holograms.get(generator.getLocation());
        if (hologram != null && !hologram.isDead()) {
            updateHologramText(hologram, generator);
        } else {
            // Hologram doesn't exist or is dead, create a new one
            createHologram(generator);
        }
    }
    
    /**
     * Update hologram text
     */
    private void updateHologramText(ArmorStand hologram, Generator generator) {
        StringBuilder text = new StringBuilder();
        
        // Generator name
        text.append(MessageUtil.colorize(generator.getName()));
        
        if (generator.isWorking()) {
            // Working status with progress
            int timeUntilNext = generator.getTimeUntilNextGeneration();
            if (timeUntilNext > 0) {
                text.append("\n").append(MessageUtil.colorize("&7Next: &f" + timeUntilNext + "s"));
            } else {
                text.append("\n").append(MessageUtil.colorize("&aGenerating..."));
            }
            
            // Type-specific information
            switch (generator.getType()) {
                case OVERCLOCKED:
                    int remaining = generator.getMaxGenerations() - generator.getCurrentGenerations();
                    text.append("\n").append(MessageUtil.colorize("&7Remaining: &f" + remaining));
                    break;
                    
                case UNSTABLE:
                    double breakChance = generator.getBreakChance() * 100;
                    text.append("\n").append(MessageUtil.colorize("&7Break Chance: &f" + String.format("%.1f%%", breakChance)));
                    break;
            }
        } else {
            // Broken status
            text.append("\n").append(MessageUtil.colorize("&cBROKEN"));
            
            if (generator.getType() == generator.getType().UNSTABLE) {
                text.append("\n").append(MessageUtil.colorize("&7Repair with: &f" + generator.getRepairCost() + " items"));
            }
        }
        
        hologram.setCustomName(text.toString());
    }
    
    /**
     * Remove hologram at location
     */
    public void removeHologram(Location location) {
        ArmorStand hologram = holograms.remove(location);
        if (hologram != null && !hologram.isDead()) {
            hologram.remove();
        }
    }
    
    /**
     * Remove all holograms
     */
    public void removeAllHolograms() {
        for (ArmorStand hologram : holograms.values()) {
            if (hologram != null && !hologram.isDead()) {
                hologram.remove();
            }
        }
        holograms.clear();
    }
    
    /**
     * Update all holograms
     */
    public void updateAllHolograms() {
        if (!plugin.getConfigManager().isHologramsEnabled()) {
            removeAllHolograms();
            return;
        }
        
        // This would be called from a repeating task
        for (Map.Entry<Location, ArmorStand> entry : holograms.entrySet()) {
            Location location = entry.getKey();
            ArmorStand hologram = entry.getValue();
            
            // Check if hologram still exists
            if (hologram == null || hologram.isDead()) {
                holograms.remove(location);
                continue;
            }
            
            // Get generator at this location
            Generator generator = plugin.getGeneratorManager().getGenerator(location);
            if (generator != null) {
                updateHologramText(hologram, generator);
            } else {
                // Generator no longer exists, remove hologram
                hologram.remove();
                holograms.remove(location);
            }
        }
    }
    
    /**
     * Check if holograms are enabled and update accordingly
     */
    public void toggleHolograms(boolean enabled) {
        if (!enabled) {
            removeAllHolograms();
        } else {
            // Recreate holograms for all generators
            // This would be called when holograms are re-enabled
            recreateAllHolograms();
        }
    }
    
    /**
     * Recreate all holograms
     */
    private void recreateAllHolograms() {
        removeAllHolograms();
        
        // Get all active generators and create holograms for them
        // This is a simplified approach - in practice you'd iterate through all active generators
    }
    
    /**
     * Reload the hologram manager
     */
    public void reload() {
        if (!plugin.getConfigManager().isHologramsEnabled()) {
            removeAllHolograms();
        } else {
            updateAllHolograms();
        }
    }
    
    /**
     * Get hologram count
     */
    public int getHologramCount() {
        return holograms.size();
    }
    
    /**
     * Check if hologram exists at location
     */
    public boolean hasHologram(Location location) {
        ArmorStand hologram = holograms.get(location);
        return hologram != null && !hologram.isDead();
    }
} 