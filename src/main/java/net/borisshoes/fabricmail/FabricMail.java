package net.borisshoes.fabricmail;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.Lifecycle;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.config.ConfigManager;
import net.borisshoes.borislib.config.ConfigSetting;
import net.borisshoes.borislib.config.IConfigSetting;
import net.borisshoes.borislib.config.values.IntConfigValue;
import net.borisshoes.borislib.datastorage.DataAccess;
import net.borisshoes.borislib.datastorage.DefaultPlayerData;
import net.borisshoes.fabricmail.cardinalcomponents.DataFixer;
import net.borisshoes.fabricmail.cardinalcomponents.IMailComponent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.Commands;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.server.players.UserNameToIdResolver;
import net.minecraft.server.players.CachedUserNameToIdResolver;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class FabricMail implements ModInitializer {
   
   public static final Logger LOGGER = LogManager.getLogger("FabricMail");
   private static final String CONFIG_NAME = "FabricMail.properties";
   public static final String MOD_ID = "fabricmail";
   public static final Registry<IConfigSetting<?>> CONFIG_SETTINGS = new MappedRegistry<>(ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(MOD_ID,"config_settings")), Lifecycle.stable());
   
   public static ConfigManager CONFIG;
   
   public static final IConfigSetting<?> MAX_PARCELS = registerConfigSetting(new ConfigSetting<>(
         new IntConfigValue("maxSentParcels", 10, new IntConfigValue.IntLimits(0,1024))));
   
   private static IConfigSetting<?> registerConfigSetting(IConfigSetting<?> setting){
      Registry.register(CONFIG_SETTINGS, Identifier.fromNamespaceAndPath(MOD_ID,setting.getId()),setting);
      return setting;
   }
   
   @Override
   public void onInitialize(){
      LOGGER.info("Sending Fabric Mail Your Way!");
      CONFIG = new ConfigManager(MOD_ID,"Fabric Mail",CONFIG_NAME,CONFIG_SETTINGS);
      
      ServerLifecycleEvents.SERVER_STARTED.register(DataFixer::serverStarted);
      CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, registrationEnvironment) -> {
         dispatcher.register(literal("mail")
               .then(literal("gui")
                     .executes(FabricMail::gui))
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
               .then(literal("broadcast").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                     .then(literal("offline").then(argument("message",greedyString())
                           .executes(context -> FabricMail.broadcast(context,getString(context,"message"),false,true,false))))
                     .then(literal("online").then(argument("message",greedyString())
                           .executes(context -> FabricMail.broadcast(context,getString(context,"message"),true,false,false))))
                     .then(literal("all").then(argument("message",greedyString())
                           .executes(context -> FabricMail.broadcast(context,getString(context,"message"),true,true,false)))))
               .then(literal("airdrop").requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                     .then(literal("offline").then(argument("message",greedyString())
                           .executes(context -> FabricMail.broadcast(context,getString(context,"message"),false,true,true))))
                     .then(literal("online").then(argument("message",greedyString())
                           .executes(context -> FabricMail.broadcast(context,getString(context,"message"),true,false,true))))
                     .then(literal("all").then(argument("message",greedyString())
                           .executes(context -> FabricMail.broadcast(context,getString(context,"message"),true,true,true)))))
         );
         
         dispatcher.register(CONFIG.generateCommand("mailconfig",""));
      });
   }
   
   private CompletableFuture<Suggestions> getRecipientSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder){
      String start = builder.getRemaining().toLowerCase(Locale.ROOT);
      List<String> playerNames = new ArrayList<>(context.getSource().getServer().getPlayerList().getPlayers().stream().map(Player::getScoreboardName).toList());
      if(context.getSource().isPlayer()){
         playerNames.removeIf(name -> name.equals(context.getSource().getPlayer().getScoreboardName()));
      }
      playerNames.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(start)).forEach(builder::suggest);
      return builder.buildFuture();
   }
   
   private static int broadcast(CommandContext<CommandSourceStack> context, String message, boolean online, boolean offline, boolean parcel){
      CommandSourceStack source = context.getSource();
      MinecraftServer server = source.getServer();
      
      MailStorage mailbox = DataAccess.getGlobal(MailStorage.KEY);
   
      if(!source.isPlayer() && parcel){
         source.sendSystemMessage(Component.translatable("text.fabricmail.only_player_broadcast_parcel").withStyle(ChatFormatting.RED));
         return -1;
      }
   
      if(message.length() > 1024){
         source.sendSystemMessage(Component.translatable("text.fabricmail.message_too_long").withStyle(ChatFormatting.RED));
         return -1;
      }
   
      CompoundTag parcelTag = new CompoundTag();
      if(parcel){
         ServerPlayer player = source.getPlayer();
         ItemStack stack = player.getMainHandItem();
         if(!stack.isEmpty()){
            Tag element = ItemStack.CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE,context.getSource().registryAccess()),stack).getOrThrow();
            if(element instanceof CompoundTag compound){
               parcelTag = compound;
               if(!player.isCreative())
                  player.getInventory().removeItem(stack);
            }
         }
      }
   
      if(online){
         for(ServerPlayer player : server.getPlayerList().getPlayers()){
            MailMessage newMail = new MailMessage(new NameAndId(UUID.fromString("291af7c7-2114-45bb-a97a-d3b4077392e8"),"System"),player.getGameProfile().name(),player.getGameProfile().id(),message,parcelTag);
            newMail.validate(server,source,null);
            mailbox.addMail(newMail);
            player.sendSystemMessage(Component.translatable("text.fabricmail.received_mail").withStyle(s ->
                  s.withClickEvent(new ClickEvent.RunCommand("/mail read "+newMail.uuid().toString()))
                        .withHoverEvent(new HoverEvent.ShowText(Component.translatable("text.fabricmail.click_view_mail",newMail.uuid().toString())))
                        .withColor(ChatFormatting.LIGHT_PURPLE)));
         }
      }
      if(offline){
         for(DefaultPlayerData value : DataAccess.allPlayerDataFor(BorisLib.PLAYER_DATA_KEY).values()){
            if(server.getPlayerList().getPlayer(value.getPlayerID()) != null) continue;
            if(value.getUsername() == null || value.getUsername().isEmpty()) continue;
            MailMessage newMail = new MailMessage(new NameAndId(UUID.fromString("291af7c7-2114-45bb-a97a-d3b4077392e8"),"System"),value.getUsername(),value.getPlayerID(),message,parcelTag);
            newMail.validate(server,source,null);
            mailbox.addMail(newMail);
         }
      }
      
      source.sendSystemMessage(Component.translatable("text.fabricmail.message_sent").withStyle(ChatFormatting.AQUA));
      
      LOGGER.log(Level.INFO,"The Following Mail was Broadcast: "+message);
      return 1;
   }
   
   private static int gui(CommandContext<CommandSourceStack> context){
      CommandSourceStack source = context.getSource();
      MinecraftServer server = source.getServer();
      if(source.isPlayer()){
         MailGui mailGui = new MailGui(source.getPlayer());
         mailGui.buildPage();
         mailGui.open();
         return 1;
      }else{
         source.sendFailure(Component.translatable("text.fabricmail.only_players_gui"));
      }
      return -1;
   }
   
   private static int list(CommandContext<CommandSourceStack> context){
      CommandSourceStack source = context.getSource();
      MinecraftServer server = source.getServer();
      if(source.isPlayer()){
         MailStorage mailbox = DataAccess.getGlobal(MailStorage.KEY);
         ServerPlayer player = source.getPlayer();
         List<MailMessage> mails = mailbox.getMailsFor(player);
         
         player.sendSystemMessage(Component.translatable("text.fabricmail.you_have_messages_click",
               Component.literal(""+mails.size()).withStyle(ChatFormatting.LIGHT_PURPLE),
               Component.translatable("text.fabricmail.click_to_read_hint").withStyle(ChatFormatting.LIGHT_PURPLE)).withStyle(ChatFormatting.AQUA));
         for(int i = 0; i < mails.size(); i++){
            MailMessage mail = mails.get(i);
            MutableComponent fromText = Component.literal("");
            DefaultPlayerData data = DataAccess.getPlayer(mail.senderId(),BorisLib.PLAYER_DATA_KEY);
            fromText.append(data.getFaceTextComponent().copy().withStyle(ChatFormatting.WHITE))
                  .append(Component.literal(" ")).append(Component.literal(mail.sender()).withStyle(ChatFormatting.DARK_AQUA));
            
            MutableComponent mailText = Component.translatable("text.fabricmail.mail_list_entry",
                  Component.literal(""+(i+1)).withStyle(ChatFormatting.AQUA),
                  fromText,
                  mail.getTimeDiff(System.currentTimeMillis()).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.BLUE);
            
            if(!mail.parcel().isEmpty()){
               mailText.append(Component.translatable("text.fabricmail.contains_parcel").withStyle(ChatFormatting.GREEN));
            }
            
            int finalI = i;
            player.sendSystemMessage(mailText.withStyle(s ->
                  s.withClickEvent(new ClickEvent.RunCommand("/mail read "+mail.uuid().toString()))
                        .withHoverEvent(new HoverEvent.ShowText(Component.translatable("text.fabricmail.click_read_mail",(finalI+1))))));
         }
         player.sendSystemMessage(Component.literal(""));
         return 1;
      }else{
         source.sendFailure(Component.translatable("text.fabricmail.only_players_receive"));
      }
      return -1;
   }
   
   private static int listOutbound(CommandContext<CommandSourceStack> context){
      CommandSourceStack source = context.getSource();
      MinecraftServer server = source.getServer();
      if(source.isPlayer()){
         MailStorage mailbox = DataAccess.getGlobal(MailStorage.KEY);
         ServerPlayer player = source.getPlayer();
         List<MailMessage> mails = mailbox.getMailsFrom(player);
         
         player.sendSystemMessage(Component.translatable("text.fabricmail.you_have_outbound_messages",
               Component.literal(""+mails.size()).withStyle(ChatFormatting.LIGHT_PURPLE),
               Component.translatable("text.fabricmail.click_unsend_hint").withStyle(ChatFormatting.LIGHT_PURPLE)).withStyle(ChatFormatting.AQUA));
         for(int i = 0; i < mails.size(); i++){
            MailMessage mail = mails.get(i);
            MutableComponent toText = Component.literal("");
            DefaultPlayerData data = DataAccess.getPlayer(mail.recipientId(),BorisLib.PLAYER_DATA_KEY);
            toText.append(data.getFaceTextComponent().copy().withStyle(ChatFormatting.WHITE))
                  .append(Component.literal(" ")).append(Component.literal(mail.recipient()).withStyle(ChatFormatting.DARK_AQUA));
            
            MutableComponent mailText = Component.translatable("text.fabricmail.mail_outbound_entry",
                  Component.literal(""+(i+1)).withStyle(ChatFormatting.AQUA),
                  toText,
                  mail.getTimeDiff(System.currentTimeMillis()).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.BLUE);
            
            if(!mail.parcel().isEmpty()){
               mailText.append(Component.translatable("text.fabricmail.contains_parcel").withStyle(ChatFormatting.GREEN));
            }
            player.sendSystemMessage(mailText.withStyle(s ->
                  s.withClickEvent(new ClickEvent.RunCommand("/mail revoke "+mail.uuid().toString()))
                        .withHoverEvent(new HoverEvent.ShowText(Component.translatable("text.fabricmail.click_revoke_mail")))));
         }
         player.sendSystemMessage(Component.literal(""));
         return 1;
      }else{
         source.sendFailure(Component.translatable("text.fabricmail.only_players_send"));
      }
      return -1;
   }
   
   private static int send(CommandContext<CommandSourceStack> context, String to, String message, boolean parcel){
      CommandSourceStack source = context.getSource();
      MinecraftServer server = source.getServer();
      if(source.isPlayer()){
         MailStorage mailbox = DataAccess.getGlobal(MailStorage.KEY);
         ServerPlayer player = source.getPlayer();
         
         if(message.length() > 1024){
            player.sendSystemMessage(Component.translatable("text.fabricmail.message_too_long").withStyle(ChatFormatting.RED));
            return -1;
         }
   
         CompoundTag parcelTag = new CompoundTag();
         ItemStack stack = ItemStack.EMPTY;
         if(parcel){
            stack = player.getMainHandItem();
            if(!stack.isEmpty()){
               Tag element = ItemStack.CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE,context.getSource().registryAccess()),stack).getOrThrow();
               if(element instanceof CompoundTag compound){
                  parcelTag = compound;
               }
            }
            
            List<MailMessage> mails = mailbox.getMailsFrom(player);
            int maxOutbound = CONFIG.getInt(MAX_PARCELS);
            int outbound = 0;
            for(MailMessage mail : mails){
               if(mail.parcel() != null && !mail.parcel().isEmpty()){
                  outbound++;
               }
            }

            if (!Commands.LEVEL_ADMINS.check(context.getSource().permissions())) {
               if (maxOutbound == 0) {
                  source.sendFailure(Component.translatable("text.fabricmail.parcel_disabled"));
                  return -1;
               }else if(outbound >= maxOutbound){
                  source.sendFailure(Component.translatable("text.fabricmail.max_outbound_reached"));
                  return -1;
               }
            }
         }
         
         ServerPlayer onlineTo = server.getPlayerList().getPlayerByName(to);
         MailMessage newMail = new MailMessage(new NameAndId(player.getGameProfile()),to,onlineTo == null ? null : onlineTo.getUUID(),message,parcelTag);
         if(newMail.sender().equals(newMail.recipient())){
            source.sendFailure(Component.translatable("text.fabricmail.cannot_mail_self"));
            return -1;
         }else{
            if(!stack.isEmpty() && !player.isCreative())
               player.getInventory().removeItem(stack);
            
            newMail.validate(server,source,player);
            mailbox.addMail(newMail);
            if(onlineTo != null){
               onlineTo.sendSystemMessage(Component.translatable("text.fabricmail.received_mail").withStyle(s ->
                     s.withClickEvent(new ClickEvent.RunCommand("/mail read "+newMail.uuid().toString()))
                           .withHoverEvent(new HoverEvent.ShowText(Component.translatable("text.fabricmail.click_view_mail",newMail.uuid().toString())))
                           .withColor(ChatFormatting.LIGHT_PURPLE)));
            }
            
            player.sendSystemMessage(Component.translatable("text.fabricmail.message_sent").withStyle(s ->
                  s.withClickEvent(new ClickEvent.RunCommand("/mail revoke "+newMail.uuid().toString()))
                        .withHoverEvent(new HoverEvent.ShowText(Component.translatable("text.fabricmail.click_revoke_id",newMail.uuid().toString())))
                        .withColor(ChatFormatting.AQUA)));
            
            LOGGER.log(Level.INFO,player.getScoreboardName()+" sent mail to "+to+": "+message);
         }
         
         return -1;
      }else{
         source.sendFailure(Component.translatable("text.fabricmail.only_players_send"));
      }
      return -1;
   }
   
   private static int delete(CommandContext<CommandSourceStack> context, String mailIDStr){
      CommandSourceStack source = context.getSource();
      MinecraftServer server = source.getServer();
      if(source.isPlayer()){
         MailStorage mailbox = DataAccess.getGlobal(MailStorage.KEY);
         ServerPlayer player = source.getPlayer();
         List<MailMessage> mails = mailbox.getMailsFor(player);
      
         if(mailIDStr.equals("all")){
            mailbox.clearMailFor(player);
            player.sendSystemMessage(Component.translatable("text.fabricmail.all_mail_deleted").withStyle(ChatFormatting.LIGHT_PURPLE));
            return 1;
         }
         
         UUID mailID = getIdOrNull(mailIDStr);
         if(mailID == null){
            player.sendSystemMessage(Component.translatable("text.fabricmail.invalid_mail_id").withStyle(ChatFormatting.RED));
            return -1;
         }
         
         MailMessage mail = mailbox.getMail(mailID.toString());
         if(mail != null && mail.recipientId().equals(player.getUUID())){
            mailbox.removeMail(mailID.toString());
            player.sendSystemMessage(Component.translatable("text.fabricmail.mail_deleted").withStyle(ChatFormatting.LIGHT_PURPLE));
            return 1;
         }else{
            player.sendSystemMessage(Component.translatable("text.fabricmail.invalid_mail_id").withStyle(ChatFormatting.RED));
            return -1;
         }
      }else{
         source.sendFailure(Component.translatable("text.fabricmail.only_players_delete"));
      }
      return -1;
   }
   
   private static int revoke(CommandContext<CommandSourceStack> context, String mailIDStr){
      CommandSourceStack source = context.getSource();
      MinecraftServer server = source.getServer();
      if(source.isPlayer()){
         MailStorage mailbox = DataAccess.getGlobal(MailStorage.KEY);
         ServerPlayer player = source.getPlayer();
         List<MailMessage> mails = mailbox.getMailsFrom(player);
         
         UUID mailID = getIdOrNull(mailIDStr);
         if(mailID == null){
            player.sendSystemMessage(Component.translatable("text.fabricmail.invalid_mail_id").withStyle(ChatFormatting.RED));
            return -1;
         }
         
         for(MailMessage mail : mails){
            if(mail.uuid().equals(mailID) && mail.senderId().equals(player.getUUID())){
               MutableComponent fromText = Component.literal("");
               DefaultPlayerData data = DataAccess.getPlayer(mail.recipientId(),BorisLib.PLAYER_DATA_KEY);
               fromText.append(data.getFaceTextComponent().copy().withStyle(ChatFormatting.WHITE))
                     .append(Component.literal(" ")).append(Component.literal(mail.recipient()).withStyle(ChatFormatting.AQUA));
               
               if(!mail.parcel().isEmpty()){
                  player.sendSystemMessage(Component.translatable("text.fabricmail.revoked_mail_to_parcel", fromText).withStyle(ChatFormatting.LIGHT_PURPLE));
               }else{
                  player.sendSystemMessage(Component.translatable("text.fabricmail.revoked_mail_to", fromText).withStyle(ChatFormatting.LIGHT_PURPLE));
               }
               
               givePlayerStack(player,mail.popParcel(context.getSource().registryAccess()));
               mailbox.removeMail(mail.uuid().toString());
               return 1;
            }
         }
         
         player.sendSystemMessage(Component.translatable("text.fabricmail.invalid_mail_id").withStyle(ChatFormatting.RED));
         return -1;
      }else{
         source.sendFailure(Component.translatable("text.fabricmail.only_players_receive"));
      }
      return -1;
   }
   
   private static int read(CommandContext<CommandSourceStack> context, String mailIDStr){
      CommandSourceStack source = context.getSource();
      MinecraftServer server = source.getServer();
      if(source.isPlayer()){
         MailStorage mailbox = DataAccess.getGlobal(MailStorage.KEY);
         ServerPlayer player = source.getPlayer();
         List<MailMessage> mails = mailbox.getMailsFor(player);
         
         UUID mailID = getIdOrNull(mailIDStr);
         if(mailID == null){
            player.sendSystemMessage(Component.translatable("text.fabricmail.invalid_mail_id").withStyle(ChatFormatting.RED));
            return -1;
         }
         
         MailMessage mail = mailbox.getMail(mailID.toString());
         if(mail != null && mail.recipientId().equals(player.getUUID())){
            MutableComponent fromText = Component.literal("");
            DefaultPlayerData data = DataAccess.getPlayer(mail.senderId(),BorisLib.PLAYER_DATA_KEY);
            fromText.append(data.getFaceTextComponent().copy().withStyle(ChatFormatting.WHITE))
                  .append(Component.literal(" ")).append(Component.literal(mail.sender()).withStyle(ChatFormatting.DARK_AQUA));
            
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.translatable("text.fabricmail.mail_from_header",
                  fromText, mail.getTimeDiff(System.currentTimeMillis()).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.BLUE));
            player.sendSystemMessage(Component.literal(mail.message()).withStyle(ChatFormatting.AQUA));
            player.sendSystemMessage(Component.literal(""));
            if(!mail.parcel().isEmpty()){
               player.sendSystemMessage(Component.translatable("text.fabricmail.parcel_added_inventory").withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC));
            }
            player.sendSystemMessage(Component.translatable("text.fabricmail.click_remove_message").withStyle(s ->
                  s.withClickEvent(new ClickEvent.RunCommand("/mail delete "+mailID))
                        .withHoverEvent(new HoverEvent.ShowText(Component.translatable("text.fabricmail.click_delete_mail")))
                        .withColor(ChatFormatting.LIGHT_PURPLE)));
            
            givePlayerStack(player,mail.popParcel(context.getSource().registryAccess()));
            
            return 1;
         }else{
            player.sendSystemMessage(Component.translatable("text.fabricmail.invalid_mail_id").withStyle(ChatFormatting.RED));
            return -1;
         }
      }else{
         source.sendFailure(Component.translatable("text.fabricmail.only_players_receive"));
      }
      return -1;
   }
   
   public static void informMail(ServerPlayer player){
      MinecraftServer server = player.level().getServer();
      MailStorage mailbox = DataAccess.getGlobal(MailStorage.KEY);
      List<MailMessage> mails = mailbox.getMailsFor(player);
      if(mails.isEmpty()) return;
   
      player.sendSystemMessage(Component.literal(""));
      player.sendSystemMessage(Component.translatable("text.fabricmail.you_have_messages",
            Component.literal(""+mails.size()).withStyle(ChatFormatting.LIGHT_PURPLE)).withStyle(ChatFormatting.AQUA));
      player.sendSystemMessage(Component.translatable("text.fabricmail.click_view_hint").withStyle(s ->
            s.withClickEvent(new ClickEvent.SuggestCommand("/mail list"))
                  .withHoverEvent(new HoverEvent.ShowText(Component.translatable("text.fabricmail.click_view_mail_generic")))
                  .withColor(ChatFormatting.LIGHT_PURPLE)));
      player.sendSystemMessage(Component.literal(""));
   }
   
   
   public static void givePlayerStack(ServerPlayer player, ItemStack stack){
      ItemEntity itemEntity;
      boolean bl = player.getInventory().add(stack);
      if (!bl || !stack.isEmpty()) {
         itemEntity = player.drop(stack, false);
         if (itemEntity == null) return;
         itemEntity.setNoPickUpDelay();
         itemEntity.setTarget(player.getUUID());
         return;
      }
      stack.setCount(1);
      itemEntity = player.drop(stack, false);
      if (itemEntity != null) {
         itemEntity.makeFakeItem();
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
