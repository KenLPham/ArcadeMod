package superhb.arcademod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import superhb.arcademod.Arcade;
import superhb.arcademod.util.ArcadePacketHandler;

import java.util.ArrayList;

public class ServerBuyMessage implements IMessage {
    private ItemStack stack;
    private ItemStack currency;
    private int cost;

    public ServerBuyMessage() {}

    public ServerBuyMessage(ItemStack stack, ItemStack currency, int amount, int cost) {
        this.stack = stack;
        this.stack.setCount(amount);
        this.currency = currency;
        this.cost = cost;
    }

    @Override
    public void toBytes (ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, stack);
        ByteBufUtils.writeItemStack(buf, currency);
        buf.writeInt(cost);
    }

    @Override
    public void fromBytes (ByteBuf buf) {
        try {
            stack = ByteBufUtils.readItemStack(buf);
            currency = ByteBufUtils.readItemStack(buf);
            cost = buf.readInt();
        } catch (Exception e) {
            Arcade.logger.info("Error: " + e);
        }
    }

    public ItemStack getStack () {
        return stack;
    }

    public ItemStack getCurrency () {
        return currency;
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
                    ArrayList<int[]> slotIndexes = new ArrayList<int[]>();
                    int totalCurrency = 0;

                    EntityPlayerMP player = context.getServerHandler().playerEntity;

                    if (player.inventory.hasItemStack(message.currency)) {

                        // Check each inventory slot for currency ItemStack
                        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                            if (player.inventory.getStackInSlot(i).getItem().equals(message.getCurrency().getItem())) {
                                totalCurrency += player.inventory.getStackInSlot(i).getCount();
                                slotIndexes.add(new int[] { i, player.inventory.getStackInSlot(i).getCount() }); // slotIndex, stackCount
                            }
                        }

                        // Remove Currency and Give ItemStack if player has enough
                        if (totalCurrency < message.getCost()) ArcadePacketHandler.INSTANCE.sendTo(new ClientBuyMessage(false), player);
                        else if (totalCurrency == message.getCost()) {
                            for (int[] i : slotIndexes) player.inventory.removeStackFromSlot(i[0]);
                            player.inventory.addItemStackToInventory(message.getStack());
                            ArcadePacketHandler.INSTANCE.sendTo(new ClientBuyMessage(true), player);
                        } else if (totalCurrency > message.getCost()) {
                            if (message.getCost() < 64) { // Less than 64
                                int useSlot = 0;
                                for (int i = 0; i < slotIndexes.size(); i++) {
                                    if (slotIndexes.get(i)[1] >= message.getCost()) useSlot = slotIndexes.get(i)[0];
                                }
                                player.inventory.decrStackSize(useSlot, message.cost);
                                player.inventory.addItemStackToInventory(message.getStack());
                            } else if (message.getCost() == 64) { // Equal to 64
                                int useSlot = 0;
                                for (int i = 0; i < slotIndexes.size(); i++) {
                                    if (slotIndexes.get(i)[1] >= message.getCost()) useSlot = slotIndexes.get(i)[0];
                                }
                                player.inventory.removeStackFromSlot(useSlot);
                                player.inventory.addItemStackToInventory(message.getStack());
                            } else if (message.getCost() > 64) { // More than 64
                                int[] useSlot;

                                int j = 0;

                                float stacksf = message.getCost() / 64.0F;
                                int stacks = message.getCost() / 64;
                                float remainder = (stacksf - stacks) * 64;

                                if (remainder == 0) useSlot = new int[stacks];
                                else useSlot = new int[stacks + 1];

                                for (int i = 0; i < slotIndexes.size(); i++) {
                                    if (j < useSlot.length) {
                                        if (slotIndexes.get(i)[1] == 64) useSlot[j] = slotIndexes.get(i)[0];
                                        else if (remainder > 0 && slotIndexes.get(i)[1] >= remainder) useSlot[j] = slotIndexes.get(i)[0];
                                        j++;
                                    }
                                }
                                for (int i = 0; i < useSlot.length; i++) {
                                    if (remainder == 0) player.inventory.decrStackSize(useSlot[i], 64);
                                    else {
                                        if (i < (useSlot.length - 1)) player.inventory.decrStackSize(useSlot[i], 64);
                                        else if (i < useSlot.length) player.inventory.decrStackSize(useSlot[i], (int)remainder);
                                    }
                                }
                                player.inventory.addItemStackToInventory(message.getStack());
                            }
                            ArcadePacketHandler.INSTANCE.sendTo(new ClientBuyMessage(true), player);
                        }
                    } else ArcadePacketHandler.INSTANCE.sendTo(new ClientBuyMessage(false), player);
                }
            });
            return null;
        }
    }
}
