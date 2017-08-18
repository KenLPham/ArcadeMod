package superhb.arcademod.util;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.obj.OBJLoader;
import superhb.arcademod.Reference;
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
        registerArcadeMachine(event, ArcadeBlocks.arcadeMachine);



        registerItems(event, ArcadeItems.coin, ArcadeItems.ticket);
        registerBlocks(event, ArcadeBlocks.invisible); //ArcadeBlocks.coinPusher
        registerBlocksWithOBJModel(event, ArcadeBlocks.prizeBox);
        registerPlushie(event, ArcadeBlocks.plushie);
    }

    // Block Registry Functions
    private static void registerBlocks (FMLPreInitializationEvent event, Block... blocks) {
        for (Block block : blocks) {
            final ItemBlock item = new ItemBlock(block);

            if (event.getSide() == Side.CLIENT) registerModelVariant(item, block, "inventory");
        }
    }

    private static void registerBlocksWithOBJModel (FMLPreInitializationEvent event, Block... blocks) {
        OBJLoader.INSTANCE.addDomain(Reference.MODID);
        registerBlocks(event, blocks);
    }

    private static void registerModelVariant (Item item, Block block, String variantType) {
        registerItemBlock(item, block);
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName(), variantType));
    }

    private static void registerBlockWithItem (FMLPreInitializationEvent event, Item item, Block block, String variant) {
        if (event.getSide() == Side.CLIENT) registerModelVariant(item, block, variant);
    }

    @Deprecated
    private static void registerArcadeMachine (Item item, Block block) {
        registerItemBlock(item, block);
        OBJLoader.INSTANCE.addDomain(Reference.MODID);

        if (item instanceof IItemMeshDefinition) {
            ModelLoader.setCustomMeshDefinition(item, ((IItemMeshDefinition)item).getMeshDefinition());
            for (EnumGame g : EnumGame.values()) {
                for (int i = 2; i < 6; i++) ModelBakery.registerItemVariants(item, new ModelResourceLocation(block.getRegistryName(), "facing=" + EnumFacing.values()[i].getName() + ",game=" + g.getName()));
            }
        }
    }

    @Deprecated
    private static void registerPlushie (Item item, Block block) {
        registerItemBlock(item, block);
        if (item instanceof IItemMeshDefinition) {
            ModelLoader.setCustomMeshDefinition(item, ((IItemMeshDefinition)item).getMeshDefinition());
            for (EnumMob m : EnumMob.values()) {
                for (int i = 2; i < 6; i++) ModelBakery.registerItemVariants(item, new ModelResourceLocation(block.getRegistryName(), "facing=" + EnumFacing.values()[i].getName() + ",mob=" + m.getName()));
            }
        }
    }

    private static void registerBlockWavefrontModelVariant (Item item, Block block, String variantType) {
        registerItemBlock(item, block);
        OBJLoader.INSTANCE.addDomain(Reference.MODID);
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(block.getRegistryName(), variantType));
    }

    private static void registerBlockWavefrontModel (Item item, Block block) {
        registerItemBlock(item, block);
        OBJLoader.INSTANCE.addDomain(Reference.MODID);
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(block.getRegistryName(), "inventory"));
    }

    @Deprecated
    private static void registerArcadeMachine (FMLPreInitializationEvent event, Block block) {
        final ItemBlockArcade item = new ItemBlockArcade(block);
        if (event.getSide() == Side.CLIENT) registerArcadeMachine(item, block);
    }

    @Deprecated
    private static void registerPlushie (FMLPreInitializationEvent event, Block block) {
        final ItemBlockPlushie item = new ItemBlockPlushie(block);
        if (event.getSide() == Side.CLIENT) registerPlushie(item, block);
    }

    private static void registerBlock (Block block) {
        GameRegistry.register(block.setUnlocalizedName(block.getRegistryName().toString()));
    }

    // Item Registry Functions
    private static void registerItem (Item item) {
        GameRegistry.register(item.setUnlocalizedName(item.getRegistryName().toString()));
    }

    private static void registerItemBlock (Item item, Block block) {
        registerBlock(block);
        GameRegistry.register(item, block.getRegistryName());
    }

    private static void registerItems (FMLPreInitializationEvent event, Item... items) {
        for (Item item : items) {
            if (event.getSide() == Side.CLIENT) {
                registerItem(item);
                ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            }
        }
    }
}
