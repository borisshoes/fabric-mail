# Fabric Mail

A simple server-sided mod that gives players the ability to send messages to offline players, that they will receive the next time they log on. Players can also include an item in their message to other players.

##### This mod should only be installed on a server.

### Player Commands
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
* ```/mail broadcast <message>``` Sends the specified message to all online players
* ```/mail airdrop <message>``` Sends the specified message to all online players and includes the item stack held in the sender's hand

### LICENSE NOTICE
By using this project in any form, you hereby give your "express assent" for the terms of the license of this project, and acknowledge that I, BorisShoes, have fulfilled my obligation under the license to "make a reasonable effort under the circumstances to obtain the express assent of recipients to the terms of this License.
