package superhb.arcademod.util;

import net.minecraft.util.IStringSerializable;

public enum EnumMob implements IStringSerializable {
    // http://minecraft.gamepedia.com/Mob
    CREEPER(0, "creeper");

    private int id;
    private String name;

    EnumMob (int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getName () {
        return name;
    }

    public static EnumMob getValue (int id) {
        return values()[id];
    }

    public static String getName (int id) {
        return values()[id].name;
    }
}
