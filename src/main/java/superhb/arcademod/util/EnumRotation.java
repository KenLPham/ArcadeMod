package superhb.arcademod.util;

import net.minecraft.util.IStringSerializable;

public enum EnumRotation implements IStringSerializable {
    NORTH(0, 4, "north"),
    NORTHEAST(1, 5, "northeast"),
    EAST(2, 6, "east"),
    SOUTHEAST(3, 7, "southeast"),
    SOUTH(4, 0, "south"),
    SOUTHWEST(5, 1, "southwest"),
    WEST(6, 2, "west"),
    NORTHWEST(7, 3, "northwest");

    private final int index;
    private final String name;

    private EnumRotation(int index, int opposite, String name) {
        this.index = index;
        this.name = name;
    }

    @Override
    public String getName () {
        return name;
    }
}
