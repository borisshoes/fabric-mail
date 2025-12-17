package net.borisshoes.fabricmail.cardinalcomponents;

import com.mojang.authlib.GameProfile;
import net.borisshoes.fabricmail.FabricMail;
import net.borisshoes.fabricmail.MailMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.*;

public class MailComponent implements IMailComponent{
   
   private final List<MailMessage> mails = new ArrayList<>();
   
   @Override
   public void readData(ValueInput view){
      try{
         mails.clear();
         for(CompoundTag mailTag : view.listOrEmpty("Mails", CompoundTag.CODEC)){
            GameProfile senderProf = new GameProfile(FabricMail.getIdOrNull(mailTag.getStringOr("fromId", "")), mailTag.getStringOr("from", ""));
            mails.add(new MailMessage(
                  senderProf,
                  mailTag.getStringOr("to", ""),
                  FabricMail.getIdOrNull(mailTag.getStringOr("toId", "")),
                  mailTag.getStringOr("message", ""),
                  UUID.fromString(mailTag.getStringOr("id", "")),
                  mailTag.getLongOr("time", 0L),
                  mailTag.getCompound("parcel").orElse(new CompoundTag())));
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   @Override
   public void writeData(ValueOutput view){
      try{
         ValueOutput.TypedOutputList<CompoundTag> listAppender = view.list("Mails", CompoundTag.CODEC);
         for(MailMessage mail : mails){
            CompoundTag mailTag = new CompoundTag();
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
   public List<MailMessage> getMailsFor(ServerPlayer player){
      mails.removeIf(mail -> !mail.checkValid(player.level().getServer()));
      return mails.stream().filter(mail -> {
         NameAndId p = mail.findRecipient(player.level().getServer());
         return p != null && p.id().equals(player.getUUID());
      }).toList();
   }
   
   @Override
   public List<MailMessage> getMailsFrom(ServerPlayer player){
      mails.removeIf(mail -> !mail.checkValid(player.level().getServer()));
      return mails.stream().filter(mail -> mail.senderId().equals(player.getUUID())).toList();
   }
   
   @Override
   public void clearMailFor(ServerPlayer player){
      mails.removeIf(mail -> !mail.checkValid(player.level().getServer()));
      mails.removeIf(mail -> {
         NameAndId p = mail.findRecipient(player.level().getServer());
         return p != null && p.id().equals(player.getUUID());
      });
   }
}
