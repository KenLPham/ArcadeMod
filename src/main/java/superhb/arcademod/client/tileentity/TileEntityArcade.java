package superhb.arcademod.client.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import superhb.arcademod.Reference;
import superhb.arcademod.client.audio.LoopingSound;

import javax.annotation.Nullable;
import java.util.ArrayList;

// TODO: Make use of Forge Energy API (RF)
//https://github.com/CJMinecraft01/BitOfEverything/blob/master/src/main/java/cjminecraft/bitofeverything/tileentity/TileEntityBlockBreaker.java
public class TileEntityArcade extends TileEntity implements ITickable {
	private int game = 0;
	
	// Sound Variables
	private SoundEvent soundEvent;
	private boolean isPlaying, shouldStart, shouldStop, loop;
	private float volume = 1f;
	
	// Leaderboard TODO: Leaderboard
	//private NBTTagList leaderboard;
	private ArcadeLeaderboard[] leaderboard;
	
	// Energy (Default 10 RF/tick)
	private EnergyStorage storage;
	
	// Multiplayer
	private ArrayList<String> playerList;
	
	public TileEntityArcade () {
		game = 0;
		storage = new EnergyStorage(5000, 1000, 0);
		leaderboard = new ArcadeLeaderboard[10];
		playerList = new ArrayList<>();
	}
	
	public TileEntityArcade (int game) {
		this.game = game;
		storage = new EnergyStorage(5000, 1000, 0);
		leaderboard = new ArcadeLeaderboard[10];
		playerList = new ArrayList<>();
	}
	
	public int getGameID () {
		return game;
	}
	
	public void setGameID (int id) {
		game = id;
	}
	
	public void saveLeaderboard (ArcadeLeaderboard[] newLeaderboard) {
		leaderboard = newLeaderboard;
	}
	
	public ArcadeLeaderboard[] getLeaderboard () {
		return leaderboard;
	}
	
	@Override
	public void update () {
		// Sound System
		// https://github.com/rykar/TheRealMcrafters-Siren-Mod/blob/master/main/java/mcrafter/SirenMod/sirens/nuclear/NuclearSirenTileEntity.java
		if (loop) {
//			if (!isPlaying && shouldStart) {
//				shouldStart = false;
//				shouldStop = false;
//				isPlaying = true;
//				LoopingSound sound = new LoopingSound((TileEntityArcade)world.getTileEntity(pos), soundEvent, SoundCategory.BLOCKS, volume);
//
//				if (getWorld().isRemote) Minecraft.getMinecraft().getSoundHandler().playSound(sound);
//			}
//			if (shouldStart) {
//				LoopingSound sound = new LoopingSound((TileEntityArcade)world.getTileEntity(pos), soundEvent, SoundCategory.BLOCKS, volume);
//				if (getWorld().isRemote) Minecraft.getMinecraft().getSoundHandler().playSound(sound);
//			}
		} else {
			// Play non looping sound
		}
		
		// Power System
		if (world != null && !world.isRemote) {
			if (!world.isBlockPowered(pos)) { // Not Powered
				// Do not emit light
			} else {
				// Emit light
			}
		}
	}
	
	private SoundEvent setSound (ResourceLocation resource) {
		return SoundEvent.REGISTRY.getObject(resource);
	}
	
	public void playSound (ResourceLocation resource, float volume, boolean looping) {
		loop = looping;
		shouldStart = true;
		setVolume(volume);
		this.soundEvent = setSound(resource);
	}
	
	public boolean shouldStop () {
		return shouldStart;
	}
	
	public void stop () {
		shouldStop = false;
	}
	
	public void setVolume (float volume) {
		this.volume = volume;
	}
	
	// Multiplayer
	public void addPlayer (String name) {
		playerList.add(name);
	}
	
	public void removePlayer (String name) {
		playerList.remove(name);
	}
	
	public void onPlayerJoin () {}
	public void onPlayerLeave () {}
	public void onGameStart () {}
	public void onGameEnd () {}
	
	@Override
	public NBTTagCompound writeToNBT (NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setInteger("Game", game);
        /*
        leaderboard = new NBTTagList();
        for (int i = 0; i < player.length; i++) {
            NBTTagCompound com = new NBTTagCompound();
            com.setString("Player", player[i]);
            com.setInteger("Score", score[i]);
            com.setString("Difficulty", difficulty[i]);
            leaderboard.appendTag(com);
        }
        */
		compound.setInteger("Energy", storage.getEnergyStored());
		//compound.setTag("Leaderboard", leaderboard);
		return compound;
	}
	
	@Override
	public void readFromNBT (NBTTagCompound compound) {
		super.readFromNBT(compound);
		game = compound.getInteger("Game");
        /*
        leaderboard = compound.getTagList("Leaderboard", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < leaderboard.tagCount(); i++) {
            player[i] = leaderboard.getCompoundTagAt(i).getString("Player");
            score[i] = leaderboard.getCompoundTagAt(i).getInteger("Score");
            difficulty[i] = leaderboard.getCompoundTagAt(i).getString("Difficulty");
        }
        */
		storage.receiveEnergy(compound.getInteger("Energy"), false);
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
	
	// Capability System
	@Override
	public <T> T getCapability (Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) return (T)storage;
		return super.getCapability(capability, facing);
	}
	
	@Override
	public boolean hasCapability (Capability<?> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY) return true;
		return super.hasCapability(capability, facing);
	}
}
