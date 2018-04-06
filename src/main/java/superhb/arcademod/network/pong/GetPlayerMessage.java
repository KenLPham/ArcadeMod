/* Pong Packets
 * When player selects the 'Multiplayer' option, this packet will be called
 * to get all players, and check what screen the other players are on.
 */

package superhb.arcademod.network.pong;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import superhb.arcademod.client.gui.GuiPong;

public class GetPlayerMessage implements IMessage {
	private BlockPos pos;
	
	public GetPlayerMessage () {}
	
	public GetPlayerMessage (BlockPos pos) {
		this.pos = pos;
	}
	
	@Override
	public void toBytes (ByteBuf buf) {
		buf.writeInt(pos.getX());
		buf.writeInt(pos.getY());
		buf.writeInt(pos.getZ());
	}
	
	@Override
	public void fromBytes (ByteBuf buf) {
		pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
	}
	
	public BlockPos getPos () {
		return pos;
	}
	
	public static class Handler implements IMessageHandler<GetPlayerMessage, IMessage> {
		@Override
		public IMessage onMessage (final GetPlayerMessage message, final MessageContext context) {
			IThreadListener thread = Minecraft.getMinecraft();
			
			thread.addScheduledTask(()->{
				GuiScreen screen = Minecraft.getMinecraft().currentScreen;
				
				if (screen instanceof GuiPong) {
				
				}
			});
			return null;
		}
	}
}
