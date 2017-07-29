package superhb.arcademod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import superhb.arcademod.Arcade;
import superhb.arcademod.util.ArcadePacketHandler;

public class ServerCoinMessage implements IMessage {
    private int cost;
    private ItemStack stack;

    public ServerCoinMessage() {}

    public ServerCoinMessage(ItemStack stack, int cost) {
        this.cost = cost;
        this.stack = stack;
    }

    @Override
    public void toBytes (ByteBuf buf) {
        buf.writeInt(cost);
        ByteBufUtils.writeItemStack(buf, stack);
    }

    @Override
    public void fromBytes (ByteBuf buf) {
        cost = buf.readInt();
        stack = ByteBufUtils.readItemStack(buf);
    }

    public int getCost () {
        return cost;
    }

    public ItemStack getStack () {
        return stack;
    }

    public static class Handler implements IMessageHandler<ServerCoinMessage, IMessage> {
        @Override
        public IMessage onMessage (final ServerCoinMessage message, final MessageContext context) {
            IThreadListener thread = (WorldServer)context.getServerHandler().playerEntity.world;

            thread.addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    EntityPlayerMP serverPlayer = context.getServerHandler().playerEntity;

                    // TODO: Do check for costs above 64
                    if (serverPlayer.inventory.hasItemStack(message.getStack())) {
                        ItemStack coin = serverPlayer.inventory.getStackInSlot(serverPlayer.inventory.getSlotFor(message.getStack()));
                        int slot = serverPlayer.inventory.getSlotFor(message.getStack());
                        if (coin.getCount() > message.getCost()){
                            serverPlayer.inventory.decrStackSize(slot, message.getCost());
                            ArcadePacketHandler.INSTANCE.sendTo(new ClientCoinMessage(true, 0), serverPlayer);
                        } else if (coin.getCount() == message.getCost()) {
                            serverPlayer.inventory.setInventorySlotContents(slot, ItemStack.EMPTY);
                            ArcadePacketHandler.INSTANCE.sendTo(new ClientCoinMessage(true, 0), serverPlayer);
                        } else ArcadePacketHandler.INSTANCE.sendTo(new ClientCoinMessage(false, -1), serverPlayer);
                    } else ArcadePacketHandler.INSTANCE.sendTo(new ClientCoinMessage(false, -1), serverPlayer);
                }
            });
            return null;
        }
    }
}
