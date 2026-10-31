<div align="center">

<img src="https://raw.githubusercontent.com/borisshoes/fabric-mail/main/src/main/resources/assets/fabricmail/icon.png" alt="Fabric Mail" width="140">

# Fabric Mail

**Send messages and items to any player, online or offline.<br/>All Server-Sided; Players don't need any mods.**

[![Minecraft Version](https://img.shields.io/modrinth/game-versions/Z63eULDV?style=for-the-badge&label=Minecraft&color=1F44DD)](https://modrinth.com/mod/fabric-mail/versions)
[![Server-side](https://img.shields.io/badge/Environment-Server--side-C65135?style=for-the-badge)](https://modrinth.com/mod/fabric-mail/versions)
[![Requires Fabric API](https://img.shields.io/badge/Requires-Fabric_API-DBD0B4?style=for-the-badge)](https://modrinth.com/mod/fabric-api)

[![Discord](https://img.shields.io/discord/1403978074550439966?style=for-the-badge&logo=discord&logoColor=white&label=Discord&color=5865F2)](https://discord.gg/dTKPeNEg6r)
[![Source](https://img.shields.io/badge/Source-GitHub-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/borisshoes/fabric-mail)
[![Donate](https://img.shields.io/badge/Support-Donate-EA4AAA?style=for-the-badge)](https://borisshoes.net/donate)

</div>

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
1. **Send a message.** `/mail send <player> <message>` delivers it straight away, even if the player is offline.
2. **Send an item.** Hold a stack in your main hand and run `/mail parcel <player> <message>`.
3. **Check your mailbox.** `/mail gui` opens your mailbox and `/mail list` shows the same mail in chat. Reading a mail puts its parcel in your inventory.
4. **Clean up.** Mail stays in your mailbox until you delete it. Deleting a mail also destroys any unclaimed parcel, so you can get rid of unwanted or troll parcels without them flooding your inventory. If you want the item, read the mail first.
5. **Change your mind.** `/mail outbound` lists the mail you have sent that is still in a mailbox. Click one to revoke it and get back its parcel if it has not been claimed.

## Server Setup
1. Install Fabric Loader and [Fabric API](https://modrinth.com/mod/fabric-api) on your server, then put the Fabric Mail jar in the `mods` folder. Every other library is bundled, and players do not need to install anything.
2. (Optional) For custom textures in the mailbox GUI, launch the server once, open `config/polymer/auto-host.json` and change `"enabled": false` to `true`. The server now generates a resource pack and sends it to players when they connect. Without it, the GUI's frame shows vanilla items.

## Commands
* `/mail gui` Opens the mailbox GUI.
* `/mail list` Lists your mail in chat. Click an entry to read it.
* `/mail outbound` Lists the mail you have sent that is still in the recipient's mailbox. Click an entry to revoke it.
* `/mail send <player> <message>` Sends a message to a player.
* `/mail parcel <player> <message>` Sends a message and the item stack in your main hand to a player.
* `/mail delete all` Clears your mailbox, including any unclaimed parcels.

Admins can send mail to everyone with `/mail broadcast` and `/mail airdrop`. The parcel limit and other settings can be viewed and changed in game with `/mailconfig`, and every command has a [Fabric Permissions API](https://github.com/lucko/fabric-permissions-api) node with a vanilla permission level fallback.

The full reference is on GitHub: [commands](https://github.com/borisshoes/fabric-mail#commands), [configuration](https://github.com/borisshoes/fabric-mail#configuration) and [permission nodes](https://github.com/borisshoes/fabric-mail#permission-nodes).

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

## Support
Questions go to the [Discord](https://discord.gg/dTKPeNEg6r), and bug reports and suggestions go to [GitHub Issues](https://github.com/borisshoes/fabric-mail/issues).

## Support My Work
Fabric Mail and all of my other mods are free and will stay that way. I make them in my spare time, so if they have been useful to you or your server and you would like to help keep development going, you can chip in at [borisshoes.net/donate](https://borisshoes.net/donate). Supporters get access to special skins in [Arcana Novum](https://modrinth.com/mod/arcana-novum), along with a special role and channel on the [Discord](https://discord.gg/dTKPeNEg6r). Every bit is always appreciated.

### License Notice
By using this project in any form, you hereby give your "express assent" for the terms of the license of this project, and acknowledge that I, BorisShoes, have fulfilled my obligation under the license to "make a reasonable effort under the circumstances to obtain the express assent of recipients to the terms of this License."
