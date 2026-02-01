package net.borisshoes.fabricmail;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.borisshoes.borislib.utils.CodecUtils;
import net.borisshoes.borislib.utils.TextUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
   private UUID senderId;
   private final String recipient;
   private UUID recipientId;
   private final String message;
   private final UUID uuid;
   private final long timestamp;
   private CompoundTag parcel;
   
   public static final Codec<MailMessage> CODEC = RecordCodecBuilder.create(i -> i.group(
         Codec.STRING.fieldOf("sender").forGetter(MailMessage::sender),
         CodecUtils.UUID_CODEC.fieldOf("sender_id").forGetter(MailMessage::senderId),
         Codec.STRING.fieldOf("recipient").forGetter(MailMessage::recipient),
         CodecUtils.UUID_CODEC.optionalFieldOf("recipient_id").forGetter(m -> Optional.ofNullable(m.recipientId())),
         Codec.STRING.fieldOf("message").forGetter(MailMessage::message),
         CodecUtils.UUID_CODEC.fieldOf("uuid").forGetter(MailMessage::uuid),
         Codec.LONG.fieldOf("timestamp").forGetter(MailMessage::timestamp),
         CompoundTag.CODEC.optionalFieldOf("parcel", new CompoundTag()).forGetter(MailMessage::parcel)
   ).apply(i, (senderName, senderId, recipientName, recipientIdOpt, message, uuid, timestamp, parcel) ->
         new MailMessage(new NameAndId(senderId, senderName), recipientName, recipientIdOpt.orElse(null), message, uuid, timestamp, parcel)));
   
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
   
   public boolean checkValid(MinecraftServer server){
      NameAndId profile = findRecipient(server);
      return profile != null;
   }
   
   public NameAndId findRecipient(MinecraftServer server){
      UserNameToIdResolver baseCache = server.services().nameToIdCache();
      if(!(baseCache instanceof CachedUserNameToIdResolver userCache)) return null;
      
      if(recipientId != null && recipient != null){
         return new NameAndId(recipientId, recipient);
      }else{
         NameAndId entry = userCache.get(recipient).orElse(null);
         if(entry != null){
            if(recipientId == null){
               recipientId = entry.id();
            }
         }
         return entry;
      }
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
