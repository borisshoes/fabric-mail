package net.borisshoes.fabricmail.cardinalcomponents;

import net.borisshoes.borislib.datastorage.DataAccess;
import net.borisshoes.fabricmail.FabricMail;
import net.borisshoes.fabricmail.MailMessage;
import net.borisshoes.fabricmail.MailStorage;
import net.minecraft.server.MinecraftServer;

import javax.print.attribute.standard.Destination;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static net.borisshoes.fabricmail.cardinalcomponents.WorldDataComponentInitializer.MAILS;

public class DataFixer {
   public static void serverStarted(MinecraftServer server){
      List<MailMessage> oldMails = MAILS.get(server.overworld()).getMails();
      MailStorage storage = DataAccess.getGlobal(MailStorage.KEY);
      int converted = 0;
      
      for(MailMessage oldMail : oldMails){
         if(storage.addMail(oldMail)) converted++;
      }
      if(converted > 0){
         oldMails.clear();
         FabricMail.LOGGER.info("Fabric Mail has converted {} old Mails into BorisLib data format. This operation should not repeat itself. You are now OK to upgrade to future versions without Mail data loss.", converted);
      }
   }
}
