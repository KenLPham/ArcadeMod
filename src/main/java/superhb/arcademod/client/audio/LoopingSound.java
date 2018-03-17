package superhb.arcademod.client.audio;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraftforge.fml.relauncher.*;
import superhb.arcademod.api.gui.GuiArcade;
import superhb.arcademod.client.tileentity.TileEntityArcade;

@SideOnly(Side.CLIENT)
public class LoopingSound extends MovingSound {
    private TileEntityArcade tile;

    public LoopingSound (TileEntityArcade tileEntity, SoundEvent sound, SoundCategory category, float volume) {
        super(sound, category);
        this.tile = tileEntity;
        this.attenuationType = AttenuationType.NONE;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = volume;
    }

    @Override
    public void update () {
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiArcade) || tile.shouldStop()) donePlaying = true;
    }
}
