package net.borisshoes.fabricmail.cardinalcomponents;

import net.minecraft.server.level.ServerLevel;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.world.WorldComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.world.WorldComponentInitializer;
import net.minecraft.resources.Identifier;

public class WorldDataComponentInitializer implements WorldComponentInitializer {
   public static final ComponentKey<IMailComponent> MAILS = ComponentRegistryV3.INSTANCE.getOrCreate(Identifier.fromNamespaceAndPath("fabricmail", "mail"), IMailComponent.class);
   
   @Override
   public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry){
      registry.registerFor(ServerLevel.OVERWORLD,MAILS, MailComponent.class, world -> new MailComponent());
   }
}