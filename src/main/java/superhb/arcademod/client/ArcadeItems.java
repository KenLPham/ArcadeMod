package superhb.arcademod.client;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import superhb.arcademod.Arcade;
import net.minecraft.item.Item;
import superhb.arcademod.Reference;

import java.util.HashSet;
import java.util.Set;

@ObjectHolder(Reference.MODID)
public class ArcadeItems {
	@ObjectHolder("coin")
	public static final Item COIN = null;
	
	@ObjectHolder("ticket")
	public static final Item TICKET = null;
	
	@EventBusSubscriber
	public static class RegistrationHandler {
		public static final Set<Item> ITEMS = new HashSet<>();
		
		@SubscribeEvent
		public static void registerItems (RegistryEvent.Register<Item> event) {
			final Item[] items = {
				new Item().setRegistryName("coin").setCreativeTab(Arcade.tab),
				new Item().setRegistryName("ticket").setCreativeTab(Arcade.tab)
			};
			
			for (Item item : items) {
				event.getRegistry().register(item.setUnlocalizedName(item.getRegistryName().toString()));
				ITEMS.add(item);
			}
		}
	}
}
