package superhb.arcademod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import superhb.arcademod.client.gui.GuiPrize;

public class ClientBuyMessage implements IMessage {
    public boolean isEnough;

    public ClientBuyMessage () {}

    public ClientBuyMessage (boolean isEnough) {
        this.isEnough = isEnough;
    }

    @Override
    public void toBytes (ByteBuf buf) {
        buf.writeBoolean(isEnough);
    }

    @Override
    public void fromBytes (ByteBuf buf) {
        isEnough = buf.readBoolean();
    }

    public boolean isEnough () {
        return isEnough;
    }

    public static class Handler implements IMessageHandler<ClientBuyMessage, IMessage> {
        @Override
        public IMessage onMessage (final ClientBuyMessage message, final MessageContext context) {
            IThreadListener thread = Minecraft.getMinecraft();

            thread.addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    GuiScreen screen = Minecraft.getMinecraft().currentScreen;

                    if (screen instanceof GuiPrize) {
                        GuiPrize prize = (GuiPrize)screen;
                        prize.isEnough(message.isEnough());
                    }
                }
            });
            return null;
        }
    }
}
