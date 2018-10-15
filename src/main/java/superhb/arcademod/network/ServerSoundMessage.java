package superhb.arcademod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import superhb.arcademod.Reference;
import superhb.arcademod.client.tileentity.TileEntityArcade;

public class ServerSoundMessage implements IMessage {
	private String soundName;
	private int x, y, z;
	private float volume;
	private boolean loop, play;
	
	public ServerSoundMessage () {}
	
	// Send ResourceLocation domain too?
	public ServerSoundMessage (ResourceLocation resource, BlockPos pos, float volume, boolean loop, boolean play) {
		soundName = resource.getResourcePath();
		x = pos.getX();
		y = pos.getY();
		z = pos.getZ();
		this.volume = volume;
		this.loop = loop;
		this.play = play;
	}
	
	public ServerSoundMessage (String name, BlockPos pos, float volume, boolean loop, boolean play) {
		soundName = name;
		x = pos.getX();
		y = pos.getY();
		z = pos.getZ();
		this.volume = volume;
		this.loop = loop;
		this.play = play;
	}
	
	@Override
	public void toBytes (ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, soundName);
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		buf.writeFloat(volume);
		buf.writeBoolean(loop);
		buf.writeBoolean(play);
	}
	
	@Override
	public void fromBytes (ByteBuf buf) {
		soundName = ByteBufUtils.readUTF8String(buf);
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
		volume = buf.readFloat();
		loop = buf.readBoolean();
		play = buf.readBoolean();
	}
	
	public ResourceLocation getResourceLocation () {
		return new ResourceLocation(Reference.MODID, soundName);
	}
	
	public BlockPos getBlockPos () {
		return new BlockPos(x, y, z);
	}
	
	public boolean isLooping () {
		return loop;
	}
	
	public boolean play () {
		return play;
	}
	
	public float getVolume () {
		return volume;
	}
	
	public static class Handler implements IMessageHandler<ServerSoundMessage, IMessage> {
		@Override
		public IMessage onMessage (final ServerSoundMessage message, final MessageContext context) {
			IThreadListener thread = (WorldServer)context.getServerHandler().player.world;
			
			thread.addScheduledTask(()->{
				World world = context.getServerHandler().player.world;
				TileEntity entity = world.getTileEntity(message.getBlockPos());
				if (entity instanceof TileEntityArcade) {
					TileEntityArcade arcade = (TileEntityArcade)world.getTileEntity(message.getBlockPos());
					if (message.play()) arcade.playSound(message.getResourceLocation(), message.getVolume(), message.isLooping());
					else arcade.stop();
				}
				
			});
			return null;
		}
	}
}
