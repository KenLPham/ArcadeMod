package superhb.arcademod.network.pong;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.awt.*;

public class ClientPongMessage implements IMessage {
	private BlockPos pos;
	private Point paddle, ball;
	private int paddleId, score, guiScreen, menu;
	private String playerName;
	private boolean disconnect;
	
	public ClientPongMessage () {}
	
	// Join
	public ClientPongMessage (BlockPos pos, int paddleId, EntityPlayer player) {
		this.pos = pos;
		this.paddleId = paddleId;
		this.playerName = player.getName();
	}
	
	// Playing
	public ClientPongMessage (Point paddle, Point ball, int score) {
		this.paddle = paddle;
		this.ball = ball;
		this.score = score;
	}
	
	// Disconnect
	public ClientPongMessage (boolean disonnect) {}
	
	public ClientPongMessage (int menu) {}
	
	@Override
	public void toBytes (ByteBuf buf) {
		if (pos != null) {
			buf.writeInt(pos.getX());
			buf.writeInt(pos.getY());
			buf.writeInt(pos.getZ());
		}
		
		if (paddle != null) {
			buf.writeInt(paddle.x);
			buf.writeInt(paddle.y);
		}
		
		if (ball != null) {
			buf.writeInt(ball.x);
			buf.writeInt(ball.y);
		}
		
		buf.writeInt(paddleId);
		
		buf.writeInt(score);
		
		buf.writeInt(guiScreen);
		
		buf.writeInt(menu);
		
		if (playerName != null) ByteBufUtils.writeUTF8String(buf, playerName);
		
		buf.writeBoolean(disconnect);
	}
	
	@Override
	public void fromBytes (ByteBuf buf) {}
	
	public static class Handler implements IMessageHandler<ClientPongMessage, IMessage> {
		@Override
		public IMessage onMessage (final ClientPongMessage message, final MessageContext context) {
			IThreadListener thread = Minecraft.getMinecraft();
			
			thread.addScheduledTask(()->{
			});
			return null;
		}
	}
}
