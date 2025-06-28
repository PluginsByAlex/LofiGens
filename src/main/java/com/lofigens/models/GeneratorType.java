package com.lofigens.models;

public enum GeneratorType {
    ITEM,           // Generates items
    COMMAND,        // Executes commands
    EXP,            // Generates experience
    UNSTABLE,       // Can break and needs repair
    OVERCLOCKED,    // Cycles through multiple items and breaks after limit
    JACKPOT         // Contributes to jackpot
} 