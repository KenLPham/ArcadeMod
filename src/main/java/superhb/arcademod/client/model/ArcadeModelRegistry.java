package superhb.arcademod.client.model;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import superhb.arcademod.Reference;
import superhb.arcademod.client.ArcadeBlocks;
import superhb.arcademod.client.ArcadeItems;
import superhb.arcademod.client.blocks.BlockArcade;
import superhb.arcademod.client.blocks.BlockPlushie;
import superhb.arcademod.client.items.IItemMeshDefinition;
import superhb.arcademod.util.EnumGame;
import superhb.arcademod.util.EnumMob;

import java.util.HashSet;
import java.util.Set;

@EventBusSubscriber(value = Side.CLIENT, modid = Reference.MODID)
public class ArcadeModelRegistry {
	public static final ArcadeModelRegistry INSTANCE = new ArcadeModelRegistry();
	
	private final Set<Item> itemsRegistered = new HashSet<>();
	
	private ArcadeModelRegistry () {}
	
	@SubscribeEvent
	public static void registerModels (ModelRegistryEvent event) {
		OBJLoader.INSTANCE.addDomain(Reference.MODID);
		INSTANCE.registerBlockModels();
		INSTANCE.registerItemModels();
	}
	
	private final StateMapperBase propertyStringMapper = new StateMapperBase() {
		@Override
		protected ModelResourceLocation getModelResourceLocation (IBlockState state) {
			return new ModelResourceLocation("minecraft:air");
		}
	};
	
	private void registerItemModels () {
		registerItemModels(ArcadeItems.COIN, ArcadeItems.TICKET);
	}
	
	private void registerBlockModels () {
		registerBlockItemModel(ArcadeBlocks.PRIZE_BOX.getDefaultState());
		registerBlockWithVariantAndFacing(ArcadeBlocks.ARCADE_MACHINE.getDefaultState().withProperty(BlockArcade.GAME, EnumGame.SNAKE).withProperty(BlockArcade.FACING, EnumFacing.NORTH), BlockArcade.GAME, BlockArcade.FACING);
		registerBlockWithVariantAndFacing(ArcadeBlocks.PLUSHIE.getDefaultState().withProperty(BlockPlushie.MOB, EnumMob.CREEPER).withProperty(BlockPlushie.FACING, EnumFacing.NORTH), BlockPlushie.MOB, BlockPlushie.FACING);
	}
	
	private <T extends Comparable<T>> void registerBlockWithVariantAndFacing (IBlockState state, IProperty<T> property, IProperty<T> facing) {
		for (T values : property.getAllowedValues()) {
			for (T face : facing.getAllowedValues()) registerBlockItemModelVariant(state.withProperty(property, values).withProperty(facing, face));
		}
	}
	
	private void registerBlockItemModel (IBlockState state) {
		Item item = Item.getItemFromBlock(state.getBlock());
		registerItemModel(item, new ModelResourceLocation(state.getBlock().getRegistryName(), "inventory"));
	}
	
	private void registerBlockItemModelVariant (IBlockState state) {
		Item item = Item.getItemFromBlock(state.getBlock());
		registerItemModelVariant(item, new ModelResourceLocation(state.getBlock().getRegistryName(), propertyStringMapper.getPropertyString(state.getProperties())));
	}
	
	private void registerItemModels (Item... items) {
		for (Item item : items) registerItemModel(item);
	}
	
	private void registerItemModel (Item item) {
		registerItemModel(item, item.getRegistryName().toString());
	}
	
	private void registerItemModel (Item item, String registryName) {
		registerItemModel(item, new ModelResourceLocation(registryName, "inventory"));
	}
	
	private void registerItemModelVariant (Item item, String variant) {
		registerItemModelVariant(item, new ModelResourceLocation(item.getRegistryName(), variant));
	}
	
	private void registerItemModelVariant (Item item, ModelResourceLocation location) {
		itemsRegistered.add(item);
		if (item instanceof IItemMeshDefinition) registerItemModel(item, ((IItemMeshDefinition)item).getMeshDefinition());
		ModelBakery.registerItemVariants(item, location);
	}
	
	private void registerItemModel (Item item, ModelResourceLocation location) {
		ModelLoader.setCustomModelResourceLocation(item, 0, location);
	}
	
	private void registerItemModel (Item item, ItemMeshDefinition mesh) {
		ModelLoader.setCustomMeshDefinition(item, mesh);
	}
}
