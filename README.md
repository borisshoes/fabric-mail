# Fabric Mail

A simple server-sided mod that gives players the ability to send messages to offline players, that they will receive the next time they log on. Players can also include an item in their message to other players.

##### This mod should only be installed on a server.

### Player Commands
* ```/mail gui``` Opens an interactable GUI for reading mail.
* ```/mail list``` Lists all of a player's mail.
* ```/mail delete all``` Clears the player's mailbox.
* ```/mail send <player> <message>``` Sends a message to the specified player.
* ```/mail parcel <player> <message>``` Sends a message to the specified player and includes the item stack held in the sender's hand.
* ```/mail outbound``` Lists all mail you have sent that is still in the recipient's mailbox. Also lets you revoke any mails sent on accident, or retrieve mistaken Parcels.
#### The following player commands are used by the click interaction in chat, but can also be run manually if you have the Mail ID. This is usually shown by hovering over mail messages.
* ```/mail read <mailID>``` Reads the specified mail.
* ```/mail delete <mailID>``` Deletes the specified mail from the player's mailbox.
* ```/mail revoke <mailID>``` Revokes a mail that you have sent.

### Admin Commands
* ```/mail broadcast all/online/offline <message>``` Sends the specified message to online, offline, or all players
* ```/mail airdrop all/online/offline <message>``` Sends the specified message to online, offline, or all online players and includes the item stack held in the sender's hand
* ```/mailconfig maxSentParcels <0-1024>``` Sets the maximum amount of outbound parcels a player can have (useful if people try to store items in their outbound mail)
* ```/mailconfig logCommandUsage <true/false>``` Enables or disables logging of successful command executions to the server console

### Permission Nodes
Fabric Mail uses the [Fabric Permissions API](https://github.com/lucko/fabric-permissions-api) for command permissions. Each node has a fallback vanilla permission level for servers without a permissions mod.

#### Player Commands

| Node | Default | Description |
|------|---------|-------------|
| `fabricmail.mail.list` | `ALL` | List your received mail |
| `fabricmail.mail.outbound` | `ALL` | List your sent (outbound) mail |
| `fabricmail.mail.read` | `ALL` | Read a specific mail by ID |
| `fabricmail.mail.gui` | `ALL` | Open the interactive mail GUI |
| `fabricmail.mail.delete` | `ALL` | Delete a mail from your mailbox |
| `fabricmail.mail.send` | `ALL` | Send a mail message to another player |
| `fabricmail.mail.parcel` | `ALL` | Send a mail with an item to another player |
| `fabricmail.mail.revoke` | `ALL` | Revoke a sent mail from a recipient's mailbox |

#### Admin Commands
| Node | Default | Description |
|------|---------|-------------|
| `fabricmail.mail.broadcast` | `GAMEMASTERS` | Broadcast a mail message to online/offline/all players |
| `fabricmail.mail.airdrop` | `GAMEMASTERS` | Airdrop a mail with item to online/offline/all players |
| `fabricmail.mail.parcel.bypass_limit` | `GAMEMASTERS` | Bypass the maximum outbound parcel limit when sending parcels |

#### Config
Fabric Mail uses the [Fabric Permissions API](https://github.com/lucko/fabric-permissions-api) (bundled via BorisLib) for command permissions. Each node has a fallback vanilla permission level for servers without a permissions mod.

| Node | Default | Description |
|------|---------|-------------|
| `fabricmail.config` | `GAMEMASTERS` | List all config values |
| `fabricmail.config.<name>.get` | `GAMEMASTERS` | Read a specific config value |
| `fabricmail.config.<name>.set` | `GAMEMASTERS` | Change a specific config value |

### Try My Other Mods!
All server-side Fabric mods — no client installation required.

|                                                                                                                | Mod                      | Description                                                                                               | Links                                                                                                                                                                                                                                                                                                                                                                                                                                              |
|:--------------------------------------------------------------------------------------------------------------:|--------------------------|-----------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| <img src="https://cdn.modrinth.com/data/9J7sCd3t/e6ce366187de25be0efc7ecc736fc27f05452888_96.webp" width="32"> | **Arcana Novum**         | Minecraft's biggest server-only full-feature Magic Mod! Adds powerful items, multiblocks and bosses!      | [![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white)](https://github.com/borisshoes/ArcanaNovum/) [![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?logo=modrinth&logoColor=white)](https://modrinth.com/mod/arcana-novum) [![CurseForge](https://img.shields.io/badge/CurseForge-F16436?logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/arcana-novum)                        |
| <img src="https://cdn.modrinth.com/data/xHHbHfVj/c6c224a3d8068cfb9b054e2a03eb9704906dd8cb_96.webp" width="32"> | **Ancestral Archetypes** | A highly configurable, Origins-style mod that lets players pick a mob to gain unique abilities!           | [![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white)](https://github.com/borisshoes/AncestralArchetypes) [![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?logo=modrinth&logoColor=white)](https://modrinth.com/mod/ancestral-archetypes) [![CurseForge](https://img.shields.io/badge/CurseForge-F16436?logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/ancestral-archetypes) |
| <img src="https://cdn.modrinth.com/data/QfXOzeIK/b35cbf33da842f170d0aa562033aaddc2a9ab653_96.webp" width="32"> | **Ender Nexus**          | Highly configurable /home, /spawn, /warp, /tpa and /rtp commands all in one, and individually disablable. | [![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white)](https://github.com/borisshoes/EnderNexus/) [![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?logo=modrinth&logoColor=white)](https://modrinth.com/mod/ender-nexus) [![CurseForge](https://img.shields.io/badge/CurseForge-F16436?logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/ender-nexus-fabric-teleports)          |
| <img src="https://cdn.modrinth.com/data/Z63eULDV/dae01789d609498b8f1637ab31d8fe20b6108020_96.webp" width="32"> | **Fabric Mail**          | An in-game virtual mailbox system for sending packages and messages between online and offline players.   | [![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white)](https://github.com/borisshoes/fabric-mail/) [![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?logo=modrinth&logoColor=white)](https://modrinth.com/mod/fabric-mail) [![CurseForge](https://img.shields.io/badge/CurseForge-F16436?logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/fabric-mail)                          |
| <img src="https://cdn.modrinth.com/data/u40ARaBc/028062616fc2fb729afdbdc697d60f93ff61a918_96.webp" width="32"> | **Fabric Trade**         | Adds /trade, a secure player-to-player trading interface.                                                 | [![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white)](https://github.com/borisshoes/fabric-trade/) [![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?logo=modrinth&logoColor=white)](https://modrinth.com/mod/fabric-trade) [![CurseForge](https://img.shields.io/badge/CurseForge-F16436?logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/fabric-trade)                       |
| <img src="https://cdn.modrinth.com/data/WdlqG9Gd/a401b9bf08c33d85c907025d6689c657b5168508_96.webp" width="32"> | **Limited AFK**          | AFK detection and management with configurable kick thresholds for servers.                               | [![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white)](https://github.com/borisshoes/LimitedAFK/) [![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?logo=modrinth&logoColor=white)](https://modrinth.com/mod/limited-afk) [![CurseForge](https://img.shields.io/badge/CurseForge-F16436?logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/limited-afk)                           |
| <img src="https://cdn.modrinth.com/data/klpvLefw/97afbda2e56c3f14e04d0f9e0e1fe99db6bd2f27_96.webp" width="32"> | **Links in Chat**        | Makes URLs posted in chat clickable.                                                                      | [![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white)](https://github.com/borisshoes/fabric-linksinchat/) [![Modrinth](https://img.shields.io/badge/Modrinth-00AF5C?logo=modrinth&logoColor=white)](https://modrinth.com/mod/links-in-chat) [![CurseForge](https://img.shields.io/badge/CurseForge-F16436?logo=curseforge&logoColor=white)](https://www.curseforge.com/minecraft/mc-mods/links-in-chat)               |


### LICENSE NOTICE
By using this project in any form, you hereby give your "express assent" for the terms of the license of this project, and acknowledge that I, BorisShoes, have fulfilled my obligation under the license to "make a reasonable effort under the circumstances to obtain the express assent of recipients to the terms of this License.
