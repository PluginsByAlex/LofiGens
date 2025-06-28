package com.lofigens.commands;

import com.lofigens.LofiGens;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EventCommand implements CommandExecutor {
    
    private final LofiGens plugin;
    
    public EventCommand(LofiGens plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("Event command - Not implemented yet");
        return true;
    }
} 