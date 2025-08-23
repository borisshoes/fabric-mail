package net.borisshoes.fabricmail.cardinalcomponents;

import com.mojang.authlib.GameProfile;
import net.borisshoes.fabricmail.FabricMail;
import net.minecraft.inventory.StackWithSlot;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;

import java.util.*;

public class MailComponent implements IMailComponent{
   
   private final List<MailMessage> mails = new ArrayList<>();
   
   @Override
   public void readData(ReadView view){
      try{
         mails.clear();
         for(NbtCompound mailTag : view.getTypedListView("Mails", NbtCompound.CODEC)){
            GameProfile senderProf = new GameProfile(FabricMail.getIdOrNull(mailTag.getString("fromId", "")), mailTag.getString("from", ""));
            mails.add(new MailMessage(
                  senderProf,
                  mailTag.getString("to", ""),
                  FabricMail.getIdOrNull(mailTag.getString("toId", "")),
                  mailTag.getString("message", ""),
                  UUID.fromString(mailTag.getString("id", "")),
                  mailTag.getLong("time", 0L),
                  mailTag.getCompound("parcel").orElse(new NbtCompound())));
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   @Override
   public void writeData(WriteView view){
      try{
         WriteView.ListAppender<NbtCompound> listAppender = view.getListAppender("Mails", NbtCompound.CODEC);
         for(MailMessage mail : mails){
            NbtCompound mailTag = new NbtCompound();
            mailTag.putString("from",mail.sender());
            mailTag.putString("fromId", mail.senderId() == null ? "" : mail.senderId().toString());
            mailTag.putString("to",mail.recipient());
            mailTag.putString("toId", mail.recipientId() == null ? "" : mail.recipientId().toString());
            mailTag.putString("message",mail.message());
            mailTag.putString("id",mail.uuid().toString());
            mailTag.putLong("time",mail.timestamp());
            mailTag.put("parcel",mail.parcel());
            listAppender.add(mailTag);
         }
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
   public MailMessage getMail(String mailId){
      Optional<MailMessage> opt = mails.stream().filter(m -> UUID.fromString(mailId).equals(m.uuid())).findFirst();
      return opt.orElse(null);
   }
   
   @Override
   public List<MailMessage> getMailsFor(ServerPlayerEntity player){
      if(player.getServer() == null) return new ArrayList<>();
      mails.removeIf(mail -> !mail.checkValid(player.getServer()));
      return mails.stream().filter(mail -> {
         GameProfile p = mail.findRecipient(player.getServer());
         return p != null && p.getId().equals(player.getUuid());
      }).toList();
   }
   
   @Override
   public List<MailMessage> getMailsFrom(ServerPlayerEntity player){
      if(player.getServer() == null) return new ArrayList<>();
      mails.removeIf(mail -> !mail.checkValid(player.getServer()));
      return mails.stream().filter(mail -> mail.senderId().equals(player.getUuid())).toList();
   }
   
   @Override
   public void clearMailFor(ServerPlayerEntity player){
      if(player.getServer() == null) return;
      mails.removeIf(mail -> !mail.checkValid(player.getServer()));
      mails.removeIf(mail -> {
         GameProfile p = mail.findRecipient(player.getServer());
         return p != null && p.getId().equals(player.getUuid());
      });
   }
}
