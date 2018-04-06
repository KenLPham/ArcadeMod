package superhb.arcademod.network.pong;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.awt.*;
import java.util.List;

public class ServerPongMessage implements IMessage {
	private BlockPos pos;
	private Point paddle, ball;
	private int paddleId, score, guiScreen, menu;
	private String playerName;
	private boolean disconnect;
	
	public ServerPongMessage () {}
	
	// Join
	public ServerPongMessage (BlockPos pos, int paddleId, EntityPlayer player) {
		this.pos = pos;
		this.paddleId = paddleId;
		this.playerName = player.getName();
	}
	
	@Override
	public void toBytes (ByteBuf buf) {
		buf.writeInt(pos.getX());
		buf.writeInt(pos.getY());
		buf.writeInt(pos.getZ());
		
		buf.writeInt(paddleId);
		
		ByteBufUtils.writeUTF8String(buf, playerName);
	}
	
	@Override
	public void fromBytes (ByteBuf buf) {
		pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
		paddleId = buf.readInt();
		playerName = ByteBufUtils.readUTF8String(buf);
	}
	
	public static class Handler implements IMessageHandler<ServerPongMessage, IMessage> {
		@Override
		public IMessage onMessage (final ServerPongMessage message, final MessageContext context) {
			IThreadListener thread = (WorldServer)context.getServerHandler().player.world;
			
			thread.addScheduledTask(() ->{
				List<EntityPlayer> players = context.getServerHandler().player.getServerWorld().playerEntities;
				
				for (EntityPlayer player : players) {
					//player
				}
			});
			return null;
		}
	}
}
