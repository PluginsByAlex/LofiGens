# LofiGens - Advanced Generator Plugin for Minecraft

LofiGens is a comprehensive generator plugin for Minecraft 1.21.4 that provides multiple types of generators, event items, jackpot system, and extensive customization options.

## ✨ Features

### 🏗️ Generator Types
- **Item Generators** - Generate specific items over time
- **Command Generators** - Execute custom commands periodically
- **EXP Generators** - Generate experience orbs
- **Unstable Generators** - Can break and require repair
- **Overclocked Generators** - Cycle through multiple items and break after limit
- **Jackpot Generators** - Contribute to server-wide jackpot

### 🧰 Event Items
- **Time Warp** - Instantly claim generated items from all your generators
- **Double Items** - Temporarily double generation output (global or per-player)

### 🎰 Jackpot System
- Server-wide jackpot with automatic drawings
- Player contribution tracking
- Admin controls for force draws and player revocation

### 🔌 Integrations
- **PlotSquared** - Restrict generator placement to owned plots (toggleable)
- **PlaceholderAPI** - Extensive placeholder support for external plugins
- **HeadDatabase** - Custom heads for jackpot GUI

### 🎯 Advanced Features
- Per-player generator slots with configurable limits
- Hologram displays above generators (toggleable)
- Generator synchronization
- Repair system for unstable generators
- Comprehensive permission system
- Sound and particle effects
- MiniMessage support for modern text formatting

## 📦 Installation

1. Download the latest release from [GitHub Releases](https://github.com/pluginsbyalex/LofiGens/releases)
2. Place the `LofiGens.jar` file in your server's `plugins` folder
3. Restart your server
4. Configure the plugin by editing `plugins/LofiGens/config.yml`

## 🔧 Configuration

The plugin comes with a comprehensive configuration file that allows you to:

- Define custom generator types with unique properties
- Configure event item behavior and durations
- Set up jackpot settings and rewards
- Customize messages, sounds, and particles
- Configure PlotSquared integration
- Set player slot limits and defaults

### Example Generator Configuration

```yaml
generators:
  coal_generator:
    type: ITEM
    name: "&7Coal Generator"
    item: COAL
    block_type: COAL_BLOCK
    spawn_interval: 1
    upgrade_to: iron_generator
    upgrade_price: 5000
```

## 📋 Commands

### Player Commands
- `/generators` - Display help menu
- `/generator slots [player]` - Check generator slots
- `/generator sync` - Synchronize all generators
- `/generators holograms <on/off>` - Toggle holograms
- `/jackpot view` - View jackpot information

### Admin Commands
- `/generators give <player> <generator> [amount]` - Give generators to players
- `/generators slots add <player> <amount>` - Add generator slots
- `/generators slots remove <player> <amount>` - Remove generator slots
- `/generators reset` - Reset all plugin data
- `/eventitems give <player> <item> [amount]` - Give event items
- `/event force <event> <time> <global/player>` - Force events
- `/jackpot draw` - Force jackpot draw
- `/jackpot revoke <player>` - Revoke player from jackpot

## 🔑 Permissions

- `lofigens.*` - All permissions (default: op)
- `lofigens.use` - Basic generator usage (default: true)
- `lofigens.give` - Give generators to players (default: op)
- `lofigens.slots.add` - Add generator slots (default: op)
- `lofigens.slots.remove` - Remove generator slots (default: op)
- `lofigens.reset` - Reset plugin data (default: op)
- `lofigens.eventitems.give` - Give event items (default: op)
- `lofigens.event.force` - Force events (default: op)
- `lofigens.jackpot.view` - View jackpot (default: true)
- `lofigens.jackpot.forcedraw` - Force jackpot draw (default: op)
- `lofigens.jackpot.revoke` - Revoke jackpot winner (default: op)

## 🔢 Placeholders (PlaceholderAPI)

- `%lofigens_amount%` - Player's placed generators
- `%lofigens_amount_max%` - Player's maximum generator slots
- `%lofigens_top_player_[1-10]%` - Top player names by items generated
- `%lofigens_top_amount_[1-10]%` - Top amounts by items generated
- `%lofigens_jackpot_amount%` - Current jackpot amount
- `%lofigens_jackpot_last_winner%` - Last jackpot winner
- `%lofigens_jackpot_last_amount%` - Last jackpot win amount
- `%lofigens_jackpot_contribution%` - Player's jackpot contributions
- `%lofigens_event_doubleitem_status%` - Double items event status
- `%lofigens_event_doubleitem_time%` - Remaining double items time

## 🏗️ Building from Source

### Prerequisites
- Java 21 or higher
- Maven 3.6 or higher

### Build Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/pluginsbyalex/LofiGens.git
   cd LofiGens
   ```

2. Build with Maven:
   ```bash
   mvn clean package
   ```

3. The compiled JAR will be in the `target` folder

## 🐛 Bug Reports & Feature Requests

Please use the [GitHub Issues](https://github.com/pluginsbyalex/LofiGens/issues) page to report bugs or request new features.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📞 Support

- Discord: [Join our Discord server](https://discord.gg/yourserver)
- GitHub Issues: [Report bugs here](https://github.com/pluginsbyalex/LofiGens/issues)
- SpigotMC: [Plugin page](https://www.spigotmc.org/resources/lofigens.xxxxx/)

## 🔄 Changelog

### v1.0.0
- Initial release
- All generator types implemented
- Event item system
- Jackpot functionality
- PlaceholderAPI integration
- PlotSquared support
- Hologram system

---

**Made with ❤️ for the Minecraft community** 