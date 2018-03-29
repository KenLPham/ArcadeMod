package superhb.arcademod.client.blocks;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.*;
import net.minecraft.block.state.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.fml.relauncher.*;
import superhb.arcademod.client.tileentity.TileEntityPlushie;
import superhb.arcademod.util.*;
import javax.annotation.Nullable;
import java.util.*;

// TODO: Redo Creeper Texture
// TODO: Change break particles depending on plushies
@SuppressWarnings("deprecation")
public class BlockPlushie extends Block implements IBlockVariant {
    // TODO: Diagonal rotation
    /** {@link net.minecraft.block.BlockBanner.BlockBannerStanding#withRotation(IBlockState, Rotation)} */
    private static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    private static final PropertyEnum ROTATION = PropertyEnum.create("rotation", EnumRotation.class);
    private static final PropertyEnum MOB = PropertyEnum.create("mob", EnumMob.class);

    // TODO: Rotations
    private static final AxisAlignedBB[][] boundingBox = { // [Mob][Rotation]
            { // Creeper TODO: Fix Bounding Box (Rotation Update)
                    new AxisAlignedBB((5.0D / 16.0D), 0.0D, (5.0D / 16.0D), (11.0D / 16.0D), (13.0D / 16.0D), (11.0D / 16.0D))
            },
            { // Pig TODO: Correct Bounding Box
                    new AxisAlignedBB((5.0D / 16.0D), 0.0D, (5.0D / 16.0D), (11.0D / 16.0D), (10.0D / 16.0D), (11.0D / 16.0D))
            }
    };

    private static final SoundEvent[] mobSounds = {
            SoundEvents.ENTITY_CREEPER_HURT,
            SoundEvents.ENTITY_PIG_AMBIENT
    };

    public BlockPlushie(Material material) {
        super(material);
        setHardness(0.8F);
        setSoundType(SoundType.CLOTH);
        setDefaultState(blockState.getBaseState().withProperty(MOB, EnumMob.CREEPER).withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    public boolean hasTileEntity (IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity (World world, IBlockState state) {
        return new TileEntityPlushie();
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
    
    /**
     * Get item to drop when harvested
     */
    @Override
    public Item getItemDropped (IBlockState state, Random rand, int fortune) {
		return null;
    }
    
    @Override
	public List<ItemStack> getDrops (IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		List<ItemStack> drops = super.getDrops(world, pos, state, fortune);
		TileEntityPlushie tile = world.getTileEntity(pos) instanceof TileEntityPlushie ? (TileEntityPlushie)world.getTileEntity(pos) : null;
		NBTTagCompound compound = new NBTTagCompound();
		ItemStack stack = new ItemStack(this);
		
		compound.setInteger("Mob", tile.getMobID());
		stack.setTagCompound(compound);
		
    	if (tile != null) drops.add(stack);
    	return drops;
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
	}

    @Override
    public boolean isReplaceable (IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox (IBlockState state, IBlockAccess source, BlockPos pos) {
        return boundingBox[0][0];
    }

    private AxisAlignedBB getBoundingBox (int mob, int rotation) {
        return boundingBox[mob][rotation];
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox (IBlockState state, World world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof TileEntityPlushie) {
            TileEntityPlushie plushie = (TileEntityPlushie)tile;
            return getBoundingBox(plushie.getMobID(), 0).offset(pos);
        }
        return state.getBoundingBox(world, pos).offset(pos);
    }

    /**
     * Called when a user uses the creative pick block button on this block
     */
    @Override
    public ItemStack getPickBlock (IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        TileEntity tile = world.getTileEntity(pos);
        NBTTagCompound compound = new NBTTagCompound();
        ItemStack stack = new ItemStack(this);
        compound.setInteger("Mob", 0);
        stack.setTagCompound(compound);

        if (tile instanceof TileEntityPlushie) {
            TileEntityPlushie plushie = (TileEntityPlushie)tile;
            compound.setInteger("Mob", plushie.getMobID());
            stack.setTagCompound(compound);
            return stack;
        }
        return stack;
    }

    @Override
    public IBlockState getActualState (IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof TileEntityPlushie) {
            TileEntityPlushie plushie = (TileEntityPlushie)tile;
            return state.withProperty(MOB, EnumMob.getValue(plushie.getMobID()));
        }
        return state;
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
        return new BlockStateContainer(this, new IProperty[] { FACING, MOB });
    }
    
    /**
     * Gets the metadata of the item this Block can drop.
     */
    @Override
    public int damageDropped (IBlockState state) {
        return 0;
    }
    
    @Override
    public void breakBlock (World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);
        world.removeTileEntity(pos);
    }

    @Override
    public IBlockState getStateForPlacement (World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return getStateFromMeta(meta).withProperty(FACING, placer.getHorizontalFacing());
    }

    @Override
    public boolean onBlockActivated (World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityPlushie) {
            TileEntityPlushie plushie = (TileEntityPlushie)tile;
            if (world.isRemote) world.playSound(player, pos, mobSounds[plushie.getMobID()], SoundCategory.BLOCKS, 1.0F, 1.0F); // TODO: Volume based on distance
        }
        return true;
    }

    @Override
    public String getVariantName (ItemStack stack) {
        if (stack.hasTagCompound()) return EnumMob.getName(stack.getTagCompound().getInteger("Mob"));
        return EnumMob.getName(0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks (CreativeTabs tab, NonNullList<ItemStack> list) {
        for (int i = 0; i < EnumMob.values().length; i++) {
            NBTTagCompound compound = new NBTTagCompound();
            ItemStack stack = new ItemStack(this);
            compound.setInteger("Mob", i);
            stack.setTagCompound(compound);
            list.add(stack);
        }
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
