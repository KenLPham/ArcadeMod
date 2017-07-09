package superhb.arcademod.util;

import net.minecraft.util.IStringSerializable;

public enum EnumGame implements IStringSerializable {
    SNAKE(0, "snake"),
    TETRIS(1, "tetris");
    //PACMAN(2, "pacman");

    private int id;
    private String registryName;

    EnumGame (int id, String registryName) {
        this.id = id;
        this.registryName = registryName;
    }

    @Override
    public String getName () {
        return registryName;
    }

    public static EnumGame getValue (int id) {
        return values()[id];
    }

    public static String getRegistryName (int id) {
        return values()[id].registryName;
    }
}
