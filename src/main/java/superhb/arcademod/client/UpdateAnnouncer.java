package superhb.arcademod.client;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.versioning.ComparableVersion;
import superhb.arcademod.Arcade;
import superhb.arcademod.Reference;

import java.util.Map;

public class UpdateAnnouncer {
    @SubscribeEvent
    public void onPlayerJoin (TickEvent.PlayerTickEvent event) {
        if (event.player.world.isRemote && event.player instanceof EntityPlayerSP) {
            EntityPlayerSP player = (EntityPlayerSP) event.player;

            // TODO: Get url from Reference
            ClickEvent openUrl = new ClickEvent(ClickEvent.Action.OPEN_URL, Reference.URL);
            TextComponentString download = new TextComponentString("[Download Latest]");

            if (Arcade.status == ForgeVersion.Status.OUTDATED || Arcade.status == ForgeVersion.Status.BETA_OUTDATED) {
                player.sendMessage(new TextComponentString(ChatFormatting.DARK_RED + "[" + Reference.NAME + "] Has an update available."));

                for (Map.Entry<ComparableVersion, String> entry : Arcade.changelog) {
                    player.sendMessage(new TextComponentString(ChatFormatting.BLUE + "[" + entry.getKey() + "] " + entry.getValue()));
                }
                player.sendMessage(download.setStyle(download.getStyle().setColor(TextFormatting.GREEN).setClickEvent(openUrl).setUnderlined(true)));
            }
            MinecraftForge.EVENT_BUS.unregister(this);
        }
    }
}
