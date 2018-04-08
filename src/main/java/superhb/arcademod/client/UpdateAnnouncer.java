package superhb.arcademod.client;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.common.*;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.versioning.ComparableVersion;
import net.minecraftforge.fml.relauncher.Side;
import superhb.arcademod.*;

import java.util.Map;

@EventBusSubscriber(value = Side.CLIENT, modid = Reference.MODID)
public class UpdateAnnouncer {
	@SubscribeEvent
	public void onPlayerJoin (TickEvent.PlayerTickEvent event) {
		ClickEvent openUrl = new ClickEvent(ClickEvent.Action.OPEN_URL, Reference.URL);
		TextComponentString download = new TextComponentString("[Download Latest]");
		
		if (event.side == Side.CLIENT) {
			if (Arcade.status == ForgeVersion.Status.OUTDATED || Arcade.status == ForgeVersion.Status.BETA_OUTDATED) {
				event.player.sendMessage(new TextComponentString(ChatFormatting.DARK_RED + "[" + Reference.NAME + "] Has an update available."));
				
				for (Map.Entry<ComparableVersion, String> entry : Arcade.changelog) {
					TextComponentString change = new TextComponentString("[" + entry.getKey() + "]\n" + entry.getValue());
					
					event.player.sendMessage(change.setStyle(change.getStyle().setColor(TextFormatting.BLUE)));
				}
				event.player.sendMessage(download.setStyle(download.getStyle().setColor(TextFormatting.GREEN).setClickEvent(openUrl).setUnderlined(true)));
			}
		}
		MinecraftForge.EVENT_BUS.unregister(this);
	}
}
