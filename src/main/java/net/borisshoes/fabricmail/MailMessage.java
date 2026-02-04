package net.borisshoes.fabricmail;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.callbacks.ItemReturnLoginCallback;
import net.borisshoes.borislib.callbacks.ItemReturnTimerCallback;
import net.borisshoes.borislib.datastorage.DataAccess;
import net.borisshoes.borislib.utils.CodecUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.commands.FetchProfileCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.ProfileResolver;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserNameToIdResolver;
import net.minecraft.server.players.CachedUserNameToIdResolver;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class MailMessage {
   
   private final String sender;
   private final UUID senderId;
   private final String recipient;
   private UUID recipientId;
   private final String message;
   private final UUID uuid;
   private final long timestamp;
   private CompoundTag parcel;
   
   public MailMessage(NameAndId senderProfile, String recipient, UUID recipientId, String message, CompoundTag parcel){
      this(senderProfile,recipient,recipientId,message,UUID.randomUUID(),System.currentTimeMillis(),parcel);
   }
   
   public MailMessage(NameAndId senderProfile, String recipient, @Nullable UUID recipientId, String message, UUID uuid, long timestamp, CompoundTag parcel){
      this.senderId = senderProfile.id();
      this.sender = senderProfile.name();
      this.recipient = recipient;
      this.recipientId = recipientId;
      this.message = message;
      this.uuid = uuid;
      this.timestamp = timestamp;
      this.parcel = parcel;
   }
   
   public String sender(){
      return sender;
   }
   
   public UUID senderId() { return senderId; }
   
   public String recipient(){
      return recipient;
   }
   
   public UUID recipientId(){ return recipientId; }
   
   public String message(){
      return message;
   }
   
   public UUID uuid(){
      return uuid;
   }
   
   public long timestamp(){
      return timestamp;
   }
   
   public CompoundTag parcel(){
      return parcel;
   }
   
   public boolean checkValidNoResolve(){
      return recipientId != null && recipient != null && senderId != null && sender != null;
   }
   
   public void validate(MinecraftServer server, CommandSourceStack commandSourceStack, @Nullable ServerPlayer player){
      if(checkValidNoResolve()) return;
      Util.nonCriticalIoPool().execute(() -> {
         ProfileResolver profileResolver = server.services().profileResolver();
         Optional<GameProfile> optional = profileResolver.fetchByName(recipient);
         server.execute(() -> optional.ifPresentOrElse(
               (gameProfile) -> {
                  this.recipientId = optional.get().id();
                  DataAccess.getPlayer(this.recipientId,BorisLib.PLAYER_DATA_KEY).tryResolve(server);
               },
               () -> {
                  commandSourceStack.sendFailure(Component.translatable("text.fabricmail.recipient_not_exist"));
                  MailStorage mailbox = DataAccess.getGlobal(MailStorage.KEY);
                  mailbox.removeMail(this.uuid);
                  ItemStack parcel = popParcel(server.registryAccess());
                  if(!parcel.isEmpty() && player != null){
                     BorisLib.addTickTimerCallback(new ItemReturnTimerCallback(parcel,player,0));
                  }
               }
         ));
      });
   }
   
   public MutableComponent getTimeDiff(long curtime){
      long subtract = (curtime - timestamp) / 1000;
      long daysDif = subtract / 86400;
      subtract -= daysDif * 86400;
      long hoursDif = subtract / 3600;
      subtract -= hoursDif * 3600;
      long minutesDif = subtract / 60;
      subtract -= minutesDif * 60;
      long secondsDiff = subtract;
      
      MutableComponent text = Component.literal("");
      boolean needSpace = false;
      if(daysDif > 0){
         text.append(Component.literal(daysDif+" "));
         text.append(Component.translatable("text.fabricmail.days"));
         needSpace = true;
      }
      if(hoursDif > 0){
         if(needSpace) text.append(Component.literal(" "));
         text.append(Component.literal(hoursDif+" "));
         text.append(Component.translatable("text.fabricmail.hours"));
         needSpace = true;
      }
      if(minutesDif > 0){
         if(needSpace) text.append(Component.literal(" "));
         text.append(Component.literal(minutesDif+" "));
         text.append(Component.translatable("text.fabricmail.minutes"));
         needSpace = true;
      }
      if(secondsDiff > 0){
         if(needSpace) text.append(Component.literal(" "));
         text.append(Component.literal(secondsDiff+" "));
         text.append(Component.translatable("text.fabricmail.seconds"));
      }
      return Component.translatable("text.fabricmail.time_diff",text);
   }
   
   private ItemStack peekParcel(HolderLookup.Provider registryLookup){
      return ItemStack.CODEC.parse(RegistryOps.create(NbtOps.INSTANCE,registryLookup),parcel).result().orElse(ItemStack.EMPTY);
   }
   
   public ItemStack popParcel(HolderLookup.Provider registryLookup){
      ItemStack stack = ItemStack.CODEC.parse(RegistryOps.create(NbtOps.INSTANCE,registryLookup),parcel).result().orElse(ItemStack.EMPTY);
      parcel = new CompoundTag();
      return stack;
   }
}
