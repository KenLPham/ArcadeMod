package superhb.arcademod.client.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import superhb.arcademod.tileentity.TileEntityArcade;
import superhb.arcademod.tileentity.TileEntityPlushie;
import superhb.arcademod.util.EnumMob;

import java.util.Random;

@SuppressWarnings("deprecation")
public class BlockPlushie extends Block implements IBlockVariant {
    // TODO: Diagonal rotation
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyEnum MOB = PropertyEnum.create("mob", EnumMob.class);
    public static final AxisAlignedBB[] boundingBox = {
        new AxisAlignedBB((5.0D / 16.0D), 0.0D, (5.0D / 16.0D), (11.0D / 16.0D), (13.0D / 16.0D), (11.0D / 16.0D)) // Creeper
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

    @Override
    public Item getItemDropped (IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(this);
    }

    // TODO: Different bounding box for different rotation?
    @Override
    public AxisAlignedBB getBoundingBox (IBlockState state, IBlockAccess source, BlockPos pos) {
        return new AxisAlignedBB((5.0D / 16.0D), 0.0D, (5.0D / 16.0D), (11.0D / 16.0D), (13.0D / 16.0D), (11.0D / 16.0D));
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox (IBlockState state, World world, BlockPos pos) {
        return state.getBoundingBox(world, pos).offset(pos);
    }

    @Override
    public ItemStack getPickBlock (IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        TileEntity tile = world.getTileEntity(pos);
        NBTTagCompound compound = new NBTTagCompound();
        ItemStack stack = new ItemStack(this);
        compound.setInteger("Mob", 0);
        stack.setTagCompound(compound);

        if (tile instanceof TileEntityArcade) {
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

        if (tile instanceof TileEntityArcade) {
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

    // TODO: If crouched don't play sound
    @Override
    public boolean onBlockActivated (World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        // TODO: Play sound
        if (!world.isRemote) world.playSound(player, pos, SoundEvents.ENTITY_CREEPER_HURT, SoundCategory.BLOCKS, 100, 0); // TODO: pitch?
        return true;
    }

    @Override
    public String getVariantName (ItemStack stack) {
        if (stack.hasTagCompound()) return EnumMob.getName(stack.getTagCompound().getInteger("Mob"));
        return EnumMob.getName(0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks (Item item, CreativeTabs tab, NonNullList<ItemStack> list) {
        NBTTagCompound creeper = new NBTTagCompound();
        ItemStack creeperStack = new ItemStack(item);
        creeper.setInteger("Mob", 0);
        creeperStack.setTagCompound(creeper);
        list.add(creeperStack);
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
