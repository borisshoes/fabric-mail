package net.borisshoes.fabricmail.cardinalcomponents;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.UserCache;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.rmi.server.UID;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class MailMessage {
   
   private final String sender;
   private UUID senderId;
   private final String recipient;
   private UUID recipientId;
   private final String message;
   private final UUID uuid;
   private final long timestamp;
   private NbtCompound parcel;
   
   
   public MailMessage(GameProfile senderProfile, String recipient, @Nullable UUID recipientId, String message, UUID uuid, long timestamp, NbtCompound parcel){
      this.senderId = senderProfile.getId();
      this.sender = senderProfile.getName();
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
   
   public NbtCompound parcel(){
      return parcel;
   }
   
   public boolean checkValid(MinecraftServer server){
      GameProfile profile = findRecipient(server);
      return profile != null;
   }
   
   public GameProfile findRecipient(MinecraftServer server){
      if(server.getUserCache() == null) return null;
      
      if(recipientId != null && recipient != null){
         return new GameProfile(recipientId, recipient);
      }else{
         GameProfile newProfile = server.getUserCache().findByName(recipient).orElse(null);
         if(newProfile != null){
            if(recipientId == null && newProfile.getId() != null){
               recipientId = newProfile.getId();
            }
         }
         return newProfile;
      }
   }
   
   public String getTimeDiff(long curTime){
      long timeDiff = (curTime - timestamp) / 1000;
      long subtract = timeDiff;
      long daysDif = subtract / 86400;
      subtract -= daysDif * 86400;
      long hoursDif = subtract / 3600;
      subtract -= hoursDif * 3600;
      long minutesDif = subtract / 60;
      subtract -= minutesDif * 60;
      long secondsDiff = subtract;
      
      String diff = "Sent [";
      if(daysDif > 0 ) diff += daysDif+" Days ";
      if(hoursDif > 0 ) diff += hoursDif+" Hours ";
      if(minutesDif > 0 ) diff += minutesDif+" Minutes ";
      if(secondsDiff > 0 ) diff += secondsDiff+" Seconds ";
      diff = diff.substring(0,diff.length()-1);
      diff += "] Ago";
      
      return diff;
   }
   
   private ItemStack peekParcel(RegistryWrapper.WrapperLookup registryLookup){
      return ItemStack.CODEC.parse(RegistryOps.of(NbtOps.INSTANCE,registryLookup),parcel).result().orElse(ItemStack.EMPTY);
   }
   
   public ItemStack popParcel(RegistryWrapper.WrapperLookup registryLookup){
      ItemStack stack = ItemStack.CODEC.parse(RegistryOps.of(NbtOps.INSTANCE,registryLookup),parcel).result().orElse(ItemStack.EMPTY);
      parcel = new NbtCompound();
      return stack;
   }
}
