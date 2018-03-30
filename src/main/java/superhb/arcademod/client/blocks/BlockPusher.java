package superhb.arcademod.client.blocks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import superhb.arcademod.Arcade;
import superhb.arcademod.client.tileentity.TileEntityPusher;
import superhb.arcademod.util.EnumGame;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// TODO: Coin pusher machine
@SuppressWarnings("deprecation")
public class BlockPusher extends Block {
	public static final PropertyDirection FACING = BlockDirectional.FACING;
	public static final PropertyEnum GAME = PropertyEnum.create("game", EnumGame.class);
	
	public BlockPusher (Material material) {
		super(material);
	}
	
	@Override
	public boolean hasTileEntity (IBlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity (World world, IBlockState state) {
		return new TileEntityPusher();
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
		return false;
	}
	
	@Override
	public boolean canSpawnInBlock () {
		return false;
	}
	
	@Override
	public boolean isReplaceable (IBlockAccess world, BlockPos pos) {
		return false;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox (IBlockState state, IBlockAccess source, BlockPos pos) {
		//return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 16.0D, 32.0D, 16.0D);
		return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBox (IBlockState state, World world, BlockPos pos) {
		return state.getBoundingBox(world, pos).offset(pos);
	}
	
	@Override
	public IBlockState getActualState (IBlockState state, IBlockAccess world, BlockPos pos) {
		return state;
	}
	
	@Override
	public IBlockState getStateFromMeta (int meta) {
		return getDefaultState();
	}
	
	@Override
	public int getMetaFromState (IBlockState state) {
		return 0;
	}

    /*
    @Override
    protected BlockStateContainer createBlockState () {
        return new BlockStateContainer(this, new IProperty[] { FACING });
    }
    */
	
	@Override
	public boolean onBlockActivated (World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (world.isRemote) player.openGui(Arcade.instance, -2, world, pos.getX(), pos.getY(), pos.getZ());

        /*
        TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof TileEntityArcade) {
            TileEntityArcade arcade = (TileEntityArcade)tile;

            if (world.isRemote) player.openGui(Arcade.instance, arcade.getGameID(), world, pos.getX(), pos.getY(), pos.getZ());
        }
        */
		return true;
	}
	
	@Override
	public void breakBlock (World world, BlockPos pos, IBlockState state) {
		super.breakBlock(world, pos, state);
		world.removeTileEntity(pos);
	}
	
	@Override
	public boolean eventReceived (IBlockState state, World world, BlockPos pos, int eventId, int eventParam) {
		super.eventReceived(state, world, pos, eventId, eventParam);
		TileEntity tile = world.getTileEntity(pos);
		return tile == null ? false : tile.receiveClientEvent(eventId, eventParam);
	}
	
	@Override
	public EnumBlockRenderType getRenderType (IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}
}
