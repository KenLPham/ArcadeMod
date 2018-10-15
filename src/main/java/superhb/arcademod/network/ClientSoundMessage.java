package superhb.arcademod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ClientSoundMessage implements IMessage {
	String soundName;
	
	public ClientSoundMessage () {}
	
	public ClientSoundMessage (ResourceLocation soundName) {
		this.soundName = soundName.getResourcePath();
	}
	
	@Override
	public void toBytes (ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, soundName);
	}
	
	@Override
	public void fromBytes (ByteBuf buf) {
		soundName = ByteBufUtils.readUTF8String(buf);
	}
	
	public String getSoundName () {
		return soundName;
	}
	
	public static class Handler implements IMessageHandler<ClientSoundMessage, IMessage> {
		@Override
		public IMessage onMessage (final ClientSoundMessage message, final MessageContext context) {
			IThreadListener thread;
			return null;
		}
	}
}
