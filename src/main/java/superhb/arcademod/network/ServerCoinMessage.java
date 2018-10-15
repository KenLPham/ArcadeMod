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

import java.util.ArrayList;

// TODO: Add coinMenu and mainMenu ID to constructor
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
        try {
            cost = buf.readInt();
            stack = ByteBufUtils.readItemStack(buf);
        } catch (Exception e) {
            Arcade.logger.info("Error: " + e);
        }
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
            IThreadListener thread = (WorldServer)context.getServerHandler().player.world;

            thread.addScheduledTask(()->{
				EntityPlayerMP serverPlayer = context.getServerHandler().player;
	
				ArrayList<int[]> slotIndexes = new ArrayList<>();
				int totalCount = 0;
				
				if (serverPlayer.inventory.hasItemStack(message.getStack())) {
					for (int i = 0; i < serverPlayer.inventory.getSizeInventory(); i++) {
						if (serverPlayer.inventory.getStackInSlot(i).getItem().equals(message.getStack().getItem())) {
							totalCount += serverPlayer.inventory.getStackInSlot(i).getCount();
							slotIndexes.add(new int[] { i, serverPlayer.inventory.getStackInSlot(i).getCount() });
						}
					}
					
					if (totalCount < message.getCost()) ArcadePacketHandler.INSTANCE.sendTo(new ClientCoinMessage(false, -1), serverPlayer);
					else if (totalCount == message.getCost()) {
						for (int[] i : slotIndexes) serverPlayer.inventory.removeStackFromSlot(i[0]);
						ArcadePacketHandler.INSTANCE.sendTo(new ClientCoinMessage(true, 0), serverPlayer);
					} else if (totalCount > message.getCost()) {
						if (message.getCost() < 64) {
							int useSlot = 0;
							for (int i = 0; i < slotIndexes.size(); i++) {
								if (slotIndexes.get(i)[1] >= message.getCost()) useSlot = slotIndexes.get(i)[0];
							}
							serverPlayer.inventory.decrStackSize(useSlot, message.getCost());
						} else if (message.getCost() == 64) {
							int useSlot = 0;
							for (int i = 0; i < slotIndexes.size(); i++) {
								if (slotIndexes.get(i)[1] >= message.getCost()) useSlot = slotIndexes.get(i)[0];
							}
							serverPlayer.inventory.removeStackFromSlot(useSlot);
						} else if (message.getCost() > 64) {
							int[] useSlot;
							int j = 0;
							
							float stacksf = message.getCost() / 64.0F;
							int stacks = message.getCost() / 64;
							float remainder = (stacksf - stacks) * 64;
							
							if (remainder == 0) useSlot = new int[stacks];
							else useSlot = new int[stacks + 1];
							
							for (int i = 0; i < slotIndexes.size(); i++) {
								if (j < useSlot.length) {
									if (slotIndexes.get(i)[i] == 64) useSlot[j] = slotIndexes.get(i)[0];
									else if (remainder > 0 && slotIndexes.get(i)[1] >= remainder) useSlot[j] = slotIndexes.get(i)[0];
									j++;
								}
							}
							for (int i = 0; i < useSlot.length; i++) {
								if (remainder == 0) serverPlayer.inventory.removeStackFromSlot(useSlot[i]);
								else {
									if (i < (useSlot.length - 1)) serverPlayer.inventory.removeStackFromSlot(useSlot[i]);
									else if (i < useSlot.length) serverPlayer.inventory.decrStackSize(useSlot[i], (int)remainder);
								}
							}
						}
						ArcadePacketHandler.INSTANCE.sendTo(new ClientCoinMessage(true, 0), serverPlayer);
					}
				} else ArcadePacketHandler.INSTANCE.sendTo(new ClientCoinMessage(false, -1), serverPlayer);
			});
            return null;
        }
    }
}
