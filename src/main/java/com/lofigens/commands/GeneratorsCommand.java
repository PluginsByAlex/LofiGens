package com.lofigens.commands;

import com.lofigens.LofiGens;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GeneratorsCommand implements CommandExecutor {
    
    private final LofiGens plugin;
    
    public GeneratorsCommand(LofiGens plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Basic help menu for now
        player.sendMessage("§6=== LofiGens Commands ===");
        player.sendMessage("§e/generators help §7- Show this help menu");
        player.sendMessage("§e/generator slots §7- Check your generator slots");
        player.sendMessage("§e/generator sync §7- Sync your generators");
        player.sendMessage("§e/jackpot view §7- View jackpot information");
        
        return true;
    }
} 