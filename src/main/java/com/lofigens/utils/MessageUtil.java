package com.lofigens.utils;

import com.lofigens.LofiGens;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MessageUtil {
    
    private static LofiGens plugin;
    
    public static void init(LofiGens pluginInstance) {
        plugin = pluginInstance;
    }
    
    /**
     * Colorize a string using legacy color codes
     */
    public static String colorize(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    /**
     * Send a message to a player
     */
    public static void sendMessage(Player player, String message) {
        if (player == null || message == null || message.isEmpty()) return;
        player.sendMessage(colorize(message));
    }
    
    /**
     * Send a message to a player with placeholders
     */
    public static void sendMessage(Player player, String message, String... placeholders) {
        String processedMessage = replacePlaceholders(message, placeholders);
        sendMessage(player, processedMessage);
    }
    
    /**
     * Replace placeholders in a message
     */
    public static String replacePlaceholders(String message, String... placeholders) {
        if (message == null) return "";
        
        String result = message;
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                result = result.replace(placeholders[i], placeholders[i + 1]);
            }
        }
        return result;
    }
    
    /**
     * Format a number with commas
     */
    public static String formatNumber(long number) {
        return String.format("%,d", number);
    }
    
    /**
     * Format a decimal number with commas
     */
    public static String formatNumber(double number) {
        return String.format("%,.2f", number);
    }
    
    /**
     * Format time in seconds to a human-readable format
     */
    public static String formatTime(int seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            return minutes + "m " + remainingSeconds + "s";
        } else {
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            int remainingSeconds = seconds % 60;
            return hours + "h " + minutes + "m " + remainingSeconds + "s";
        }
    }
    
    /**
     * Get a progress bar string
     */
    public static String getProgressBar(double current, double max, int length) {
        double percentage = Math.min(1.0, Math.max(0.0, current / max));
        int filled = (int) (percentage * length);
        
        StringBuilder bar = new StringBuilder();
        bar.append("&a");
        for (int i = 0; i < filled; i++) {
            bar.append("█");
        }
        bar.append("&7");
        for (int i = filled; i < length; i++) {
            bar.append("█");
        }
        
        return colorize(bar.toString());
    }
    
    /**
     * Broadcast a message to all online players
     */
    public static void broadcast(String message) {
        Bukkit.broadcastMessage(colorize(message));
    }
    
    /**
     * Broadcast a message to all online players with placeholders
     */
    public static void broadcast(String message, String... placeholders) {
        String processedMessage = replacePlaceholders(message, placeholders);
        broadcast(processedMessage);
    }
    
    /**
     * Send a title to a player (1.8+ method)
     */
    public static void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        if (player == null) return;
        
        try {
            // Try to use the newer title API (1.11+)
            player.sendTitle(colorize(title), colorize(subtitle), fadeIn, stay, fadeOut);
        } catch (NoSuchMethodError e) {
            // Fallback to regular chat message if title API is not available
            sendMessage(player, title);
            if (subtitle != null && !subtitle.isEmpty()) {
                sendMessage(player, subtitle);
            }
        }
    }
    
    /**
     * Send an action bar message to a player
     */
    public static void sendActionBar(Player player, String message) {
        if (player == null || message == null || message.isEmpty()) return;
        
        try {
            // Try to use Spigot's action bar method
            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                    new net.md_5.bungee.api.chat.TextComponent(colorize(message)));
        } catch (Exception e) {
            // Fallback to regular message if action bar is not available
            sendMessage(player, message);
        }
    }
} 