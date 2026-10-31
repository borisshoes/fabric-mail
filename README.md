<div align="center">

<img src="src/main/resources/assets/fabricmail/icon.png" alt="Fabric Mail" width="160">

# Fabric Mail

**Send messages and items to any player, online or offline.<br/>All Server-Sided; Players don't need any mods.**

[![Modrinth Downloads](https://img.shields.io/modrinth/dt/Z63eULDV?style=for-the-badge&logo=modrinth&logoColor=white&label=Modrinth&color=00AF5C)](https://modrinth.com/mod/fabric-mail)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1331022?style=for-the-badge&logo=curseforge&logoColor=white&label=CurseForge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/fabric-mail)
[![Minecraft Version](https://img.shields.io/modrinth/game-versions/Z63eULDV?style=for-the-badge&label=Minecraft&color=1F44DD)](https://modrinth.com/mod/fabric-mail/versions)
[![Latest Release](https://img.shields.io/github/v/release/borisshoes/fabric-mail?style=for-the-badge&label=Release&color=1F44DD)](https://github.com/borisshoes/fabric-mail/releases/latest)

[![Discord](https://img.shields.io/discord/1403978074550439966?style=for-the-badge&logo=discord&logoColor=white&label=Discord&color=5865F2)](https://discord.gg/dTKPeNEg6r)
[![Donate](https://img.shields.io/badge/Support-Donate-EA4AAA?style=for-the-badge)](https://borisshoes.net/donate)
[![Requires Fabric API](https://img.shields.io/badge/Requires-Fabric_API-DBD0B4?style=for-the-badge)](https://modrinth.com/mod/fabric-api)
[![Server-side](https://img.shields.io/badge/Environment-Server--side-C65135?style=for-the-badge)](#installation)

</div>

Fabric Mail is a virtual mailbox for Fabric servers. Players can leave messages and items for each other whether or not the recipient is online, and read them the next time they log on. It only needs to be installed on the server.

## Contents
- [Features](#features)
- [Getting Started](#getting-started)
- [Installation](#installation)
- [Commands](#commands)
- [Configuration](#configuration)
- [Permission Nodes](#permission-nodes)
- [Building from Source](#building-from-source)
- [Support and Contributing](#support-and-contributing)
- [Support My Work](#support-my-work)
- [Try My Other Mods!](#try-my-other-mods)
- [License](#license)

## Features
- **Mail anyone.** Send a message of up to 1024 characters to any player, including one who is offline or has never joined the server.
- **Parcels.** Attach the item stack in your hand to a message. Enchantments, names and other item data are kept.
- **Never miss a message.** Online players are told when mail arrives, and everyone else is told how much mail is waiting when they log on.
- **A mailbox GUI.** Browse your inbox and outbox in a chest menu with sorting, parcel filters and click actions, or use clickable lists in chat.
- **Unsend mistakes.** Revoke any mail still in the recipient's mailbox and get back a parcel they have not claimed.
- **Server announcements.** Admins can broadcast a message to all, online or offline players, or airdrop an item to them with it.
- **Stops item hoarding.** A configurable limit on outbound parcels stops players from using other people's mailboxes as free storage.
- **Built for servers.** No client mods, mail that is saved with the world, settings that can be changed without a restart, and a permission node for every command.

## Getting Started
1. **Send a message.** `/mail send <player> <message>` delivers it straight away, even if the player is offline. Tab completion suggests online players, but you can type any username.
2. **Send an item.** Hold a stack in your main hand and run `/mail parcel <player> <message>`.
3. **Check your mailbox.** `/mail gui` opens your mailbox and `/mail list` shows the same mail in chat. Reading a mail puts its parcel in your inventory.
4. **Clean up.** Mail stays in your mailbox until you delete it. Deleting a mail also destroys any unclaimed parcel, so you can get rid of unwanted or troll parcels without them flooding your inventory. If you want the item, read the mail first.
5. **Change your mind.** `/mail outbound` lists the mail you have sent that is still in a mailbox. Click one to revoke it and get back its parcel if it has not been claimed.

### The Mailbox GUI
The book at the top of the menu switches between your inbox and outbox. The sort button cycles through newest first, oldest first and alphabetical, and the filter button cycles through all mail, mail with a parcel and mail without one.

| Mode | Click | Action |
|---|---|---|
| Inbox | Left click | Read the mail and claim its parcel |
| Inbox | Shift + left click | Read the mail, claim its parcel and delete the mail |
| Inbox | Right click | Delete the mail, along with any unclaimed parcel |
| Outbox | Click | Revoke the mail and get back any unclaimed parcel |

### Who Can Receive Mail
Recipients are looked up by username, ignoring case: first among online players, then among players who have joined the server. If neither finds exactly one match, the name is looked up through Mojang, which lets you mail someone before their first visit. If that fails too, the sender is told the recipient does not exist and gets their parcel back. Servers can turn off the Mojang lookup with [`useMojangLookup`](#configuration).

## Installation
1. Install [Fabric Loader](https://fabricmc.net/use/server/) and [Fabric API](https://modrinth.com/mod/fabric-api) on your server.
2. Download Fabric Mail from [Modrinth](https://modrinth.com/mod/fabric-mail), [CurseForge](https://www.curseforge.com/minecraft/mc-mods/fabric-mail) or [GitHub Releases](https://github.com/borisshoes/fabric-mail/releases) and put the jar in the server's `mods` folder. Every other library the mod uses, including [BorisLib](https://github.com/borisshoes/BorisLib), is bundled in the jar.

Players do not need to install anything.

### Resource Pack
The resource pack is optional. Without it, the mailbox GUI works the same, but its frame shows vanilla items instead of custom textures. To have the server generate the pack and send it to players when they connect:
1. Launch the server with the mod installed at least once to generate the configs.
2. Open `config/polymer/auto-host.json` in the server folder.
3. In the first few lines there will be a line that says `"enabled": false`. Change this to `true`.
4. (Optional) You can also make the resource pack required, or set the `"message"` line to tell players why they are being asked to download it.

## Commands
### Player Commands
* `/mail gui` Opens the mailbox GUI.
* `/mail list` Lists your mail in chat. Click an entry to read it.
* `/mail outbound` Lists the mail you have sent that is still in the recipient's mailbox. Click an entry to revoke it.
* `/mail send <player> <message>` Sends a message to a player.
* `/mail parcel <player> <message>` Sends a message and the item stack in your main hand to a player. In creative mode, the item stays in your hand.
* `/mail delete all` Clears your mailbox, including any unclaimed parcels.

The following commands are used by the click actions in chat, but can also be run by hand. Hovering over a mail message shows its Mail ID.
* `/mail read <mailID>` Reads a mail and claims its parcel.
* `/mail delete <mailID>` Deletes a mail from your mailbox, including any unclaimed parcel.
* `/mail revoke <mailID>` Revokes a mail you have sent and returns any unclaimed parcel.

### Admin Commands
* `/mail broadcast all|online|offline <message>` Sends a message to every selected player.
* `/mail airdrop all|online|offline <message>` Sends a message to every selected player, and gives each of them a copy of the item stack in your main hand. Only a player can run this command, and in creative mode the item stays in your hand.

Broadcasts and airdrops are sent from `System` and cannot be revoked. The offline options reach every player who has joined the server before.

## Configuration
Settings are stored in `config/FabricMail.properties`. They can also be viewed and changed in game, where changes take effect immediately and are saved.

* `/mailconfig` Lists all settings.
* `/mailconfig <setting> [value]` Shows a setting, or changes it if a value is given.
* `/mailconfig reset <setting>` Resets a setting to its default.
* `/mailconfig resetAll` Resets every setting to its default.

| Setting | Default | Description |
|---|---|---|
| `maxSentParcels` | `10` | The most unclaimed outbound parcels a player can have, from 0 to 1024. 0 disables parcels. |
| `logCommandUsage` | `false` | Whether successful command usage is logged to the server console. |
| `useMojangLookup` | `true` | Whether recipients who have never joined the server are looked up through Mojang. When disabled, mail to those players fails to send. |

## Permission Nodes
Fabric Mail uses the [Fabric Permissions API](https://github.com/lucko/fabric-permissions-api) for command permissions, so it works with permission mods like LuckPerms. Each node has a fallback vanilla permission level for servers without a permissions mod.

### Player Commands
| Node | Default | Description |
|------|---------|-------------|
| `fabricmail.mail.gui` | `ALL` | Open the mailbox GUI |
| `fabricmail.mail.list` | `ALL` | List your received mail |
| `fabricmail.mail.outbound` | `ALL` | List your sent mail |
| `fabricmail.mail.read` | `ALL` | Read a mail by ID |
| `fabricmail.mail.delete` | `ALL` | Delete mail from your mailbox |
| `fabricmail.mail.send` | `ALL` | Send a message to another player |
| `fabricmail.mail.parcel` | `ALL` | Send a message with an item to another player |
| `fabricmail.mail.revoke` | `ALL` | Revoke a mail you have sent |

### Admin Commands
| Node | Default | Description |
|------|---------|-------------|
| `fabricmail.mail.broadcast` | `GAMEMASTERS` | Broadcast a message to all, online or offline players |
| `fabricmail.mail.airdrop` | `GAMEMASTERS` | Airdrop a message and item to all, online or offline players |
| `fabricmail.mail.parcel.bypass_limit` | `GAMEMASTERS` | Bypass the `maxSentParcels` limit when sending parcels |

### Config Commands
| Node | Default | Description |
|------|---------|-------------|
| `fabricmail.config` | `GAMEMASTERS` | List all config values |
| `fabricmail.config.<name>.get` | `GAMEMASTERS` | Read a specific config value |
| `fabricmail.config.<name>.set` | `GAMEMASTERS` | Change or reset a specific config value |
| `fabricmail.config.reset_all` | `GAMEMASTERS` | Reset every config value to its default |

`<name>` is the setting's name in lower case. For example, the nodes for `maxSentParcels` are `fabricmail.config.maxsentparcels.get` and `fabricmail.config.maxsentparcels.set`.

## Building from Source
Building requires JDK 25.

```bash
./gradlew build
```

The mod jar is written to `build/libs/`.

## Support and Contributing
- Questions and discussion: [Discord](https://discord.gg/dTKPeNEg6r)
- Bug reports and suggestions: [GitHub Issues](https://github.com/borisshoes/fabric-mail/issues)
- Pull requests are welcome. Each supported Minecraft version has its own branch, so please target the one you are working on.
- Translations are welcome. The language files are in `src/main/resources/data/fabricmail/lang` and `src/main/resources/assets/fabricmail/lang`, and both need the same entries.

## Support My Work
Fabric Mail and all of my other mods are free and will stay that way. I make them in my spare time, so if they have been useful to you or your server and you would like to help keep development going, you can chip in at [borisshoes.net/donate](https://borisshoes.net/donate). Supporters get access to special skins in [Arcana Novum](https://modrinth.com/mod/arcana-novum), along with a special role and channel on the [Discord](https://discord.gg/dTKPeNEg6r). Every bit is always appreciated.

## Try My Other Mods!
All server-side Fabric mods — no client installation required.

| | Mod | Description | Links |
|:---:|---|---|---|
| <img src="https://cdn.modrinth.com/data/9J7sCd3t/e6ce366187de25be0efc7ecc736fc27f05452888_96.webp" alt="Arcana Novum" width="64"> | **Arcana Novum** | Minecraft's biggest server-side magic mod: powerful items, multiblock structures, custom bosses and deep lore. | [![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white)](https://github.com/borisshoes/ArcanaNovum) [![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?logo=modrinth&logoColor=white)](https://modrinth.com/mod/arcana-novum) [![CurseForge](https://img.shields.io/badge/CurseForge-F16436?logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/arcana-novum) |
| <img src="https://cdn.modrinth.com/data/xHHbHfVj/c6c224a3d8068cfb9b054e2a03eb9704906dd8cb_96.webp" alt="Ancestral Archetypes" width="64"> | **Ancestral Archetypes** | A highly configurable, Origins-style mod that lets players pick a mob to gain unique abilities! | [![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white)](https://github.com/borisshoes/AncestralArchetypes) [![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?logo=modrinth&logoColor=white)](https://modrinth.com/mod/ancestral-archetypes) [![CurseForge](https://img.shields.io/badge/CurseForge-F16436?logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/ancestral-archetypes) |
| <img src="https://cdn.modrinth.com/data/QfXOzeIK/b35cbf33da842f170d0aa562033aaddc2a9ab653_96.webp" alt="Ender Nexus" width="64"> | **Ender Nexus** | Highly configurable /home, /spawn, /warp, /tpa and /rtp commands all in one, and individually disablable. | [![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white)](https://github.com/borisshoes/EnderNexus/) [![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?logo=modrinth&logoColor=white)](https://modrinth.com/mod/ender-nexus) [![CurseForge](https://img.shields.io/badge/CurseForge-F16436?logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/ender-nexus-fabric-teleports) |
| <img src="https://cdn.modrinth.com/data/u40ARaBc/028062616fc2fb729afdbdc697d60f93ff61a918_96.webp" alt="Fabric Trade" width="64"> | **Fabric Trade** | Adds /trade, a secure player-to-player trading interface. | [![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white)](https://github.com/borisshoes/fabric-trade/) [![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?logo=modrinth&logoColor=white)](https://modrinth.com/mod/fabric-trade) [![CurseForge](https://img.shields.io/badge/CurseForge-F16436?logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/fabric-trade) |
| <img src="https://cdn.modrinth.com/data/WdlqG9Gd/a401b9bf08c33d85c907025d6689c657b5168508_96.webp" alt="Limited AFK" width="64"> | **Limited AFK** | AFK detection and management with configurable kick thresholds for servers. | [![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white)](https://github.com/borisshoes/LimitedAFK/) [![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?logo=modrinth&logoColor=white)](https://modrinth.com/mod/limited-afk) [![CurseForge](https://img.shields.io/badge/CurseForge-F16436?logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/limited-afk) |
| <img src="https://cdn.modrinth.com/data/klpvLefw/97afbda2e56c3f14e04d0f9e0e1fe99db6bd2f27_96.webp" alt="Links in Chat" width="64"> | **Links in Chat** | Makes URLs posted in chat clickable. | [![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white)](https://github.com/borisshoes/fabric-linksinchat/) [![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?logo=modrinth&logoColor=white)](https://modrinth.com/mod/links-in-chat) [![CurseForge](https://img.shields.io/badge/CurseForge-F16436?logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/links-in-chat) |
| <img src="https://cdn.modrinth.com/data/C5TrjUEt/dc939ba47afafd637182bff0e566a80173d94c12_96.webp" alt="PulseNet" width="64"> | **PulseNet** | A networking optimization mod that uses packet batching and other optimizations. Fully compatible with Krypton. | [![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white)](https://github.com/Pulse-MC/PulseNet) [![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?logo=modrinth&logoColor=white)](https://modrinth.com/mod/pulsenet) [![CurseForge](https://img.shields.io/badge/CurseForge-F16436?logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/pulsenet) |

## License
Fabric Mail is licensed under the [Open Software License 3.0](LICENSE).

By using this project in any form, you hereby give your "express assent" for the terms of the license of this project, and acknowledge that I, BorisShoes, have fulfilled my obligation under the license to "make a reasonable effort under the circumstances to obtain the express assent of recipients to the terms of this License."
