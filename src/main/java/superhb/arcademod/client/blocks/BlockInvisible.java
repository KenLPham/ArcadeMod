package superhb.arcademod.client.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import superhb.arcademod.client.ArcadeBlocks;
import superhb.arcademod.client.tileentity.TileEntityArcade;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Used for MultiBlock purposes only
// TODO: Make it easier to use with any block
// TODO: Show break progress on model
// TODO: Change break particles
@SuppressWarnings("deprecation")
public class BlockInvisible extends Block {
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    public BlockInvisible (Material material) {
        super(material);
        setHardness(1.0F);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        translucent = true;
    }

    @Override
    public boolean isFullCube (IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube (IBlockState state) {
        return false;
    }

    @Override
    public boolean isPassable (IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public boolean canSpawnInBlock () {
        return false;
    }

    @Override
    public boolean isReplaceable (IBlockAccess world, BlockPos pos) {
        return false;
    }
    
    /**
     * Get item to drop when harvested
     */
    @Override
    public Item getItemDropped (IBlockState state, Random rand, int fortune) {
        return null;
    }
    
    @Override
    public List<ItemStack> getDrops (IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
    	List<ItemStack> drops = new ArrayList<ItemStack>();
    	drops.add(new ItemStack(Blocks.AIR));
    	
        return drops;//world.getBlockState(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ())).getBlock().getDrops(world, new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ()), state, fortune);
    }
    
    @Override
    public boolean removedByPlayer (IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        if (willHarvest) return true; //If it will harvest, delay deletion of the block until after getDrops
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }
    
    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack tool) {
        super.harvestBlock(world, player, pos, state, te, tool);
        world.setBlockToAir(pos);
        world.getBlockState(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ())).getBlock().harvestBlock(world, player, new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ()), state, te, tool);
    }

    @Override
    public AxisAlignedBB getBoundingBox (IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(FACING)) {
            case NORTH:
                return new AxisAlignedBB((1.0D / 16.0D), 0.0D, 0.0D, (15.0D / 16.0D), 1.0D, (14.0D / 16.0D));
            case SOUTH:
                return new AxisAlignedBB((1.0D / 16.0D), 0.0D, (2.0D / 16.0D), (15.0D / 16.0D), 1.0D, 1.0D);
            case WEST:
                return new AxisAlignedBB(0.0D, 0.0D, (1.0D / 16.0D), (14.0D / 16.0D), 1.0D, (15.0D / 16.0D));
            case EAST:
                return new AxisAlignedBB((2.0D / 16.0D), 0.0D, (1.0D / 16.0D), 1.0D, 1.0D, (15.0D / 16.0D));
        }
        return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, (15.0D / 16.0D), (12.0D / 16.0D));
    }

    @Override
    public boolean hasTileEntity (IBlockState state) {
        return true;
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox (IBlockState state, World world, BlockPos pos) {
        return state.getBoundingBox(world, pos).offset(pos);
    }

    @Override
    public IBlockState getStateFromMeta (int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta));
    }

    @Override
    public int getMetaFromState (IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    protected BlockStateContainer createBlockState () {
        return new BlockStateContainer(this, new IProperty[] { FACING });
    }

    @Override
    public int damageDropped (IBlockState state) {
        return 0;
    }

    @Override
    public boolean onBlockActivated (World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return world.getBlockState(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ())).getBlock().onBlockActivated(world, new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ()), state, player, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public ItemStack getPickBlock (IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return world.getBlockState(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ())).getBlock().getPickBlock(state, target, world, new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ()), player);
    }

    @Override
    public void onBlockHarvested (World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        world.setBlockState(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ()), Blocks.AIR.getDefaultState(), 3);
    }

    @Override
    public void onBlockDestroyedByExplosion (World world, BlockPos pos, Explosion explosion) {
		world.setBlockToAir(pos);
		world.getBlockState(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ())).getBlock().onBlockDestroyedByExplosion(world, new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ()), explosion);
    }

    @Override
    public void onBlockDestroyedByPlayer (World world, BlockPos pos, IBlockState state) {
        world.setBlockToAir(pos);
        world.getBlockState(new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ())).getBlock().onBlockDestroyedByPlayer(world, new BlockPos(pos.getX(), pos.getY() - 1, pos.getZ()), state);
    }

    @Override
    public EnumBlockRenderType getRenderType (IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
    }
}
