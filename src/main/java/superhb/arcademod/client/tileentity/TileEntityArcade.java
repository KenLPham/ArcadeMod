package superhb.arcademod.client.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

// TODO: Move to client package
public class TileEntityArcade extends TileEntity {
    private int game = 0;
    private NBTTagList leaderboard;
    private String player[] = { "", "", "", "", "", "", "", "", "", "" }, difficulty[] = { "", "", "", "", "", "", "", "", "", "" };
    private int score[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

    public TileEntityArcade () {
        game = 0;
    }

    public TileEntityArcade (int game) {
        this.game = game;
    }

    public int getGameID () {
        return game;
    }

    public void setGameID (int id) {
        game = id;
    }

    public void addToLeaderboard (int place, String name, int score, String difficulty) {
        player[place] = name;
        this.score[place] = score;
        this.difficulty[place] = difficulty;
    }

    public NBTTagList getLeaderboard () {
        return leaderboard;
    }

    @Override
    public NBTTagCompound writeToNBT (NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("Game", game);
        leaderboard = new NBTTagList();
        for (int i = 0; i < player.length; i++) {
            NBTTagCompound com = new NBTTagCompound();
            com.setString("Player", player[i]);
            com.setInteger("Score", score[i]);
            com.setString("Difficulty", difficulty[i]);
            leaderboard.appendTag(com);
        }
        compound.setTag("Leaderboard", leaderboard);
        return compound;
    }

    @Override
    public void readFromNBT (NBTTagCompound compound) {
        super.readFromNBT(compound);
        game = compound.getInteger("Game");
        leaderboard = compound.getTagList("Leaderboard", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < leaderboard.tagCount(); i++) {
            player[i] = leaderboard.getCompoundTagAt(i).getString("Player");
            score[i] = leaderboard.getCompoundTagAt(i).getInteger("Score");
            difficulty[i] = leaderboard.getCompoundTagAt(i).getString("Difficulty");
        }
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
