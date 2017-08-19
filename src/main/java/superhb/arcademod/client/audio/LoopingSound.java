package superhb.arcademod.client.audio;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.*;
import net.minecraftforge.fml.relauncher.*;
import superhb.arcademod.api.gui.GuiArcade;

@SideOnly(Side.CLIENT)
public class LoopingSound extends MovingSound {
    public LoopingSound (SoundEvent sound, SoundCategory category) {
        super(sound, category);
        this.attenuationType = AttenuationType.NONE;
        this.repeat = true;
        this.repeatDelay = 0;
    }

    @Override
    public void update () {
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiArcade)) donePlaying = true;
    }
}
