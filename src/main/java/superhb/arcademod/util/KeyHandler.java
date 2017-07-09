package superhb.arcademod.util;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class KeyHandler {
    public static KeyBinding up, down, left, right, select;

    public static void preInit () {
        up = new KeyBinding("Up", Keyboard.KEY_UP, "Arcade Mod");
        down = new KeyBinding("Down", Keyboard.KEY_DOWN, "Arcade Mod");
        left = new KeyBinding("Left", Keyboard.KEY_LEFT, "Arcade Mod");
        right = new KeyBinding("Right", Keyboard.KEY_RIGHT, "Arcade Mod");
        select = new KeyBinding("Select", Keyboard.KEY_RETURN, "Arcade Mod");
        registerKeyBinding(up, down, left, right, select);
    }

    private static void registerKeyBinding (KeyBinding... keys) {
        for (KeyBinding key : keys) ClientRegistry.registerKeyBinding(key);
    }
}
