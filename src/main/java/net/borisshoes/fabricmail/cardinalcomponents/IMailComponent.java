package net.borisshoes.fabricmail.cardinalcomponents;

import net.borisshoes.fabricmail.MailMessage;
import org.ladysnake.cca.api.v3.component.ComponentV3;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public interface IMailComponent extends ComponentV3 {
   List<MailMessage> getMails();
   List<MailMessage> getMailsFor(ServerPlayer player);
   List<MailMessage> getMailsFrom(ServerPlayer player);
   MailMessage getMail(String mailId);
   boolean addMail(MailMessage mail);
   boolean removeMail(String mailId);
   void clearMailFor(ServerPlayer player);
}