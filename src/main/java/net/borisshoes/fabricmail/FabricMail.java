package net.borisshoes.fabricmail;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.DataResult;
import net.borisshoes.fabricmail.cardinalcomponents.IMailComponent;
import net.borisshoes.fabricmail.cardinalcomponents.MailMessage;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.borisshoes.fabricmail.cardinalcomponents.WorldDataComponentInitializer.MAILS;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class FabricMail implements ModInitializer {
   
   private static final Logger logger = LogManager.getLogger("FabricMail");
   
   @Override
   public void onInitialize(){
   
      logger.info("Sending Fabric Mail Your Way!");
   
      CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, registrationEnvironment) -> {
         dispatcher.register(literal("mail")
               .then(literal("list")
                     .executes(FabricMail::list))
               .then(literal("outbound")
                     .executes(FabricMail::listOutbound))
               .then(literal("revoke")
                     .then(argument("mail_id",string())
                           .executes(context -> FabricMail.revoke(context,getString(context,"mail_id")))))
               .then(literal("read")
                     .then(argument("mail_id",string())
                           .executes(context -> FabricMail.read(context,getString(context,"mail_id")))))
               .then(literal("delete")
                     .then(argument("mail_id",string())
                           .executes(context -> FabricMail.delete(context,getString(context,"mail_id"))))
                     .then(literal("all")
                           .executes(context -> FabricMail.delete(context,"all"))))
               .then(literal("send")
                     .then(argument("player",word()).suggests(this::getRecipientSuggestions)
                           .then(argument("message",greedyString())
                                 .executes(context -> FabricMail.send(context,getString(context,"player"),getString(context,"message"),false)))))
               .then(literal("parcel")
                     .then(argument("player",word()).suggests(this::getRecipientSuggestions)
                           .then(argument("message",greedyString())
                                 .executes(context -> FabricMail.send(context,getString(context,"player"),getString(context,"message"),true)))))
               .then(literal("broadcast").requires(source -> source.hasPermissionLevel(2))
                     .then(argument("message",greedyString())
                           .executes(context -> FabricMail.broadcast(context,getString(context,"message"),false))))
               .then(literal("airdrop").requires(source -> source.hasPermissionLevel(2))
                     .then(argument("message",greedyString())
                           .executes(context -> FabricMail.broadcast(context,getString(context,"message"),true))))
         );
      });
   }
   
   private CompletableFuture<Suggestions> getRecipientSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder){
      String start = builder.getRemaining().toLowerCase();
      List<String> playerNames = new ArrayList<>(context.getSource().getServer().getPlayerManager().getPlayerList().stream().map(PlayerEntity::getNameForScoreboard).toList());
      if(context.getSource().isExecutedByPlayer()){
         playerNames.removeIf(name -> name.equals(context.getSource().getPlayer().getNameForScoreboard()));
      }
      playerNames.stream().filter(s -> s.toLowerCase().startsWith(start)).forEach(builder::suggest);
      return builder.buildFuture();
   }
   
   private static int broadcast(CommandContext<ServerCommandSource> context, String message, boolean parcel){
      ServerCommandSource source = context.getSource();
      MinecraftServer server = source.getServer();
   
      IMailComponent mailbox = MAILS.get(server.getOverworld());
   
      if(!source.isExecutedByPlayer() && parcel){
         source.sendMessage(Text.literal("Only a player can broadcast with a parcel").formatted(Formatting.RED));
         return -1;
      }
   
      if(message.length() > 1024){
         source.sendMessage(Text.literal("Message Length Exceeds 1024 Characters").formatted(Formatting.RED));
         return -1;
      }
   
      NbtCompound parcelTag = new NbtCompound();
      if(parcel){
         ServerPlayerEntity player = source.getPlayer();
         ItemStack stack = player.getMainHandStack();
         if(!stack.isEmpty()){
            NbtElement element = stack.encode(context.getSource().getRegistryManager());
            if(element instanceof NbtCompound compound){
               parcelTag = compound;
               if(!player.isCreative())
                  player.getInventory().removeOne(stack);
            }
         }
      }
   
      for(ServerPlayerEntity player : server.getPlayerManager().getPlayerList()){
         MailMessage newMail = new MailMessage(new GameProfile(null,"System"),player.getGameProfile().getName(),player.getGameProfile().getId(),message, UUID.randomUUID(),System.currentTimeMillis(),parcelTag);
         newMail.checkValid(server);
         mailbox.addMail(newMail);
         List<MailMessage> mails = mailbox.getMailsFor(player);
         for(int i = 0; i < mails.size(); i++){
            MailMessage mail = mails.get(i);
            if(mail.uuid().equals(newMail.uuid())){
               break;
            }
         }
         player.sendMessage(Text.literal("You Just Received Mail!").styled(s ->
               s.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mail read "+newMail.uuid().toString()))
                     .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to View Your Mail! (Mail ID: "+newMail.uuid().toString()+")")))
                     .withColor(Formatting.LIGHT_PURPLE)));
      }
      
      source.sendMessage(Text.literal("Message Sent!").formatted(Formatting.AQUA));
   
      logger.log(Level.INFO,"The Following Mail was Broadcast: "+message);
      return 1;
   }
   
   private static int list(CommandContext<ServerCommandSource> context){
      ServerCommandSource source = context.getSource();
      MinecraftServer server = source.getServer();
      if(source.isExecutedByPlayer() && server != null){
         IMailComponent mailbox = MAILS.get(server.getOverworld());
         ServerPlayerEntity player = source.getPlayer();
         List<MailMessage> mails = mailbox.getMailsFor(player);
   
         player.sendMessage(Text.literal(""));
         player.sendMessage(Text.literal("")
               .append(Text.literal("You have ").formatted(Formatting.AQUA))
               .append(Text.literal(""+mails.size()).formatted(Formatting.LIGHT_PURPLE))
               .append(Text.literal(" messages (").formatted(Formatting.AQUA))
               .append(Text.literal("Click to read").formatted(Formatting.LIGHT_PURPLE))
               .append(Text.literal(")").formatted(Formatting.AQUA)));
         for(int i = 0; i < mails.size(); i++){
            MailMessage mail = mails.get(i);
            MutableText mailText = Text.literal("")
                  .append(Text.literal("("+(i+1)+") ").formatted(Formatting.AQUA))
                  .append(Text.literal("From: ").formatted(Formatting.BLUE))
                  .append(Text.literal(mail.sender()+" ").formatted(Formatting.DARK_AQUA))
                  .append(Text.literal(mail.getTimeDiff(System.currentTimeMillis())).formatted(Formatting.GOLD));
            
            if(!mail.parcel().isEmpty()){
               mailText.append(Text.literal(" {Contains Parcel}").formatted(Formatting.GREEN));
            }
            
            int finalI = i;
            player.sendMessage(mailText.styled(s ->
                  s.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mail read "+mail.uuid().toString()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to Read Mail #"+(finalI+1))))));
         }
         player.sendMessage(Text.literal(""));
         return 1;
      }else{
         source.sendError(Text.literal("Only players can receive mail"));
      }
      return -1;
   }
   
   private static int listOutbound(CommandContext<ServerCommandSource> context){
      ServerCommandSource source = context.getSource();
      MinecraftServer server = source.getServer();
      if(source.isExecutedByPlayer() && server != null){
         IMailComponent mailbox = MAILS.get(server.getOverworld());
         ServerPlayerEntity player = source.getPlayer();
         List<MailMessage> mails = mailbox.getMailsFrom(player);
         
         player.sendMessage(Text.literal(""));
         player.sendMessage(Text.literal("")
               .append(Text.literal("You have ").formatted(Formatting.AQUA))
               .append(Text.literal(""+mails.size()).formatted(Formatting.LIGHT_PURPLE))
               .append(Text.literal(" outbound messages ").formatted(Formatting.AQUA))
               .append(Text.literal("(Click to Un-send)").formatted(Formatting.LIGHT_PURPLE))
               .append(Text.literal(":").formatted(Formatting.AQUA)));
         for(int i = 0; i < mails.size(); i++){
            MailMessage mail = mails.get(i);
            MutableText mailText = Text.literal("")
                  .append(Text.literal("("+(i+1)+") ").formatted(Formatting.AQUA))
                  .append(Text.literal("To: ").formatted(Formatting.BLUE))
                  .append(Text.literal(mail.recipient()+" ").formatted(Formatting.DARK_AQUA))
                  .append(Text.literal(mail.getTimeDiff(System.currentTimeMillis())).formatted(Formatting.GOLD));
            
            if(!mail.parcel().isEmpty()){
               mailText.append(Text.literal(" {Contains Parcel}").formatted(Formatting.GREEN));
            }
            
            player.sendMessage(mailText.styled(s ->
                  s.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mail revoke "+mail.uuid().toString()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to Revoke this Mail")))));
         }
         player.sendMessage(Text.literal(""));
         return 1;
      }else{
         source.sendError(Text.literal("Only players can send mail"));
      }
      return -1;
   }
   
   private static int send(CommandContext<ServerCommandSource> context, String to, String message, boolean parcel){
      ServerCommandSource source = context.getSource();
      MinecraftServer server = source.getServer();
      if(source.isExecutedByPlayer() && server != null){
         IMailComponent mailbox = MAILS.get(server.getOverworld());
         ServerPlayerEntity player = source.getPlayer();
         
         if(message.length() > 1024){
            player.sendMessage(Text.literal("Message Length Exceeds 1024 Characters").formatted(Formatting.RED));
            return -1;
         }
   
         NbtCompound parcelTag = new NbtCompound();
         ItemStack stack = ItemStack.EMPTY;
         if(parcel){
            stack = player.getMainHandStack();
            if(!stack.isEmpty()){
               NbtElement element = stack.encode(context.getSource().getRegistryManager());
               if(element instanceof NbtCompound compound){
                  parcelTag = compound;
               }
            }
         }
         
         ServerPlayerEntity onlineTo = server.getPlayerManager().getPlayer(to);
         
         MailMessage newMail = new MailMessage(player.getGameProfile(),to,onlineTo == null ? null : onlineTo.getUuid(),message, UUID.randomUUID(),System.currentTimeMillis(),parcelTag);
         if(newMail.sender().equals(newMail.recipient())){
            source.sendError(Text.literal("You cannot send a mail to yourself!"));
            return -1;
         }else if(newMail.checkValid(server)){
            if(!stack.isEmpty() && !player.isCreative())
               player.getInventory().removeOne(stack);
            
            mailbox.addMail(newMail);
            ServerPlayerEntity recipient = player.getServer().getPlayerManager().getPlayer(to);
            if(recipient != null){
               List<MailMessage> mails = mailbox.getMailsFor(recipient);
               for(int i = 0; i < mails.size(); i++){
                  MailMessage mail = mails.get(i);
                  if(mail.uuid().equals(newMail.uuid())){
                     break;
                  }
               }
               recipient.sendMessage(Text.literal("You Just Received Mail!").styled(s ->
                     s.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mail read "+newMail.uuid().toString()))
                           .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to View Your Mail! (Mail ID: "+newMail.uuid().toString()+")")))
                           .withColor(Formatting.LIGHT_PURPLE)));
            }
            
            player.sendMessage(Text.literal("Message Sent!").styled(s ->
                  s.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mail revoke "+newMail.uuid().toString()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to Revoke (Mail ID: "+newMail.uuid().toString()+")")))
                        .withColor(Formatting.AQUA)));
            
            logger.log(Level.INFO,player.getNameForScoreboard()+" sent mail to "+to+": "+message);
         }else{
            source.sendError(Text.literal("Recipient Does Not Exist"));
         }
         
         return -1;
      }else{
         source.sendError(Text.literal("Only players can send mail"));
      }
      return -1;
   }
   
   private static int delete(CommandContext<ServerCommandSource> context, String mailIDStr){
      ServerCommandSource source = context.getSource();
      MinecraftServer server = source.getServer();
      if(source.isExecutedByPlayer() && server != null){
         IMailComponent mailbox = MAILS.get(server.getOverworld());
         ServerPlayerEntity player = source.getPlayer();
         List<MailMessage> mails = mailbox.getMailsFor(player);
      
         if(mailIDStr.equals("all")){
            mailbox.clearMailFor(player);
            player.sendMessage(Text.literal("All Mail Deleted").formatted(Formatting.LIGHT_PURPLE));
            return 1;
         }
         
         UUID mailID = getIdOrNull(mailIDStr);
         if(mailID == null){
            player.sendMessage(Text.literal("Invalid Mail ID").formatted(Formatting.RED));
            return -1;
         }
         
         MailMessage mail = mailbox.getMail(mailID.toString());
         if(mail != null && mail.recipientId().equals(player.getUuid())){
            mailbox.removeMail(mailID.toString());
            player.sendMessage(Text.literal("Mail Deleted").formatted(Formatting.LIGHT_PURPLE));
            return 1;
         }else{
            player.sendMessage(Text.literal("Invalid Mail ID").formatted(Formatting.RED));
            return -1;
         }
      }else{
         source.sendError(Text.literal("Only players can delete mail"));
      }
      return -1;
   }
   
   private static int revoke(CommandContext<ServerCommandSource> context, String mailIDStr){
      ServerCommandSource source = context.getSource();
      MinecraftServer server = source.getServer();
      if(source.isExecutedByPlayer() && server != null){
         IMailComponent mailbox = MAILS.get(server.getOverworld());
         ServerPlayerEntity player = source.getPlayer();
         List<MailMessage> mails = mailbox.getMailsFrom(player);
         
         UUID mailID = getIdOrNull(mailIDStr);
         if(mailID == null){
            player.sendMessage(Text.literal("Invalid Mail ID").formatted(Formatting.RED));
            return -1;
         }
         
         for(MailMessage mail : mails){
            if(mail.uuid().equals(mailID) && mail.senderId().equals(player.getUuid())){
               if(!mail.parcel().isEmpty()){
                  player.sendMessage(Text.literal("")
                        .append(Text.literal("Revoked Mail to ").formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(mail.recipient()).formatted(Formatting.AQUA))
                        .append(Text.literal(" and returned the Parcel to your Inventory.").formatted(Formatting.LIGHT_PURPLE)));
               }else{
                  player.sendMessage(Text.literal("")
                        .append(Text.literal("Revoked Mail to ").formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(mail.recipient()).formatted(Formatting.AQUA))
                        .append(Text.literal(".").formatted(Formatting.LIGHT_PURPLE)));
               }
               
               givePlayerStack(player,mail.popParcel(context.getSource().getRegistryManager()));
               mailbox.removeMail(mail.uuid().toString());
               return 1;
            }
         }
         
         player.sendMessage(Text.literal("Invalid Mail ID").formatted(Formatting.RED));
         return -1;
      }else{
         source.sendError(Text.literal("Only players can receive mail"));
      }
      return -1;
   }
   
   private static int read(CommandContext<ServerCommandSource> context, String mailIDStr){
      ServerCommandSource source = context.getSource();
      MinecraftServer server = source.getServer();
      if(source.isExecutedByPlayer() && server != null){
         IMailComponent mailbox = MAILS.get(server.getOverworld());
         ServerPlayerEntity player = source.getPlayer();
         List<MailMessage> mails = mailbox.getMailsFor(player);
         
         UUID mailID = getIdOrNull(mailIDStr);
         if(mailID == null){
            player.sendMessage(Text.literal("Invalid Mail ID").formatted(Formatting.RED));
            return -1;
         }
         
         MailMessage mail = mailbox.getMail(mailID.toString());
         if(mail != null && mail.recipientId().equals(player.getUuid())){
            player.sendMessage(Text.literal(""));
            player.sendMessage(Text.literal("")
                  .append(Text.literal("From: ").formatted(Formatting.BLUE))
                  .append(Text.literal(mail.sender()+" ").formatted(Formatting.DARK_AQUA))
                  .append(Text.literal(mail.getTimeDiff(System.currentTimeMillis())).formatted(Formatting.GOLD)));
            player.sendMessage(Text.literal(mail.message()).formatted(Formatting.AQUA));
            player.sendMessage(Text.literal(""));
            if(!mail.parcel().isEmpty()){
               player.sendMessage(Text.literal("This Mail contained a Parcel. It has been added to your Inventory.").formatted(Formatting.GREEN,Formatting.ITALIC));
            }
            player.sendMessage(Text.literal("[Click to remove message from your mailbox]").styled(s ->
                  s.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mail delete "+mailID))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to Delete Mail")))
                        .withColor(Formatting.LIGHT_PURPLE)));
            
            givePlayerStack(player,mail.popParcel(context.getSource().getRegistryManager()));
            
            return 1;
         }else{
            player.sendMessage(Text.literal("Invalid Mail ID").formatted(Formatting.RED));
            return -1;
         }
      }else{
         source.sendError(Text.literal("Only players can receive mail"));
      }
      return -1;
   }
   
   public static void informMail(ServerPlayerEntity player){
      MinecraftServer server = player.getServer();
      IMailComponent mailbox = MAILS.get(server.getOverworld());
      List<MailMessage> mails = mailbox.getMailsFor(player);
      if(mails.isEmpty()) return;
   
      player.sendMessage(Text.literal(""));
      player.sendMessage(Text.literal("")
            .append(Text.literal("You have ").formatted(Formatting.AQUA))
            .append(Text.literal(""+mails.size()).formatted(Formatting.LIGHT_PURPLE))
            .append(Text.literal(" messages").formatted(Formatting.AQUA)));
      player.sendMessage(Text.literal("[Click to here to view]").styled(s ->
            s.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mail list"))
                  .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to View Your Mail!")))
                  .withColor(Formatting.LIGHT_PURPLE)));
      player.sendMessage(Text.literal(""));
   }
   
   
   public static void givePlayerStack(ServerPlayerEntity player, ItemStack stack){
      ItemEntity itemEntity;
      boolean bl = player.getInventory().insertStack(stack);
      if (!bl || !stack.isEmpty()) {
         itemEntity = player.dropItem(stack, false);
         if (itemEntity == null) return;
         itemEntity.resetPickupDelay();
         itemEntity.setOwner(player.getUuid());
         return;
      }
      stack.setCount(1);
      itemEntity = player.dropItem(stack, false);
      if (itemEntity != null) {
         itemEntity.setDespawnImmediately();
      }
   }
   
   
   public static UUID getIdOrNull(String id){
      try{
         return UUID.fromString(id);
      }catch(Exception e){
         return null;
      }
   }
}
