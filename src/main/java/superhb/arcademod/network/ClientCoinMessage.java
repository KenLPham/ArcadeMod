package superhb.arcademod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import superhb.arcademod.api.gui.GuiArcade;

public class ClientCoinMessage implements IMessage {
    private boolean enoughCoins = true;
    private int menu = -1;

    public ClientCoinMessage() {}

    public ClientCoinMessage(boolean enoughCoins, int menu) {
        this.enoughCoins = enoughCoins;
        this.menu = menu;
    }

    @Override
    public void toBytes (ByteBuf buf) {
        buf.writeInt(menu);
        buf.writeBoolean(enoughCoins);
    }

    @Override
    public void fromBytes (ByteBuf buf) {
        menu = buf.readInt();
        enoughCoins = buf.readBoolean();
    }

    public boolean isEnoughCoins () {
        return enoughCoins;
    }

    public int getMenu () {
        return menu;
    }

    public static class Handler implements IMessageHandler<ClientCoinMessage, IMessage> {
        @Override
        public IMessage onMessage (final ClientCoinMessage message, final MessageContext context) {
            IThreadListener thread = Minecraft.getMinecraft();

            thread.addScheduledTask(new Runnable() {
                @Override
                public void run() {
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
                }
            });
            return null;
        }
    }
}
