package net.borisshoes.fabricmail.cardinalcomponents;

import net.minecraft.server.world.ServerWorld;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.world.WorldComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.world.WorldComponentInitializer;
import net.minecraft.util.Identifier;

public class WorldDataComponentInitializer implements WorldComponentInitializer {
   public static final ComponentKey<IMailComponent> MAILS = ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier("fabricmail", "mail"), IMailComponent.class);
   
   @Override
   public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry){
      registry.registerFor(ServerWorld.OVERWORLD,MAILS, MailComponent.class, world -> new MailComponent());
   }
}