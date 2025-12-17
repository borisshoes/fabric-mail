package net.borisshoes.fabricmail;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.borisshoes.borislib.callbacks.LoginCallback;
import net.borisshoes.borislib.datastorage.DataKey;
import net.borisshoes.borislib.datastorage.DataRegistry;
import net.borisshoes.borislib.utils.CodecUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static net.borisshoes.fabricmail.FabricMail.MOD_ID;

public class MailStorage {
   public static final Codec<MailStorage> CODEC = RecordCodecBuilder.create(i -> i.group(
         MailMessage.CODEC.listOf().optionalFieldOf("mail", List.of()).forGetter(MailStorage::getMessages)
   ).apply(i, (list) -> {
      MailStorage c = new MailStorage();
      c.mails.addAll(list);
      return c;
   }));
   
   public static final DataKey<MailStorage> KEY = DataRegistry.register(DataKey.ofGlobal(Identifier.fromNamespaceAndPath(MOD_ID, "mail"),CODEC,MailStorage::new));
   
   public final List<MailMessage> mails = new ArrayList<>();
   
   public MailStorage(){}
   
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
}
