package superhb.arcademod.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import superhb.arcademod.Arcade;

public class RewardMessage implements IMessage {
    private ItemStack reward;

    public RewardMessage () {}

    public RewardMessage (ItemStack stack) {
        reward = stack;
    }

    public RewardMessage (Item item, int amount, int meta, NBTTagCompound compound) {
        ItemStack reward = new ItemStack(item, amount, meta);
        reward.setTagCompound(compound);
        this.reward = reward;
    }

    public RewardMessage (Item item, int amount, int meta) {
        reward = new ItemStack(item, amount, meta);
    }

    @Override
    public void toBytes (ByteBuf buf) {
        ByteBufUtils.writeItemStack(buf, reward);
    }

    @Override
    public void fromBytes (ByteBuf buf) {
        try  {
            reward = ByteBufUtils.readItemStack(buf);
        } catch (Exception e) {
            Arcade.logger.info("Error: " + e);
        }
    }

    public ItemStack getReward () {
        return reward;
    }

    public static class Handler implements IMessageHandler<RewardMessage, IMessage> {
        @Override
        public IMessage onMessage (final RewardMessage message, final MessageContext context) {
            IThreadListener thread = (WorldServer)context.getServerHandler().player.world;

            thread.addScheduledTask(()->{
				EntityPlayerMP player = context.getServerHandler().player;
				player.inventory.addItemStackToInventory(message.getReward());
			});
            return null;
        }
    }
}
