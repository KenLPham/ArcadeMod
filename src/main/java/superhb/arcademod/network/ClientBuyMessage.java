package superhb.arcademod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ClientBuyMessage implements IMessage {
    public String msg;

    public ClientBuyMessage () {}

    public ClientBuyMessage (String message) {
        msg = message;
    }

    @Override
    public void toBytes (ByteBuf buf) {
        for (int i = 0; i < msg.length(); i++) buf.setChar(i, msg.charAt(i));
    }

    @Override
    public void fromBytes (ByteBuf buf) {
        for (int i = 0; i < buf.capacity(); i++) msg += (char)buf.getByte(i);
    }

    public String getMessage () {
        return msg;
    }

    public static class Handler implements IMessageHandler<ClientBuyMessage, IMessage> {
        @Override
        public IMessage onMessage (final ClientBuyMessage message, final MessageContext context) {
            IThreadListener thread = (WorldServer)context.getServerHandler().playerEntity.world;

            thread.addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    // TODO: Print msg into gui
                }
            });
            return null;
        }
    }
    /*
    if (message.getMenu() == -1) {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;

        if (screen instanceof GuiArcade) {
            GuiArcade arcade = (GuiArcade) screen;
            arcade.isEnoughCoins(message.isEnoughCoins());
        }
    } else {
        GuiScreen screen = Minecraft.getMinecraft().currentScreen;

        if (screen instanceof GuiArcade) {
            GuiArcade arcade = (GuiArcade) screen;
            arcade.menu = message.getMenu();
        }
    }
     */
}
