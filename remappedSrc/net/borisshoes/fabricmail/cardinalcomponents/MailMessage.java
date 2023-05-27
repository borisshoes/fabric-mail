package net.borisshoes.fabricmail.cardinalcomponents;

import com.mojang.authlib.GameProfile;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;

import java.util.Objects;
import java.util.UUID;

public class MailMessage {
   
   private final String sender;
   private final String recipient;
   private final String message;
   private final UUID uuid;
   private final long timestamp;
   private NbtCompound parcel;
   
   
   public MailMessage(String sender, String recipient, String message, UUID uuid, long timestamp, NbtCompound parcel){
      this.sender = sender;
      this.recipient = recipient;
      this.message = message;
      this.uuid = uuid;
      this.timestamp = timestamp;
      this.parcel = parcel;
   }
   
   public String sender(){
      return sender;
   }
   
   public String recipient(){
      return recipient;
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
   
   public NbtCompound parcel(){
      return parcel;
   }
   
   public GameProfile findSender(MinecraftServer server){
      return server.getUserCache().findByName(sender).orElse(null);
   }
   
   public GameProfile findRecipient(MinecraftServer server){
      return server.getUserCache().findByName(recipient).orElse(null);
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
   
   private ItemStack peekParcel(){
      return ItemStack.fromNbt(parcel);
   }
   
   public ItemStack popParcel(){
      ItemStack stack = ItemStack.fromNbt(parcel);
      parcel = new NbtCompound();
      return Objects.requireNonNullElse(stack, ItemStack.EMPTY);
   }
}
