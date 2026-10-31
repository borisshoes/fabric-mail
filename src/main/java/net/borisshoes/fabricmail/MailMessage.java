package net.borisshoes.fabricmail;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.players.NameAndId;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class MailMessage {

   private final String sender;
   private final UUID senderId;
   private final String recipient;
   private final UUID recipientId;
   private final String message;
   private final UUID uuid;
   private final long timestamp;
   private CompoundTag parcel;
   
   public MailMessage(NameAndId senderProfile, String recipient, UUID recipientId, String message, CompoundTag parcel){
      this(senderProfile, recipient, recipientId, message, UUID.randomUUID(), System.currentTimeMillis(), parcel);
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
   
   public UUID senderId(){
      return senderId;
   }
   
   public String recipient(){
      return recipient;
   }
   
   public UUID recipientId(){
      return recipientId;
   }
   
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
         text.append(Component.literal(daysDif + " "));
         text.append(Component.translatable("text.fabricmail.days"));
         needSpace = true;
      }
      if(hoursDif > 0){
         if(needSpace) text.append(Component.literal(" "));
         text.append(Component.literal(hoursDif + " "));
         text.append(Component.translatable("text.fabricmail.hours"));
         needSpace = true;
      }
      if(minutesDif > 0){
         if(needSpace) text.append(Component.literal(" "));
         text.append(Component.literal(minutesDif + " "));
         text.append(Component.translatable("text.fabricmail.minutes"));
         needSpace = true;
      }
      if(secondsDiff > 0){
         if(needSpace) text.append(Component.literal(" "));
         text.append(Component.literal(secondsDiff + " "));
         text.append(Component.translatable("text.fabricmail.seconds"));
      }
      return Component.translatable("text.fabricmail.time_diff", text);
   }
   
   private ItemStack peekParcel(HolderLookup.Provider registryLookup){
      return ItemStack.CODEC.parse(RegistryOps.create(NbtOps.INSTANCE, registryLookup), parcel).result().orElse(ItemStack.EMPTY);
   }
   
   public ItemStack popParcel(HolderLookup.Provider registryLookup){
      ItemStack stack = ItemStack.CODEC.parse(RegistryOps.create(NbtOps.INSTANCE, registryLookup), parcel).result().orElse(ItemStack.EMPTY);
      parcel = new CompoundTag();
      return stack;
   }
}
