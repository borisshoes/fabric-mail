package net.borisshoes.fabricmail.cardinalcomponents;

import org.ladysnake.cca.api.v3.component.ComponentV3;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockBox;

import java.util.HashMap;
import java.util.List;

public interface IMailComponent extends ComponentV3 {
   List<MailMessage> getMails();
   List<MailMessage> getMailsFor(ServerPlayerEntity player);
   List<MailMessage> getMailsFrom(ServerPlayerEntity player);
   MailMessage getMail(String mailId);
   boolean addMail(MailMessage mail);
   boolean removeMail(String mailId);
   void clearMailFor(ServerPlayerEntity player);
}