package net.borisshoes.fabricmail.cardinalcomponents;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MailComponent implements IMailComponent{
   
   private final List<MailMessage> mails = new ArrayList<>();
   
   @Override
   public void readFromNbt(NbtCompound tag){
      try{
         mails.clear();
         NbtList mailsTag = tag.getList("Mails", NbtType.COMPOUND);
         for (NbtElement e : mailsTag) {
            NbtCompound mailTag = (NbtCompound) e;
            mails.add(new MailMessage(mailTag.getString("from"),mailTag.getString("to"),mailTag.getString("message"),UUID.fromString(mailTag.getString("id")),mailTag.getLong("time"), mailTag.getCompound("parcel")));
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   @Override
   public void writeToNbt(NbtCompound tag){
      try{
         NbtList mailsTag = new NbtList();
         for(MailMessage mail : mails){
            NbtCompound mailTag = new NbtCompound();
            mailTag.putString("from",mail.sender());
            mailTag.putString("to",mail.recipient());
            mailTag.putString("message",mail.message());
            mailTag.putString("id",mail.uuid().toString());
            mailTag.putLong("time",mail.timestamp());
            mailTag.put("parcel",mail.parcel());
            mailsTag.add(mailTag);
         }
         tag.put("Mails",mailsTag);
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   @Override
   public List<MailMessage> getMails(){
      return mails;
   }
   
   @Override
   public boolean addMail(MailMessage mail){
      if(mails.stream().anyMatch(m -> mail.uuid().equals(m.uuid()))) return false;
      return mails.add(mail);
   }
   
   @Override
   public boolean removeMail(String mailId){
      return mails.removeIf(m -> UUID.fromString(mailId).equals(m.uuid()));
   }
   
   @Override
   public List<MailMessage> getMailsFor(ServerPlayerEntity player){
      if(player.getServer() == null) return new ArrayList<>();
      return mails.stream().filter(mail -> {
         GameProfile p = mail.findRecipient(player.getServer());
         return p != null && p.getId().equals(player.getUuid());
      }).toList();
   }
   
   @Override
   public void clearMailFor(ServerPlayerEntity player){
      if(player.getServer() == null) return;
      mails.removeIf(mail -> {
         GameProfile p = mail.findRecipient(player.getServer());
         return p != null && p.getId().equals(player.getUuid());
      });
   }
}
