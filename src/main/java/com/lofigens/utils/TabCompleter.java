package com.lofigens.utils;

import com.lofigens.LofiGens;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TabCompleter implements org.bukkit.command.TabCompleter {
    
    private final LofiGens plugin;
    
    public TabCompleter(LofiGens plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        switch (command.getName().toLowerCase()) {
            case "generators":
                return handleGeneratorsTab(sender, args);
            case "generator":
                return handleGeneratorTab(sender, args);
            case "jackpot":
                return handleJackpotTab(sender, args);
            case "eventitems":
                return handleEventItemsTab(sender, args);
            case "event":
                return handleEventTab(sender, args);
        }
        
        return completions;
    }
    
    private List<String> handleGeneratorsTab(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Main subcommands
            completions.addAll(Arrays.asList("help", "give", "slots", "reset", "holograms"));
            return filterCompletions(completions, args[0]);
        }
        
        if (args.length >= 2) {
            switch (args[0].toLowerCase()) {
                case "give":
                    if (args.length == 2) {
                        // Player names
                        return getOnlinePlayerNames(args[1]);
                    } else if (args.length == 3) {
                        // Generator types
                        completions.addAll(getGeneratorTypes());
                        return filterCompletions(completions, args[2]);
                    } else if (args.length == 4) {
                        // Amount (suggest common amounts)
                        completions.addAll(Arrays.asList("1", "5", "10", "16", "32", "64"));
                        return filterCompletions(completions, args[3]);
                    }
                    break;
                    
                case "slots":
                    if (args.length == 2) {
                        completions.addAll(Arrays.asList("add", "remove"));
                        return filterCompletions(completions, args[1]);
                    } else if (args.length == 3) {
                        // Player names
                        return getOnlinePlayerNames(args[2]);
                    } else if (args.length == 4) {
                        // Amount
                        completions.addAll(Arrays.asList("1", "5", "10", "25", "50"));
                        return filterCompletions(completions, args[3]);
                    }
                    break;
                    
                case "holograms":
                    if (args.length == 2) {
                        completions.addAll(Arrays.asList("on", "off", "toggle"));
                        return filterCompletions(completions, args[1]);
                    }
                    break;
            }
        }
        
        return completions;
    }
    
    private List<String> handleGeneratorTab(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("slots", "sync"));
            return filterCompletions(completions, args[0]);
        }
        
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "slots":
                    // Player names (optional)
                    return getOnlinePlayerNames(args[1]);
                case "sync":
                    // No additional parameters
                    break;
            }
        }
        
        return completions;
    }
    
    private List<String> handleJackpotTab(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("view", "draw", "revoke"));
            return filterCompletions(completions, args[0]);
        }
        
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "revoke":
                    // Player names
                    return getOnlinePlayerNames(args[1]);
                case "view":
                case "draw":
                    // No additional parameters
                    break;
            }
        }
        
        return completions;
    }
    
    private List<String> handleEventItemsTab(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("give");
            return filterCompletions(completions, args[0]);
        }
        
        if (args.length >= 2 && args[0].equalsIgnoreCase("give")) {
            if (args.length == 2) {
                // Player names
                return getOnlinePlayerNames(args[1]);
            } else if (args.length == 3) {
                // Event item types
                completions.addAll(getEventItemTypes());
                return filterCompletions(completions, args[2]);
            } else if (args.length == 4) {
                // Amount
                completions.addAll(Arrays.asList("1", "5", "10", "16", "32"));
                return filterCompletions(completions, args[3]);
            }
        }
        
        return completions;
    }
    
    private List<String> handleEventTab(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.add("force");
            return filterCompletions(completions, args[0]);
        }
        
        if (args.length >= 2 && args[0].equalsIgnoreCase("force")) {
            if (args.length == 2) {
                // Event types
                completions.addAll(Arrays.asList("doubleitem", "timewarp"));
                return filterCompletions(completions, args[1]);
            } else if (args.length == 3) {
                // Time duration (in seconds)
                completions.addAll(Arrays.asList("60", "300", "600", "1800", "3600"));
                return filterCompletions(completions, args[2]);
            } else if (args.length == 4) {
                // Global or player
                completions.addAll(Arrays.asList("global", "player"));
                return filterCompletions(completions, args[3]);
            } else if (args.length == 5 && args[3].equalsIgnoreCase("player")) {
                // Player names
                return getOnlinePlayerNames(args[4]);
            }
        }
        
        return completions;
    }
    
    private List<String> getOnlinePlayerNames(String partial) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(partial.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    private List<String> getGeneratorTypes() {
        List<String> types = new ArrayList<>();
        
        // Get generator types from config
        if (plugin.getConfigManager().getGeneratorsSection() != null) {
            types.addAll(plugin.getConfigManager().getGeneratorsSection().getKeys(false));
        }
        
        // Fallback to common types if config not loaded
        if (types.isEmpty()) {
            types.addAll(Arrays.asList("basic", "fast", "overclocked", "unstable", "jackpot", "exp"));
        }
        
        return types;
    }
    
    private List<String> getEventItemTypes() {
        List<String> types = new ArrayList<>();
        
        // Get event item types from config
        if (plugin.getConfigManager().getEventItemsSection() != null) {
            types.addAll(plugin.getConfigManager().getEventItemsSection().getKeys(false));
        }
        
        // Fallback to common types if config not loaded
        if (types.isEmpty()) {
            types.addAll(Arrays.asList("double_item_token", "time_warp_token"));
        }
        
        return types;
    }
    
    private List<String> filterCompletions(List<String> completions, String partial) {
        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(partial.toLowerCase()))
                .collect(Collectors.toList());
    }
} 