package com.lofigens.placeholders;

import com.lofigens.LofiGens;
import com.lofigens.models.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.List;

public class LofiGensPlaceholders extends PlaceholderExpansion {
    
    private final LofiGens plugin;
    
    public LofiGensPlaceholders(LofiGens plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public boolean canRegister() {
        return true;
    }
    
    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }
    
    @Override
    public String getIdentifier() {
        return "lofigens";
    }
    
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) {
            return "";
        }
        
        PlayerData playerData = plugin.getPlayerDataManager().getPlayerData(player);
        
        // Individual player placeholders
        switch (params.toLowerCase()) {
            case "amount":
                return String.valueOf(playerData.getPlacedGenerators());
                
            case "amount_max":
                return String.valueOf(playerData.getGeneratorSlots());
                
            case "total_items":
                return String.valueOf(playerData.getTotalItemsGenerated());
                
            case "total_exp":
                return String.valueOf(playerData.getTotalExpGenerated());
                
            case "jackpot_contributions":
                return String.valueOf(playerData.getJackpotContributions());
        }
        
        // Jackpot placeholders
        if (params.startsWith("jackpot_")) {
            switch (params.toLowerCase()) {
                case "jackpot_amount":
                    return String.valueOf(plugin.getJackpotManager().getCurrentJackpotAmount());
                    
                case "jackpot_last_winner":
                    return plugin.getJackpotManager().getLastWinner();
                    
                case "jackpot_last_amount":
                    return String.valueOf(plugin.getJackpotManager().getLastWinAmount());
            }
        }
        
        // Event placeholders
        if (params.startsWith("event_")) {
            switch (params.toLowerCase()) {
                case "event_doubleitem_status":
                    return plugin.getEventManager().isDoubleItemsActive(player.getUniqueId()) ? "Active" : "Inactive";
                    
                case "event_doubleitem_time":
                    int remaining = plugin.getEventManager().getDoubleItemsRemainingTime(player.getUniqueId());
                    return remaining > 0 ? String.valueOf(remaining) : "0";
            }
        }
        
        // Top player placeholders
        if (params.startsWith("top_player_")) {
            try {
                int position = Integer.parseInt(params.substring(11)); // Remove "top_player_"
                List<PlayerData> topPlayers = plugin.getPlayerDataManager().getTopPlayersByItems(10);
                
                if (position > 0 && position <= topPlayers.size()) {
                    return topPlayers.get(position - 1).getPlayerName();
                }
            } catch (NumberFormatException e) {
                // Invalid number
            }
            return "N/A";
        }
        
        if (params.startsWith("top_amount_")) {
            try {
                int position = Integer.parseInt(params.substring(11)); // Remove "top_amount_"
                List<PlayerData> topPlayers = plugin.getPlayerDataManager().getTopPlayersByItems(10);
                
                if (position > 0 && position <= topPlayers.size()) {
                    return String.valueOf(topPlayers.get(position - 1).getTotalItemsGenerated());
                }
            } catch (NumberFormatException e) {
                // Invalid number
            }
            return "0";
        }
        
        return null; // Placeholder not found
    }
} 