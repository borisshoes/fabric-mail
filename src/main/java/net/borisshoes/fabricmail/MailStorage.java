package net.borisshoes.fabricmail;

import com.mojang.authlib.GameProfile;
import net.borisshoes.borislib.datastorage.DataKey;
import net.borisshoes.borislib.datastorage.DataRegistry;
import net.borisshoes.borislib.datastorage.StorableData;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.level.storage.ValueInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.borisshoes.fabricmail.FabricMail.MOD_ID;

public class MailStorage implements StorableData {
   
   public static final DataKey<MailStorage> KEY = DataRegistry.register(DataKey.ofGlobal(Identifier.fromNamespaceAndPath(MOD_ID, "mail"), MailStorage::new));
   
   public final List<MailMessage> mails = new ArrayList<>();
   
   public MailStorage(){
   }
   
   public List<MailMessage> getMessages(){
      return mails;
   }
   
   public List<MailMessage> getMails(){
      return mails;
   }
   
   public boolean addMail(MailMessage mail){
      if(mails.stream().anyMatch(m -> mail.uuid().equals(m.uuid()))) return false;
      return mails.add(mail);
   }
   
   public boolean removeMail(String mailId){
      return mails.removeIf(m -> UUID.fromString(mailId).equals(m.uuid()));
   }
   
   public MailMessage getMail(String mailId){
      Optional<MailMessage> opt = mails.stream().filter(m -> UUID.fromString(mailId).equals(m.uuid())).findFirst();
      return opt.orElse(null);
   }
   
   public List<MailMessage> getMailsFor(ServerPlayer player){
      mails.removeIf(mail -> !mail.checkValid(player.level().getServer()));
      return mails.stream().filter(mail -> {
         NameAndId p = mail.findRecipient(player.level().getServer());
         return p != null && p.id().equals(player.getUUID());
      }).toList();
   }
   
   public List<MailMessage> getMailsFrom(ServerPlayer player){
      mails.removeIf(mail -> !mail.checkValid(player.level().getServer()));
      return mails.stream().filter(mail -> mail.senderId().equals(player.getUUID())).toList();
   }
   
   public List<MailMessage> getMailsForOrFrom(ServerPlayer player){
      mails.removeIf(mail -> !mail.checkValid(player.level().getServer()));
      return mails.stream().filter(mail -> {
         NameAndId p = mail.findRecipient(player.level().getServer());
         boolean forPlayer = p != null && p.id().equals(player.getUUID());
         boolean fromPlayer = mail.senderId().equals(player.getUUID());
         return forPlayer || fromPlayer;
      }).toList();
   }
   
   public void clearMailFor(ServerPlayer player){
      mails.removeIf(mail -> !mail.checkValid(player.level().getServer()));
      mails.removeIf(mail -> {
         NameAndId p = mail.findRecipient(player.level().getServer());
         return p != null && p.id().equals(player.getUUID());
      });
   }
   
   @Override
   public void read(ValueInput view){
      try{
         mails.clear();
         for(CompoundTag mailTag : view.listOrEmpty("mail", CompoundTag.CODEC)){
            mails.add(new MailMessage(
                  new NameAndId(AlgoUtils.getUUID(mailTag.getStringOr("sender_id", "")), mailTag.getStringOr("sender", "")),
                  mailTag.getStringOr("recipient", ""),
                  FabricMail.getIdOrNull(mailTag.getStringOr("recipient_id", "")),
                  mailTag.getStringOr("message", ""),
                  UUID.fromString(mailTag.getStringOr("uuid", "")),
                  mailTag.getLongOr("timestamp", 0L),
                  mailTag.getCompound("parcel").orElse(new CompoundTag())));
         }
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   
   @Override
   public void writeNbt(CompoundTag tag){
      try{
         ListTag mailList = new ListTag();
         for(MailMessage mail : mails){
            CompoundTag mailTag = new CompoundTag();
            mailTag.putString("sender", mail.sender());
            mailTag.putString("sender_id", mail.senderId() == null ? "" : mail.senderId().toString());
            mailTag.putString("recipient", mail.recipient());
            mailTag.putString("recipient_id", mail.recipientId() == null ? "" : mail.recipientId().toString());
            mailTag.putString("message", mail.message());
            mailTag.putString("uuid", mail.uuid().toString());
            mailTag.putLong("timestamp", mail.timestamp());
            mailTag.put("parcel", mail.parcel());
            mailList.add(mailTag);
         }
         tag.put("mail", mailList);
      }catch(Exception e){
         e.printStackTrace();
      }
   }
}
