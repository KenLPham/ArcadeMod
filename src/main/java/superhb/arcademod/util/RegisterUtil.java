package superhb.arcademod.util;

import superhb.arcademod.content.ArcadeBlocks;
import superhb.arcademod.content.ArcadeItems;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import superhb.arcademod.content.items.ItemBlockArcade;

public class RegisterUtil {
    public static void registerAll (FMLPreInitializationEvent event) {
        registerItems(event, ArcadeItems.coin, ArcadeItems.ticket);
        registerBlocks(event, ArcadeBlocks.coinPusher, ArcadeBlocks.plushie, ArcadeBlocks.invisible);
        registerArcadeMachine(event, ArcadeBlocks.arcadeMachine);
    }

    private static void registerBlocks (FMLPreInitializationEvent event, Block... blocks) {
        for (Block block : blocks) {
            final ItemBlock item = new ItemBlock(block);

            if (event.getSide() == Side.CLIENT) {
                GameRegistry.register(block.setUnlocalizedName(block.getRegistryName().toString()));
                GameRegistry.register(item, block.getRegistryName());
                ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName(), "inventory"));
            }
        }
    }

    @Deprecated
    private static void registerBlockWithVariant (FMLPreInitializationEvent event, Block block, String variantType, String variantName) {
        final ItemBlock item = new ItemBlock(block);

        if (event.getSide() == Side.CLIENT) {
            GameRegistry.register(block.setUnlocalizedName(block.getRegistryName().toString()));
            GameRegistry.register(item, block.getRegistryName());
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName(), variantType + "=" + variantName));
        }
    }

    private static void registerArcadeMachine (FMLPreInitializationEvent event, Block block) {
        final ItemBlockArcade item = new ItemBlockArcade(block);

        if (event.getSide() == Side.CLIENT) {
            GameRegistry.register(block.setUnlocalizedName(block.getRegistryName().toString()));
            GameRegistry.register(item, block.getRegistryName());
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName(), "game"));

            //ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName() + "_" + EnumGame.getValue(0).getName(), "inventory"));
            //ModelLoader.registerItemVariants(item, new ModelResourceLocation(item.getRegistryName() + "_" + EnumGame.getValue(1).getName(), "inventory"));
        }
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
