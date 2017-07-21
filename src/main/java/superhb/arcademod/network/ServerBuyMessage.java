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
import superhb.arcademod.client.ArcadeItems;
import superhb.arcademod.util.ArcadePacketHandler;

// TODO: Client Buy Packet
public class ServerBuyMessage implements IMessage {
    private ItemStack stack;
    private ItemStack currency;
    private int amount;
    private int cost;

    public ServerBuyMessage() {}

    public ServerBuyMessage(ItemStack stack, ItemStack currency, int amount, int cost) {
        this.stack = stack;
        this.currency = currency;
        this.amount = amount;
        this.cost = cost;
    }

    @Override
    public void toBytes (ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, stack);
        ByteBufUtils.writeItemStack(buf, currency);
        buf.writeInt(amount);
        buf.writeInt(cost);
    }

    @Override
    public void fromBytes (ByteBuf buf) {
        stack = ByteBufUtils.readItemStack(buf);
        currency = ByteBufUtils.readItemStack(buf);
        amount = buf.readInt();
        cost = buf.readInt();
    }

    public ItemStack getStack () {
        return stack;
    }

    public ItemStack getCurrency () {
        return currency;
    }

    public int getAmount () {
        return amount;
    }

    public int getCost () {
        return cost;
    }

    public static class Handler implements IMessageHandler<ServerBuyMessage, IMessage> {
        @Override
        public IMessage onMessage (final ServerBuyMessage message, final MessageContext context) {
            IThreadListener thread = (WorldServer)context.getServerHandler().playerEntity.world;

            thread.addScheduledTask(new Runnable() {
                @Override
                public void run() {
                    EntityPlayerMP player = context.getServerHandler().playerEntity;
                    if (player.inventory.hasItemStack(message.currency)) {
                        int totalTickets = 0;
                        // TODO: Check if player has enough tickets before giving prize.
                        // TODO: Take amount of tickets needed
                        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                            if (player.inventory.getStackInSlot(i).getItem() == ArcadeItems.ticket) {
                                totalTickets += player.inventory.getStackInSlot(i).getCount();
                                // TODO: Store slot index
                            }
                        }
                        if (totalTickets < message.getCost()) ArcadePacketHandler.INSTANCE.sendTo(new ClientBuyMessage("Not Enough Tickets!"), player);
                        else if (totalTickets == message.getCost()) {
                            // TODO: Remove all stacks
                            player.inventory.addItemStackToInventory(message.getStack());
                        } else if (totalTickets > message.getCost()) {
                            // TODO: Remove amount
                            player.inventory.addItemStackToInventory(message.getStack());
                        }
                    } else {
                        ArcadePacketHandler.INSTANCE.sendTo(new ClientBuyMessage("No Tickets!"), player);
                    }
                }
            });
            return null;
        }
    }
}
