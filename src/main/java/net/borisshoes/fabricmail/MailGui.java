package net.borisshoes.fabricmail;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.borisshoes.fabricmail.cardinalcomponents.IMailComponent;
import net.borisshoes.fabricmail.cardinalcomponents.MailMessage;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static net.borisshoes.fabricmail.FabricMail.givePlayerStack;
import static net.borisshoes.fabricmail.cardinalcomponents.WorldDataComponentInitializer.MAILS;

public class MailGui extends SimpleGui {
   
   private MailSort sortType;
   private MailFilter filterType;
   private int page = 1;
   private boolean outboundMode;
   private List<MailMessage> mailList;
   
   public MailGui(ServerPlayerEntity player){
      super(ScreenHandlerType.GENERIC_9X6, player, false);
      setTitle(Text.literal("Your Mail Inbox"));
      this.outboundMode = false;
      this.sortType = MailSort.RECENT_FIRST;
      this.filterType = MailFilter.NONE;
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, SlotActionType action){
      boolean indexInCenter = index > 9 && index < 45 && index % 9 != 0 && index % 9 != 8;
      if(index == 4){
         this.outboundMode = !this.outboundMode;
         buildMailboxGui();
      }else if(index == 0){
         boolean backwards = type == ClickType.MOUSE_RIGHT;
         boolean shiftLeft = type == ClickType.MOUSE_LEFT_SHIFT;
         if(shiftLeft){
            this.sortType = MailSort.RECENT_FIRST;
         }else{
            this.sortType = MailSort.cycleSort(this.sortType,backwards);
         }
         
         buildMailboxGui();
      }else if(index == 8){
         boolean backwards = type == ClickType.MOUSE_RIGHT;
         boolean shiftLeft = type == ClickType.MOUSE_LEFT_SHIFT;
         if(shiftLeft){
            this.filterType = MailFilter.NONE;
         }else{
            this.filterType = MailFilter.cycleFilter(this.filterType,backwards);
         }
         this.mailList = sortedFilteredMailList();
         
         int numPages = (int) Math.ceil((float)mailList.size()/28.0);
         if(this.page > numPages){
            this.page = Math.max(1,numPages);
         }
         buildMailboxGui();
      }else if(index == 45){
         if(this.page > 1){
            this.page--;
            buildMailboxGui();
         }
      }else if(index == 53){
         int numPages = (int) Math.ceil((float)mailList.size()/28.0);
         if(this.page < numPages){
            this.page++;
            buildMailboxGui();
         }
      }else if(indexInCenter){
         int ind = (7*(index/9 - 1) + (index % 9 - 1)) + 28*(this.page-1);
         if(ind >= mailList.size()) return true;
         IMailComponent mailbox = MAILS.get(player.getServer().getOverworld());
         MailMessage mail = mailList.get(ind);
         boolean right = type == ClickType.MOUSE_RIGHT;
         boolean shiftLeft = type == ClickType.MOUSE_LEFT_SHIFT;
         
         if(outboundMode){
            if(!mail.parcel().isEmpty()){
               player.sendMessage(Text.literal("")
                     .append(Text.literal("Revoked Mail to ").formatted(Formatting.LIGHT_PURPLE))
                     .append(Text.literal(mail.recipient()).formatted(Formatting.AQUA))
                     .append(Text.literal(" and returned the Parcel to your Inventory.").formatted(Formatting.LIGHT_PURPLE)));
            }else{
               player.sendMessage(Text.literal("")
                     .append(Text.literal("Revoked Mail to ").formatted(Formatting.LIGHT_PURPLE))
                     .append(Text.literal(mail.recipient()).formatted(Formatting.AQUA))
                     .append(Text.literal(".").formatted(Formatting.LIGHT_PURPLE)));
            }
            
            givePlayerStack(player,mail.popParcel(player.getRegistryManager()));
            mailbox.removeMail(mail.uuid().toString());
         }else{
            if(right){
               mailbox.removeMail(mail.uuid().toString());
               player.sendMessage(Text.literal("Deleted Mail "+mail.uuid().toString()).formatted(Formatting.LIGHT_PURPLE));
            }else if(shiftLeft){
               player.sendMessage(Text.literal(""));
               player.sendMessage(Text.literal("")
                     .append(Text.literal("From: ").formatted(Formatting.BLUE))
                     .append(Text.literal(mail.sender()+" ").formatted(Formatting.DARK_AQUA))
                     .append(Text.literal(mail.getTimeDiff(System.currentTimeMillis())).formatted(Formatting.GOLD)));
               player.sendMessage(Text.literal(mail.message()).formatted(Formatting.AQUA));
               player.sendMessage(Text.literal(""));
               if(!mail.parcel().isEmpty()){
                  player.sendMessage(Text.literal("This Mail contained a Parcel. It has been added to your Inventory.").formatted(Formatting.GREEN,Formatting.ITALIC));
               }
               
               givePlayerStack(player,mail.popParcel(player.getRegistryManager()));
               mailbox.removeMail(mail.uuid().toString());
               player.sendMessage(Text.literal("Deleted Mail "+mail.uuid().toString()).formatted(Formatting.LIGHT_PURPLE));
            }else{
               player.sendMessage(Text.literal(""));
               player.sendMessage(Text.literal("")
                     .append(Text.literal("From: ").formatted(Formatting.BLUE))
                     .append(Text.literal(mail.sender()+" ").formatted(Formatting.DARK_AQUA))
                     .append(Text.literal(mail.getTimeDiff(System.currentTimeMillis())).formatted(Formatting.GOLD)));
               player.sendMessage(Text.literal(mail.message()).formatted(Formatting.AQUA));
               player.sendMessage(Text.literal(""));
               if(!mail.parcel().isEmpty()){
                  player.sendMessage(Text.literal("This Mail contained a Parcel. It has been added to your Inventory.").formatted(Formatting.GREEN,Formatting.ITALIC));
               }
               player.sendMessage(Text.literal("[Click to remove message from your mailbox]").styled(s ->
                     s.withClickEvent(new ClickEvent.RunCommand("/mail delete "+mail.uuid().toString()))
                           .withHoverEvent(new HoverEvent.ShowText(Text.literal("Click to Delete Mail")))
                           .withColor(Formatting.LIGHT_PURPLE)));
               
               givePlayerStack(player,mail.popParcel(player.getRegistryManager()));
            }
         }
         buildMailboxGui();
      }
      return true;
   }
   
   public void buildMailboxGui(){
      this.mailList = sortedFilteredMailList();
      
      if(this.outboundMode){
         setTitle(Text.literal("Your Mail Outbox"));
      }else{
         setTitle(Text.literal("Your Mail Inbox"));
      }
      
      int numPages = (int) Math.ceil((float)this.mailList.size()/28.0);
      
      for(int i = 0; i < getSize(); i++){
         GuiElementBuilder pane = new GuiElementBuilder(Items.BLUE_STAINED_GLASS_PANE).hideTooltip();
         setSlot(i,pane);
      }
      
      GuiElementBuilder nextArrow = new GuiElementBuilder(Items.SPECTRAL_ARROW).hideDefaultTooltip();
      nextArrow.setName((Text.literal("").append(Text.literal("Next Page").formatted(Formatting.GOLD))));
      nextArrow.addLoreLine(removeItalics((Text.literal("").append(Text.literal("("+page+" of "+numPages+")").formatted(Formatting.DARK_PURPLE)))));
      setSlot(53,nextArrow);
      
      GuiElementBuilder prevArrow = new GuiElementBuilder(Items.SPECTRAL_ARROW).hideDefaultTooltip();
      prevArrow.setName((Text.literal("").append(Text.literal("Prev Page").formatted(Formatting.GOLD))));
      prevArrow.addLoreLine(removeItalics((Text.literal("").append(Text.literal("("+page+" of "+numPages+")").formatted(Formatting.DARK_PURPLE)))));
      setSlot(45,prevArrow);
      
      GuiElementBuilder modeButton = new GuiElementBuilder(this.outboundMode ? Items.WRITABLE_BOOK : Items.WRITTEN_BOOK).hideDefaultTooltip();
      modeButton.setName((Text.literal("").append(Text.literal(this.outboundMode ? "Outbox Mode" : "Inbox Mode").formatted(Formatting.AQUA))));
      modeButton.addLoreLine(removeItalics(Text.literal(this.outboundMode ? "Showing you your outbound mails" : "Showing you your inbound mails").formatted(Formatting.DARK_PURPLE)));
      modeButton.addLoreLine(removeItalics(Text.literal("")));
      modeButton.addLoreLine(removeItalics(Text.literal("").append(Text.literal("Click").formatted(Formatting.AQUA)).append(Text.literal(" to toggle the mailbox mode").formatted(Formatting.LIGHT_PURPLE))));
      setSlot(4,modeButton);
      
      GuiElementBuilder filterBuilt = new GuiElementBuilder(Items.HOPPER).hideDefaultTooltip();
      filterBuilt.setName(Text.literal("Filter Mail").formatted(Formatting.DARK_PURPLE));
      filterBuilt.addLoreLine(removeItalics(Text.literal("").append(Text.literal("Click").formatted(Formatting.AQUA)).append(Text.literal(" to change current filter.").formatted(Formatting.LIGHT_PURPLE))));
      filterBuilt.addLoreLine(removeItalics(Text.literal("").append(Text.literal("Right Click").formatted(Formatting.GREEN)).append(Text.literal(" to cycle filter backwards.").formatted(Formatting.LIGHT_PURPLE))));
      filterBuilt.addLoreLine(removeItalics(Text.literal("").append(Text.literal("Shift Left Click").formatted(Formatting.YELLOW)).append(Text.literal(" to reset filter.").formatted(Formatting.LIGHT_PURPLE))));
      filterBuilt.addLoreLine(removeItalics(Text.literal("")));
      filterBuilt.addLoreLine(removeItalics(Text.literal("").append(Text.literal("Current Filter: ").formatted(Formatting.AQUA)).append(MailFilter.getColoredLabel(this.filterType))));
      setSlot(8,filterBuilt);
      
      GuiElementBuilder sortBuilt = new GuiElementBuilder(Items.NETHER_STAR).hideDefaultTooltip();
      sortBuilt.setName(Text.literal("Sort Mail").formatted(Formatting.DARK_PURPLE));
      sortBuilt.addLoreLine(removeItalics(Text.literal("").append(Text.literal("Click").formatted(Formatting.AQUA)).append(Text.literal(" to change current sort type.").formatted(Formatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(removeItalics(Text.literal("").append(Text.literal("Right Click").formatted(Formatting.GREEN)).append(Text.literal(" to cycle sort backwards.").formatted(Formatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(removeItalics(Text.literal("").append(Text.literal("Shift Left Click").formatted(Formatting.YELLOW)).append(Text.literal(" to reset sort.").formatted(Formatting.LIGHT_PURPLE))));
      sortBuilt.addLoreLine(removeItalics(Text.literal("")));
      sortBuilt.addLoreLine(removeItalics(Text.literal("").append(Text.literal("Sorting By: ").formatted(Formatting.AQUA)).append(MailSort.getColoredLabel(this.sortType))));
      setSlot(0,sortBuilt);
      
      
      int k = (page-1)*28;
      for(int i = 0; i < 4; i++){
         for(int j = 0; j < 7; j++){
            if(k < this.mailList.size() && k >= 0){
               MailMessage mail = this.mailList.get(k);
               boolean hasParcel = !mail.parcel().isEmpty();
               GuiElementBuilder mailItem = new GuiElementBuilder(hasParcel ? Items.CHEST : Items.PAPER).hideDefaultTooltip();
               
               if(this.outboundMode){
                  mailItem.setName((Text.literal("")
                        .append(Text.literal("Mail to: ").formatted(Formatting.BLUE))
                        .append(Text.literal(mail.recipient()).formatted(Formatting.DARK_AQUA,Formatting.BOLD))));
                  mailItem.addLoreLine(removeItalics(Text.literal(mail.getTimeDiff(System.currentTimeMillis())).formatted(Formatting.GOLD)));
                  if(!mail.parcel().isEmpty()){
                     mailItem.addLoreLine(removeItalics(Text.literal("< Contains Parcel >").formatted(Formatting.GREEN)));
                  }
                  mailItem.addLoreLine(removeItalics(Text.literal("")));
                  mailItem.addLoreLine(removeItalics((Text.literal("")
                        .append(Text.literal("Click").formatted(Formatting.AQUA))
                        .append(Text.literal(" to REVOKE this mail").formatted(Formatting.RED)))));
               }else{
                  mailItem.setName((Text.literal("")
                        .append(Text.literal("Mail from: ").formatted(Formatting.BLUE))
                        .append(Text.literal(mail.sender()).formatted(Formatting.DARK_AQUA,Formatting.BOLD))));
                  mailItem.addLoreLine(removeItalics(Text.literal(mail.getTimeDiff(System.currentTimeMillis())).formatted(Formatting.GOLD)));
                  if(!mail.parcel().isEmpty()){
                     mailItem.addLoreLine(removeItalics(Text.literal("< Contains Parcel >").formatted(Formatting.GREEN)));
                  }
                  mailItem.addLoreLine(removeItalics(Text.literal("")));
                  mailItem.addLoreLine(removeItalics((Text.literal("")
                        .append(Text.literal("Click").formatted(Formatting.AQUA))
                        .append(Text.literal(" to READ this mail").formatted(Formatting.LIGHT_PURPLE)))));
                  mailItem.addLoreLine(removeItalics((Text.literal("")
                        .append(Text.literal("Shift Click").formatted(Formatting.GREEN))
                        .append(Text.literal(" to READ AND DELETE this mail").formatted(Formatting.GOLD)))));
                  mailItem.addLoreLine(removeItalics((Text.literal("")
                        .append(Text.literal("Right Click").formatted(Formatting.YELLOW))
                        .append(Text.literal(" to DELETE this mail").formatted(Formatting.RED)))));
               }
               setSlot((i*9+10)+j,mailItem);
            }else{
               setSlot((i*9+10)+j,new GuiElementBuilder(Items.AIR));
            }
            k++;
         }
      }
   }
   
   
   private List<MailMessage> sortedFilteredMailList(){
      IMailComponent mailbox = MAILS.get(player.getServer().getOverworld());
      List<MailMessage> mails = this.outboundMode ? mailbox.getMailsFrom(player) : mailbox.getMailsFor(player);
      
      List<MailMessage> sortedFiltered = new ArrayList<>(mails.stream().filter(mail -> MailFilter.matchesFilter(this.player,this.filterType,mail)).toList());
      
      switch(this.sortType){
         case RECENT_LAST -> {
            sortedFiltered.sort(Comparator.comparingLong(MailMessage::timestamp));
         }
         case null, default -> {
            sortedFiltered.sort(Comparator.comparingLong(mail -> -mail.timestamp()));
         }
      }
      return sortedFiltered;
   }
   
   public enum MailSort{
      RECENT_FIRST("Recent First"),
      RECENT_LAST("Recent Last");
      
      public final String label;
      
      MailSort(String label){
         this.label = label;
      }
      
      public static Text getColoredLabel(MailSort sort){
         MutableText text = Text.literal(sort.label);
         
         return switch(sort){
            case RECENT_FIRST -> text.formatted(Formatting.AQUA);
            case RECENT_LAST -> text.formatted(Formatting.GREEN);
         };
      }
      
      public static MailSort cycleSort(MailSort sort, boolean backwards){
         MailSort[] sorts = MailSort.values();
         int ind = -1;
         for(int i = 0; i < sorts.length; i++){
            if(sort == sorts[i]){
               ind = i;
            }
         }
         ind += backwards ? -1 : 1;
         if(ind >= sorts.length) ind = 0;
         if(ind < 0) ind = sorts.length-1;
         return sorts[ind];
      }
   }
   
   public enum MailFilter{
      NONE("None"),
      HAS_PARCEL("Has Parcel"),
      NO_PARCEL("No Parcel");
      
      public final String label;
      
      MailFilter(String label){
         this.label = label;
      }
      
      public static Text getColoredLabel(MailFilter filter){
         MutableText text = Text.literal(filter.label);
         
         return switch(filter){
            case NONE -> text.formatted(Formatting.WHITE);
            case HAS_PARCEL -> text.formatted(Formatting.GREEN);
            case NO_PARCEL -> text.formatted(Formatting.RED);
         };
      }
      
      public static MailFilter cycleFilter(MailFilter filter, boolean backwards){
         MailFilter[] filters = MailFilter.values();
         int ind = -1;
         for(int i = 0; i < filters.length; i++){
            if(filter == filters[i]){
               ind = i;
            }
         }
         ind += backwards ? -1 : 1;
         if(ind >= filters.length) ind = 0;
         if(ind < 0) ind = filters.length-1;
         return filters[ind];
      }
      
      public static boolean matchesFilter(ServerPlayerEntity player, MailFilter filter, MailMessage mail){
         if(filter == MailFilter.NONE) return true;
         boolean hasParcel = !mail.parcel().isEmpty();
         if(filter == MailFilter.HAS_PARCEL) return hasParcel;
         if(filter == MailFilter.NO_PARCEL) return !hasParcel;
         return false;
      }
   }
   
   private static MutableText removeItalics(MutableText text){
      Style parentStyle = Style.EMPTY.withColor(Formatting.DARK_PURPLE).withItalic(false).withBold(false).withUnderline(false).withObfuscated(false).withStrikethrough(false);
      return text.setStyle(text.getStyle().withParent(parentStyle));
   }
}
