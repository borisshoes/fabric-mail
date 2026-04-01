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

### LICENSE NOTICE
By using this project in any form, you hereby give your "express assent" for the terms of the license of this project, and acknowledge that I, BorisShoes, have fulfilled my obligation under the license to "make a reasonable effort under the circumstances to obtain the express assent of recipients to the terms of this License.
