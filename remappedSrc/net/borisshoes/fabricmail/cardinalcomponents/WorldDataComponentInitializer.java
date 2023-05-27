package net.borisshoes.fabricmail.cardinalcomponents;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import net.minecraft.util.Identifier;

public class WorldDataComponentInitializer implements WorldComponentInitializer {
   public static final ComponentKey<IMailComponent> MAILS = ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier("fabricmail", "mail"), IMailComponent.class);
   
   @Override
   public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry){
      registry.register(MAILS, MailComponent.class, world -> new MailComponent());
   }
}