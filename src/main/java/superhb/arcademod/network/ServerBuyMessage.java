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
import superhb.arcademod.client.ArcadeItems;
import superhb.arcademod.util.ArcadePacketHandler;

import java.util.ArrayList;

// TODO: Client Buy Packet
public class ServerBuyMessage implements IMessage {
    private ItemStack stack;
    private ItemStack currency;
    private int cost;

    public ServerBuyMessage() {}

    public ServerBuyMessage(Item item, ItemStack currency, int amount, int cost) {
        this.stack = new ItemStack(item, amount);
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
        stack = ByteBufUtils.readItemStack(buf);
        currency = ByteBufUtils.readItemStack(buf);
        cost = buf.readInt();
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
                    player.inventory.addItemStackToInventory(message.getStack());

                    if (player.inventory.hasItemStack(message.currency)) {
                        Arcade.logger.info("Has Currency ItemStack");

                        // Check each inventory slot for currency ItemStack
                        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                            if (player.inventory.getStackInSlot(i).getItem().equals(message.getCurrency().getItem())) {
                                Arcade.logger.info(String.format("Stack in Slot [%d] has currency ItemStack with [%d] in the stack", i, player.inventory.getStackInSlot(i).getCount()));
                                totalCurrency += player.inventory.getStackInSlot(i).getCount();
                                Arcade.logger.info(String.format("Total Currency [%d]", totalCurrency));
                                slotIndexes.add(new int[] { i, player.inventory.getStackInSlot(i).getCount() }); // slotIndex, stackCount
                            }
                        }

                        // Remove Currency and Give ItemStack if player has enough
                        if (totalCurrency < message.getCost()) Arcade.logger.info("Not enough currency!");
                        else if (totalCurrency == message.getCost()) {
                            Arcade.logger.info("Just enough");
                            for (int[] i : slotIndexes) {
                                player.inventory.removeStackFromSlot(i[0]);
                            }
                            player.inventory.addItemStackToInventory(message.getStack());
                        } else if (totalCurrency > message.getCost()) {
                            Arcade.logger.info("Player has more than enough!");
                            // TODO: Prize that cost more than 64 and equal to 64
                            if (message.getCost() < 64) {
                                Arcade.logger.info("Cost less than 64");
                                int useSlot = 0;
                                for (int i = 0; i < slotIndexes.size(); i++) {
                                    if (slotIndexes.get(i)[1] >= message.getCost()) useSlot = slotIndexes.get(i)[0];
                                    else Arcade.logger.info(String.format("Slot [%d] only has [%d] currency", slotIndexes.get(i)[0], slotIndexes.get(i)[1]));
                                }
                                Arcade.logger.info(String.format("Using Slot [%d]", useSlot));
                                player.inventory.decrStackSize(useSlot, message.cost);
                                Arcade.logger.info(message.getStack().getDisplayName());
                                player.inventory.addItemStackToInventory(message.getStack());
                                Arcade.logger.info("Took currency and gave stack");
                            } else if (message.getCost() == 64) {
                                Arcade.logger.info("Cost is equal to 64");
                            } else if (message.getCost() > 64) {
                                Arcade.logger.info("Cost more than 64");
                            }
                        }
                    } else Arcade.logger.info("Player doesn't have any currency");
                }
            });
            return null;
        }
    }
}
