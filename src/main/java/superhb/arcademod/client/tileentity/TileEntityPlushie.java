package superhb.arcademod.client.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class TileEntityPlushie extends TileEntity {
    private int mob = 0;

    public TileEntityPlushie () {
        mob = 0;
    }

    public TileEntityPlushie (int mob) {
        this.mob = mob;
    }

    public int getMobID () {
        return mob;
    }

    public void setMobID (int id) {
        mob = id;
    }

    @Override
    public NBTTagCompound writeToNBT (NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("Mob", mob);
        return compound;
    }

    @Override
    public void readFromNBT (NBTTagCompound compound) {
        super.readFromNBT(compound);
        mob = compound.getInteger("Mob");
    }

    @Override
    public NBTTagCompound getUpdateTag () {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag (NBTTagCompound tag) {
        readFromNBT(tag);
    }

    @Override
    public void onDataPacket (NetworkManager net, SPacketUpdateTileEntity packet) {
        NBTTagCompound tag = packet.getNbtCompound();
        handleUpdateTag(tag);
    }

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket () {
        NBTTagCompound tag = getUpdateTag();
        final int meta = 0;

        return new SPacketUpdateTileEntity(pos, meta, tag);
    }

    @Override
    public boolean shouldRefresh (World world, BlockPos pos, IBlockState old, IBlockState newState) {
        if (world.isAirBlock(pos)) return true;
        return false;
    }
}
