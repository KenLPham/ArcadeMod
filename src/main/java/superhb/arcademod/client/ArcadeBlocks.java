package superhb.arcademod.client;

import jline.internal.Preconditions;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraft.block.Block;
import superhb.arcademod.Arcade;
import superhb.arcademod.Reference;
import superhb.arcademod.client.blocks.*;
import superhb.arcademod.client.items.*;
import java.util.HashSet;
import java.util.Set;

@ObjectHolder(Reference.MODID)
public class ArcadeBlocks {
	@ObjectHolder("arcade_machine")
	public static final BlockArcade ARCADE_MACHINE = null;
	
	@ObjectHolder("coin_pusher")
	public static final BlockPusher COIN_PUSHER = null;
	
	@ObjectHolder("plushie")
	public static final BlockPlushie PLUSHIE = null;
	
	@ObjectHolder("invisible")
	public static final BlockInvisible INVISIBLE = null;
	
	@ObjectHolder("prize_box")
	public static final BlockPrize PRIZE_BOX = null;
	
	@EventBusSubscriber
	public static class RegistrationHandler {
		public static final Set<ItemBlock> ITEM_BLOCKS = new HashSet<>();
		
		@SubscribeEvent
		public static void registerBlocks (RegistryEvent.Register<Block> event) {
			final Block[] blocks = {
					new BlockArcade(Material.ROCK).setRegistryName("arcade_machine").setCreativeTab(Arcade.tab),
					new BlockPusher(Material.ROCK).setRegistryName("coin_pusher"),//.setCreativeTab(Arcade.tab)
					new BlockPlushie(Material.CLOTH).setRegistryName("plushie").setCreativeTab(Arcade.tab),
					new BlockInvisible(Material.ROCK).setRegistryName("invisible"),
					new BlockPrize(Material.IRON).setRegistryName("prize_box").setCreativeTab(Arcade.tab)
			};
			
			for (Block block : blocks) event.getRegistry().registerAll(block.setUnlocalizedName(block.getRegistryName().toString()));
		}
		
		@SubscribeEvent
		public static void registerItemBlocks (RegistryEvent.Register<Item> event) {
			final ItemBlock[] items = {
					new ItemBlockArcade(ARCADE_MACHINE),
					new ItemBlock(COIN_PUSHER),
					new ItemBlockPlushie(PLUSHIE),
					new ItemBlock(INVISIBLE),
					new ItemBlock(PRIZE_BOX)
			};
			
			for (ItemBlock item : items) {
				final Block block = item.getBlock();
				event.getRegistry().register(item.setRegistryName(block.getRegistryName()));
				ITEM_BLOCKS.add(item);
			}
		}
	}
}
