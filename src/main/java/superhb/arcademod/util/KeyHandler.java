package superhb.arcademod.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;
import java.util.List;

public class KeyHandler {
	public static KeyBinding up, down, left, right, select;
	
	public static final List<KeyBinding> bindings;
	
	static {
		bindings = ImmutableList.of(
				up = new KeyBinding(I18n.format("control.arcademod:up.name"), Keyboard.KEY_UP, I18n.format("mod.arcademod:name.locale")),
				down = new KeyBinding(I18n.format("control.arcademod:down.name"), Keyboard.KEY_DOWN, I18n.format("mod.arcademod:name.locale")),
				left = new KeyBinding(I18n.format("control.arcademod:left.name"), Keyboard.KEY_LEFT, I18n.format("mod.arcademod:name.locale")),
				right = new KeyBinding(I18n.format("control.arcademod:right.name"), Keyboard.KEY_RIGHT, I18n.format("mod.arcademod:name.locale")),
				select = new KeyBinding(I18n.format("control.arcademod:select.name"), Keyboard.KEY_RETURN, I18n.format("mod.arcademod:name.locale"))
		);
	}
	
	public static void registerKeyBinding () {
		for (KeyBinding key : bindings) ClientRegistry.registerKeyBinding(key);
	}
}
