package superhb.arcademod.util;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.item.ItemStack;
import superhb.arcademod.client.ArcadeBlocks;
import superhb.arcademod.client.ArcadeItems;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import superhb.arcademod.client.items.IItemMeshDefinition;
import superhb.arcademod.client.items.ItemBlockArcade;
import superhb.arcademod.client.items.ItemBlockPlushie;

public class RegisterUtil {
    public static void registerAll (FMLPreInitializationEvent event) {
        //registerBlockWithItem(event, ArcadeBlocks.arcadeMachine, new ItemBlockArcade(ArcadeBlocks.arcadeMachine), "inventory");
        registerItemMeshDefinition(event, new ItemBlockArcade(ArcadeBlocks.arcadeMachine), ArcadeBlocks.arcadeMachine);


        registerItems(event, ArcadeItems.coin, ArcadeItems.ticket);
        registerBlocks(event, ArcadeBlocks.invisible, ArcadeBlocks.prizeBox); //ArcadeBlocks.coinPusher, ArcadeBlocks.testArcade
        //registerArcadeMachine(event, ArcadeBlocks.arcadeMachine);
        registerPlushie(event, ArcadeBlocks.plushie);
    }

    private static void registerBlocks (FMLPreInitializationEvent event, Block... blocks) {
        for (Block block : blocks) {
            final ItemBlock item = new ItemBlock(block);

            if (event.getSide() == Side.CLIENT) registerModelVariant(block, item, "inventory");
        }
    }

    private static void registerModelVariant (Block block, Item item, String variantType) {
        GameRegistry.register(block.setUnlocalizedName(block.getRegistryName().toString()));
        GameRegistry.register(item, block.getRegistryName());
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName(), variantType));
    }

    private static void registerBlockWithItem (FMLPreInitializationEvent event, Block block, ItemBlock item, String variant) {
        if (event.getSide() == Side.CLIENT) registerModelVariant(block, item, variant);
    }

    private static <T extends IItemMeshDefinition> void registerItemMeshDefinition (FMLPreInitializationEvent event, T item, Block block) {
        if (event.getSide() == Side.CLIENT) {
            GameRegistry.register(block.setUnlocalizedName(block.getRegistryName().toString()));
            GameRegistry.register((Item)item, block.getRegistryName());

            ItemMeshDefinition def = item.getMeshDefinition();
            ModelLoader.setCustomMeshDefinition((Item)item, def);
            ModelBakery.registerItemVariants((Item)item, def.getModelLocation(new ItemStack((Item)item)));
        }
    }

    @Deprecated
    private static void registerArcadeMachine (FMLPreInitializationEvent event, Block block) {
        final ItemBlockArcade item = new ItemBlockArcade(block);
        if (event.getSide() == Side.CLIENT) registerModelVariant(block, item, "game");
    }

    @Deprecated
    private static void registerPlushie (FMLPreInitializationEvent event, Block block) {
        final ItemBlockPlushie item = new ItemBlockPlushie(block);
        if (event.getSide() == Side.CLIENT) registerModelVariant(block, item, "mob");
    }

    private static void registerItems (FMLPreInitializationEvent event, Item... items) {
        for (Item item : items) {
            if (event.getSide() == Side.CLIENT) {
                GameRegistry.register(item.setUnlocalizedName(item.getRegistryName().toString()));
                ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            }
        }
    }
}
