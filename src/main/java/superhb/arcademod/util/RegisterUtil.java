package superhb.arcademod.util;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import superhb.arcademod.Reference;
import superhb.arcademod.client.*;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;
import superhb.arcademod.client.items.*;

@EventBusSubscriber
public class RegisterUtil {
	@SubscribeEvent
	public static void registerItems (RegistryEvent.Register<Item> event) {
		registerItems(event.getRegistry(), ArcadeItems.coin, ArcadeItems.ticket);
		registerItemBlock(event.getRegistry(), ArcadeBlocks.prizeBox);
		registerArcadeItemBlock(event.getRegistry(), ArcadeBlocks.arcadeMachine);
		registerPlushieItemBlock(event.getRegistry(), ArcadeBlocks.plushie);
	}
	
	@SubscribeEvent
	public static void registerModels (ModelRegistryEvent event) {
		registerModels(ArcadeItems.coin, ArcadeItems.ticket);
		registerOBJBlocks(ArcadeBlocks.prizeBox);
		registerArcadeModel(ArcadeBlocks.arcadeMachine);
		registerPlushiesModel(ArcadeBlocks.plushie);
	}
	
	@SubscribeEvent
	public static void registerBlocks (RegistryEvent.Register<Block> event) {
		registerBlocks(event.getRegistry(), ArcadeBlocks.arcadeMachine, ArcadeBlocks.prizeBox, ArcadeBlocks.invisible, ArcadeBlocks.plushie, ArcadeBlocks.coinPusher);
	}
	
	private static void registerItems (IForgeRegistry<Item> registry, Item... items) {
		for (Item item : items) registry.register(item.setUnlocalizedName(item.getRegistryName().toString()));
	}
	
	private static void registerModels (Item... items) {
		for (Item item : items) registerModel(item.setUnlocalizedName(item.getRegistryName().toString()));
	}
	
	private static void registerModel (Item item) {
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
	}
	
	private static void registerModelVariant (Block block, String variantType) {
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName(), variantType));
	}
	
	private static void registerItemBlock (IForgeRegistry<Item> registry, Block block) {
		ItemBlock item = new ItemBlock(block);
		registry.register(item.setRegistryName(block.getRegistryName().toString()));
	}
	
	private static void registerPlushieItemBlock (IForgeRegistry<Item> registry, Block block) {
		ItemBlockPlushie item = new ItemBlockPlushie(block);
		registry.register(item.setRegistryName(block.getRegistryName().toString()));
	}
	
	private static void registerArcadeItemBlock (IForgeRegistry<Item> registry, Block block) {
		ItemBlockArcade item = new ItemBlockArcade(block);
		registry.register(item.setRegistryName(block.getRegistryName().toString()));
	}
	
	private static void registerBlocks (IForgeRegistry<Block> registry, Block... blocks) {
		for (Block block : blocks) registry.register(block.setUnlocalizedName(block.getRegistryName().toString()));
	}
	
	private static void registerOBJBlocks (Block... blocks) {
		for (Block block : blocks) {
			OBJLoader.INSTANCE.addDomain(Reference.MODID);
			registerModelVariant(block, "inventory");
		}
	}
	
	private static void registerArcadeModel (Block block) {
		final ItemBlockArcade item = new ItemBlockArcade(block);
		
		OBJLoader.INSTANCE.addDomain(Reference.MODID);
		if (item instanceof IItemMeshDefinition) {
			ModelLoader.setCustomMeshDefinition(item, ((IItemMeshDefinition)item).getMeshDefinition());
			for (EnumGame g : EnumGame.values()) {
				for (int i = 2; i < 6; i++) ModelBakery.registerItemVariants(item, new ModelResourceLocation(block.getRegistryName(), "facing=" + EnumFacing.values()[i].getName() + ",game=" + g.getName()));
			}
		}
	}
	
	private static void registerPlushiesModel (Block block) {
		final ItemBlockPlushie item = new ItemBlockPlushie(block);
		
		if (item instanceof IItemMeshDefinition) {
			ModelLoader.setCustomMeshDefinition(item, ((IItemMeshDefinition)item).getMeshDefinition());
			for (EnumMob m : EnumMob.values()) {
				for (int i = 2; i < 6; i++) {
					ModelBakery.registerItemVariants(item, new ModelResourceLocation(block.getRegistryName(), "facing=" + EnumFacing.values()[i].getName() + ",mob=" + m.getName()));
				}
			}
		}
	}
}
