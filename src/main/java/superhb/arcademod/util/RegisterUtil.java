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
    // Generic Registry Functions
    public static void registerAll (FMLPreInitializationEvent event) {
        // Blocks
        registerBlocks(event, ArcadeBlocks.invisible, ArcadeBlocks.coinPusher);
        registerOBJBlocks(event, ArcadeBlocks.prizeBox);
        registerArcadeMachines(event, ArcadeBlocks.arcadeMachine);
        registerPlushies(event, ArcadeBlocks.plushie);


        // Items
        registerItems(event, ArcadeItems.coin, ArcadeItems.ticket);
    }

    private static void registerModelVariant (Item item, Block block, String variantType) {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation(block.getRegistryName(), variantType));
    }

    // Block Registry Functions
    private static void registerBlocks (FMLPreInitializationEvent event, Block... blocks) {
        for (Block block : blocks) {
            final ItemBlock item = new ItemBlock(block);

            registerItemBlock(item, block);
            if (event.getSide().isClient()) registerModelVariant(item, block, "inventory");
        }
    }

    private static void registerItemBlock (Item item, Block block) {
        registerBlock(block);
        GameRegistry.register(item, block.getRegistryName());
    }

    private static void registerBlock (Block block) {
        GameRegistry.register(block.setUnlocalizedName(block.getRegistryName().toString()));
    }

    private static void registerOBJBlocks (FMLPreInitializationEvent event, Block... blocks) {
        for (Block block : blocks) {
            final ItemBlock item = new ItemBlock(block);
            registerItemBlock(item, block);

            if (event.getSide().isClient()) {
                OBJLoader.INSTANCE.addDomain(Reference.MODID);
                registerModelVariant(item, block, "inventory");
            }
        }
    }

    // TODO: Register Arcade Machines with RegisterUtil#registerOBJBlocks instead
    @Deprecated
    private static void registerArcadeMachines (FMLPreInitializationEvent event, Block arcadeMachine) {
        final ItemBlockArcade item = new ItemBlockArcade(arcadeMachine);
        registerItemBlock(item, arcadeMachine);

        if (event.getSide().isClient()) {
            OBJLoader.INSTANCE.addDomain(Reference.MODID);
            if (item instanceof IItemMeshDefinition) {
                ModelLoader.setCustomMeshDefinition(item, ((IItemMeshDefinition)item).getMeshDefinition());
                for (EnumGame g : EnumGame.values()) {
                    for (int i = 2; i < 6; i++) ModelBakery.registerItemVariants(item, new ModelResourceLocation(arcadeMachine.getRegistryName(), "facing=" + EnumFacing.values()[i].getName() + ",game=" + g.getName()));
                }
            }
        }
    }

    // TODO: Register Arcade Machines with RegisterUtil#registerOBJBlocks instead
    @Deprecated
    private static void registerPlushies (FMLPreInitializationEvent event, Block plushie) {
        final ItemBlockPlushie item = new ItemBlockPlushie(plushie);
        registerItemBlock(item, plushie);

        if (event.getSide().isClient()) {
            if (item instanceof IItemMeshDefinition) {
                ModelLoader.setCustomMeshDefinition(item, ((IItemMeshDefinition) item).getMeshDefinition());
                for (EnumMob m : EnumMob.values()) {
                    for (int i = 2; i < 6; i++) ModelBakery.registerItemVariants(item, new ModelResourceLocation(plushie.getRegistryName(), "facing=" + EnumFacing.values()[i].getName() + ",mob=" + m.getName()));
                }
            }
        }
    }

    // Item Registry Functions
    private static void registerItems (FMLPreInitializationEvent event, Item... items) {
        for (Item item : items) {
            GameRegistry.register(item.setUnlocalizedName(item.getRegistryName().toString()));
            if (event.getSide().isClient()) ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
    }
}
