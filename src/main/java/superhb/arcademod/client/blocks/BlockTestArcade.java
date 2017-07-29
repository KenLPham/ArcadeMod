package superhb.arcademod.client.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.World;
import superhb.arcademod.client.tileentity.TileEntityTestArcade;

// https://github.com/MinecraftForge/MinecraftForge/blob/f9c7caaf0cda38cdc18842d47e216a66f0c7c14e/src/test/java/net/minecraftforge/debug/ModelLoaderRegistryDebug.java
@SuppressWarnings("deprecation")
public class BlockTestArcade extends Block {
    public BlockTestArcade (Material material) {
        super(material);
        translucent = true;
    }

    @Override
    public boolean isOpaqueCube (IBlockState state) {
        return false;
    }

    @Override
    public boolean hasTileEntity (IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity (World world, IBlockState state) {
        return new TileEntityTestArcade();
    }

    @Override
    public EnumBlockRenderType getRenderType (IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }
}
