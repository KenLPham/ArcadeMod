package superhb.arcademod.api.gui;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;

public class GuiSoundButton extends GuiButton {
    private SoundEvent sound;

    public GuiSoundButton(int id, int x, int y, int width, int height, String text, SoundEvent sound) {
        super(id, x, y, width, height, text);
        this.sound = sound;
    }

    public GuiSoundButton(int id, int x, int y, int width, int height, String text) {
        super(id, x, y, width, height, text);
        sound = SoundEvents.UI_BUTTON_CLICK;
    }

    @Override
    public void playPressSound (SoundHandler handler) {
        handler.playSound(PositionedSoundRecord.getMasterRecord(sound, 1.0F));
    }
}
