package net.borisshoes.fabricmail;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.borisshoes.borislib.BorisLib;
import net.borisshoes.borislib.datastorage.DataAccess;
import net.borisshoes.borislib.gui.*;
import net.borisshoes.borislib.utils.AlgoUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import static net.borisshoes.borislib.utils.TextUtils.removeItalics;
import static net.borisshoes.fabricmail.FabricMail.givePlayerStack;

@SuppressWarnings("unchecked")
public class MailGui extends PagedGui<MailMessage> {
   
   private boolean outboundMode = false;
   
   public MailGui(ServerPlayer player){
      super(MenuType.GENERIC_9x6, player, DataAccess.getGlobal(MailStorage.KEY).getMailsForOrFrom(player));
      
      MailFilter.setData(this.outboundMode,player.getUUID());
      
      itemElemBuilder((mail) -> {
         boolean hasParcel = !mail.parcel().isEmpty();
         GuiElementBuilder mailItem = new GuiElementBuilder(hasParcel ? Items.CHEST : Items.PAPER).hideDefaultTooltip();
         
         if(this.outboundMode){
            mailItem.setName(Component.translatable("gui.fabricmail.mail_to",
                  Component.literal(mail.recipient()).withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD)).withStyle(ChatFormatting.BLUE));
            mailItem.addLoreLine(mail.getTimeDiff(System.currentTimeMillis()).withStyle(ChatFormatting.GOLD));
            if(!mail.parcel().isEmpty()){
               mailItem.addLoreLine(Component.translatable("gui.fabricmail.contains_parcel_gui").withStyle(ChatFormatting.GREEN));
            }
            mailItem.addLoreLine(Component.literal(""));
            mailItem.addLoreLine(Component.translatable("gui.fabricmail.click_revoke",
                  Component.translatable("gui.borislib.click").withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.RED));
         }else{
            mailItem.setName(Component.translatable("gui.fabricmail.mail_from",
                  Component.literal(mail.sender()).withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.BOLD)).withStyle(ChatFormatting.BLUE));
            mailItem.addLoreLine(mail.getTimeDiff(System.currentTimeMillis()).withStyle(ChatFormatting.GOLD));
            if(!mail.parcel().isEmpty()){
               mailItem.addLoreLine(Component.translatable("gui.fabricmail.contains_parcel_gui").withStyle(ChatFormatting.GREEN));
            }
            mailItem.addLoreLine(Component.literal(""));
            mailItem.addLoreLine(Component.translatable("gui.fabricmail.click_read",
                  Component.translatable("gui.borislib.click").withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.LIGHT_PURPLE));
            mailItem.addLoreLine(Component.translatable("gui.fabricmail.shift_click_read_delete",
                  Component.translatable("gui.borislib.shift_click").withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.GOLD));
            mailItem.addLoreLine(Component.translatable("gui.fabricmail.right_click_delete",
                  Component.translatable("gui.borislib.right_click").withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.RED));
         }
         return mailItem;
      });
      
      blankItem(GuiElementBuilder.from(GraphicalItem.withColor(GraphicalItem.PAGE_BG, 0xffca90)));
      
      elemClickFunction((mail, index, clickType) -> {
         MailStorage mailbox = DataAccess.getGlobal(MailStorage.KEY);
         if(outboundMode){
            if(!mail.parcel().isEmpty()){
               player.sendSystemMessage(Component.translatable("text.fabricmail.revoked_mail_to_parcel",
                     Component.literal(mail.recipient()).withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.LIGHT_PURPLE));
            }else{
               player.sendSystemMessage(Component.translatable("text.fabricmail.revoked_mail_to",
                     Component.literal(mail.recipient()).withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.LIGHT_PURPLE));
            }
            
            givePlayerStack(player,mail.popParcel(player.registryAccess()));
            mailbox.removeMail(mail.uuid().toString());
         }else{
            if(clickType == ClickType.MOUSE_RIGHT){
               mailbox.removeMail(mail.uuid().toString());
               player.sendSystemMessage(Component.translatable("gui.fabricmail.deleted_mail", mail.uuid().toString()).withStyle(ChatFormatting.LIGHT_PURPLE));
            }else if(clickType == ClickType.MOUSE_LEFT_SHIFT){
               player.sendSystemMessage(Component.literal(""));
               player.sendSystemMessage(Component.translatable("text.fabricmail.mail_from_header",
                     Component.literal(mail.sender()).withStyle(ChatFormatting.DARK_AQUA),
                     mail.getTimeDiff(System.currentTimeMillis()).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.BLUE));
               player.sendSystemMessage(Component.literal(mail.message()).withStyle(ChatFormatting.AQUA));
               player.sendSystemMessage(Component.literal(""));
               if(!mail.parcel().isEmpty()){
                  player.sendSystemMessage(Component.translatable("text.fabricmail.parcel_added_inventory").withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC));
               }
               
               givePlayerStack(player,mail.popParcel(player.registryAccess()));
               mailbox.removeMail(mail.uuid().toString());
               player.sendSystemMessage(Component.translatable("gui.fabricmail.deleted_mail", mail.uuid().toString()).withStyle(ChatFormatting.LIGHT_PURPLE));
            }else{
               player.sendSystemMessage(Component.literal(""));
               player.sendSystemMessage(Component.translatable("text.fabricmail.mail_from_header",
                     Component.literal(mail.sender()).withStyle(ChatFormatting.DARK_AQUA),
                     mail.getTimeDiff(System.currentTimeMillis()).withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.BLUE));
               player.sendSystemMessage(Component.literal(mail.message()).withStyle(ChatFormatting.AQUA));
               player.sendSystemMessage(Component.literal(""));
               if(!mail.parcel().isEmpty()){
                  player.sendSystemMessage(Component.translatable("text.fabricmail.parcel_added_inventory").withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC));
               }
               player.sendSystemMessage(Component.translatable("text.fabricmail.click_remove_message").withStyle(s ->
                     s.withClickEvent(new ClickEvent.RunCommand("/mail delete "+mail.uuid().toString()))
                           .withHoverEvent(new HoverEvent.ShowText(Component.translatable("text.fabricmail.click_delete_mail")))
                           .withColor(ChatFormatting.LIGHT_PURPLE)));
               
               givePlayerStack(player,mail.popParcel(player.registryAccess()));
            }
         }
         buildPage();
      });
      
      curSort(MailSort.RECENT_FIRST);
      curFilter(MailFilter.NONE);
   }
   
   @Override
   public boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action){
      if(index == 4){
         this.outboundMode = !this.outboundMode;
         this.buildPage();
      }
      return super.onAnyClick(index, type, action);
   }
   
   @Override
   public void buildPage(){
      items(DataAccess.getGlobal(MailStorage.KEY).getMailsForOrFrom(player));
      MailFilter.setData(this.outboundMode,player.getUUID());
      GuiHelper.outlineGUI(this, 0x1F44DD, Component.literal(""));
      
      if(this.outboundMode){
         setTitle(Component.translatable("gui.fabricmail.outbox_title"));
      }else{
         setTitle(Component.translatable("gui.fabricmail.inbox_title"));
      }
      
      ;
      GuiElementBuilder modeButton = new GuiElementBuilder(this.outboundMode ? Items.WRITABLE_BOOK : Items.WRITTEN_BOOK).hideDefaultTooltip();
      modeButton.setName(Component.translatable(this.outboundMode ? "gui.fabricmail.outbox_mode" : "gui.fabricmail.inbox_mode").withStyle(ChatFormatting.AQUA));
      modeButton.addLoreLine(Component.translatable(this.outboundMode ? "gui.fabricmail.showing_outbound" : "gui.fabricmail.showing_inbound").withStyle(ChatFormatting.DARK_PURPLE));
      modeButton.addLoreLine(Component.literal(""));
      modeButton.addLoreLine(Component.translatable("gui.fabricmail.toggle_mailbox_mode",
            Component.translatable("gui.borislib.click").withStyle(ChatFormatting.AQUA)
      ).withStyle(ChatFormatting.LIGHT_PURPLE));
      setSlot(4,modeButton);
      
      super.buildPage();
   }
   
   @Override
   public List<MailMessage> getFilteredSortedList(){
      return super.getFilteredSortedList().stream().filter(mail -> this.outboundMode == (mail.senderId().equals(player.getUUID()))).toList();
   }
   
   private static class MailSort extends GuiSort<MailMessage> {
      public static final List<MailSort> SORTS = new ArrayList<>();
      
      public static final MailSort RECENT_LAST = new MailSort("gui.fabricmail.recent_last", ChatFormatting.AQUA.getColor().intValue(),
            Comparator.comparingLong(MailMessage::timestamp));
      public static final MailSort RECENT_FIRST = new MailSort("gui.fabricmail.recent_first", ChatFormatting.GREEN.getColor().intValue(),
            Comparator.comparingLong(mail -> -mail.timestamp()));
      
      private MailSort(String key, int color, Comparator<MailMessage> comparator){
         super(key, color, comparator);
         SORTS.add(this);
      }
      
      @Override
      protected List<MailSort> getList(){
         return SORTS;
      }
      
      public MailSort getStaticDefault(){
         return RECENT_FIRST;
      }
   }
   
   private static class MailFilter extends GuiFilter<MailMessage> {
      public static final List<MailFilter> FILTERS = new ArrayList<>();
      private static boolean outbound = false;
      private static UUID player = AlgoUtils.getUUID(BorisLib.BLANK_UUID);
      
      public static final MailFilter NONE = new MailFilter("gui.borislib.none", ChatFormatting.WHITE.getColor().intValue(), entry -> isOutbound() == (entry.senderId().equals(getPlayer())));
      public static final MailFilter HAS_PARCEL = new MailFilter("gui.fabricmail.has_parcel", ChatFormatting.GREEN.getColor().intValue(), entry -> !entry.parcel().isEmpty() && isOutbound() == (entry.senderId().equals(getPlayer())));
      public static final MailFilter NO_PARCEL = new MailFilter("gui.fabricmail.no_parcel", ChatFormatting.RED.getColor().intValue(), entry -> entry.parcel().isEmpty() && isOutbound() == (entry.senderId().equals(getPlayer())));
      
      private MailFilter(String key, int color, Predicate<MailMessage> predicate){
         super(key, color, predicate);
         FILTERS.add(this);
      }
      
      @Override
      protected List<MailFilter> getList(){
         return FILTERS;
      }
      
      public MailFilter getStaticDefault(){
         return NONE;
      }
      
      public static boolean isOutbound(){
         return outbound;
      }
      
      public static UUID getPlayer(){
         return player;
      }
      
      public static void setData(boolean outbound, UUID uuid){
         MailFilter.outbound = outbound;
         MailFilter.player = uuid;
      }
   }
}
