# Ender Nexus

A simple server-sided mod that gives players the ability to send messages to offline players, that they will receive the next time they log on. Players can also include an item in their message to other players.

##### This mod should only be installed on a server.

### Player Commands
* ```/mail list``` Lists all of a player's mail
* ```/mail read <0-100>``` Reads the mail at the given index
* ```/mail delete <0-100/all>``` Deletes the mail at the given index from the player's mailbox
* ```/mail send <player> <message>``` Sends a message to the specified player
* ```/mail parcel <player> <message>``` Sends a message to the specified player and includes the item stack held in the sender's hand

### Admin Commands
* ```/mail broadcast <message>``` Sends the specified message to all online players
* ```/mail airdrop <message>``` Sends the specified message to all online players and includes the item stack held in the sender's hand

### LICENSE NOTICE
By using this project in any form, you hereby give your "express assent" for the terms of the license of this project, and acknowledge that I, BorisShoes, have fulfilled my obligation under the license to "make a reasonable effort under the circumstances to obtain the express assent of recipients to the terms of this License.
