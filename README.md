# HoloFX - Advanced Hologram Management Plugin

A powerful and highly customizable Spigot/Bukkit plugin for creating, managing, and editing holograms with advanced features including per-line permissions, dynamic editing, and optimized batch loading.

## Features

### 🎯 Core Functionality
- **Create & Manage Holograms** - Easily create and manage multiple holograms with custom text
- **Advanced Editing System** - 20+ editing commands for complete hologram customization
- **Per-Line Permissions** - Control line visibility based on player permissions
- **Batch Loading** - Optimized asynchronous hologram loading to prevent server lag
- **Dynamic Positioning** - Move and teleport holograms in real-time with interactive mode

### 🎨 Customization Options
- **Text Properties**: Font, opacity, shadow strength, shadow radius
- **Display Settings**: Billboard modes (fixed, vertical, horizontal, center), view range
- **Transformations**: Position offset (X, Y, Z), scale transformation, rotation
- **Visual Effects**: Background color, line width, see-through mode, brightness

### 🔒 Advanced Features
- **Permission-Based Visibility** - Show/hide individual lines based on player permissions
- **Edit Queue System** - Prevents concurrent edits on the same hologram
- **Persistence** - YAML-based configuration with auto-save functionality
- **Multi-World Support** - Seamlessly manage holograms across multiple worlds
- **Namespaced PDC Keys** - Unique identification for all hologram entities

### ⚙️ Developer-Friendly
- **Modular Architecture** - Clean separation of concerns with dedicated managers
- **Configuration Management** - Centralized message system (messages.yml)
- **Tab Completion** - Full tab-completion support for all commands
- **Batch Processing** - Configurable batch size and delays for optimal performance

## Commands

### Main Commands
```
/holo create <name> <text>          - Create a new hologram at your location
/holo list [page]                   - List all loaded holograms (with pagination)
/holo remove <name>                 - Remove a hologram and delete its configuration
/holo summon <name>                 - Spawn an inactive hologram at your location
/holo teleport <name>               - Move hologram to your current position
/holo move <name>                   - Enter interactive move mode
/holo save [name]                   - Save hologram(s) to configuration files
/holo reload [name]                 - Reload hologram configuration(s)
/holo settings <name>               - Open hologram settings interface
/holo edit <name> <action> [args]   - Edit hologram properties
```

### Edit Actions
- **addline** - Add a new line to the hologram
- **editline** - Edit existing line text
- **removeline** - Remove a specific line
- **set_offset_x/y/z** - Adjust position offsets
- **set_scale** - Change hologram scale [x,y,z]
- **set_alignment** - Set text alignment
- **set_billboard** - Set billboard mode
- **set_opacity** - Control text transparency
- **set_shadow_strength** - Adjust shadow effect
- **set_shadow_radius** - Modify shadow radius
- **set_permission** - Set line-specific permissions
- **set_background** - Configure background color
- **set_brightness** - Adjust brightness
- **And more...**

## Permissions

```yaml
holo.use              - Access to HoloFX commands (default: op)
holo.create           - Create new holograms (default: op)
holo.list             - List holograms (default: op)
holo.summon           - Spawn holograms (default: op)
holo.remove           - Remove holograms (default: op)
holo.reload           - Reload configurations (default: op)
holo.edit             - Edit holograms (default: op)
holo.settings         - Access settings interface (default: op)
holo.teleport         - Teleport to hologram (default: op)
```

## Configuration

### Installation
1. Download the latest release JAR file
2. Place it in your `plugins/` folder
3. Start your server - the plugin will generate default configuration files
4. Edit `plugins/HoloFX/messages.yml` to customize messages
5. Create hologram files in `plugins/HoloFX/holograms/` directory

### Hologram File Format (YAML)
```yaml
name: "ExampleHologram"
enabled: true
location:
  world: "world"
  x: 0.0
  y: 64.0
  z: 0.0
  yaw: 0.0
  pitch: 0.0
lines:
  - text: "&bLine 1"
    display_settings:
      billboard: fixed
      offset_x: 0.0
      offset_y: 0.0
      offset_z: 0.0
  - text: "&aLine 2"
    display_settings:
      permission: "holo.vip"
```

## Performance

- **Batch Loading**: Configurable batch processing prevents server lag during hologram spawning
- **Concurrent Processing**: Thread-safe hologram management with ConcurrentHashMap
- **Visibility Caching**: Optimized player visibility calculations
- **Async Operations**: Non-blocking file I/O and configuration loading

## License

This project is licensed under a Custom License - see LICENSE.txt for details.

## Author

**oneyz_** - Developer

