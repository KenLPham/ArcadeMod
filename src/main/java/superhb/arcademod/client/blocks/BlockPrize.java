package superhb.arcademod.client.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import superhb.arcademod.Arcade;
import superhb.arcademod.tileentity.TileEntityPrize;

// TODO: Design
@SuppressWarnings("deprecation")
public class BlockPrize extends Block {
    public BlockPrize(Material material) {
        super(material);
        setHardness(1.0F);
    }

    @Override
    public boolean hasTileEntity (IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity (World world, IBlockState state) {
        return new TileEntityPrize();
    }

    @Override
    public void breakBlock (World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);
        world.removeTileEntity(pos);
    }

    @Override
    public void onBlockPlacedBy (World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityPrize) {
            TileEntityPrize prize = (TileEntityPrize)tile;
            prize.setCustomName(stack.getDisplayName());
        }
    }

    // TODO: If crouched don't open gui
    @Override
    public boolean onBlockActivated (World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (world.isRemote) player.openGui(Arcade.instance, -1, world, pos.getX(), pos.getY(), pos.getZ());
        return true;
    }

    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer () {
        return BlockRenderLayer.SOLID;
    }

    @Override
    public boolean isOpaqueCube (IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube (IBlockState state) {
        return true;
    }

    @Override
    public EnumBlockRenderType getRenderType (IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }
}
