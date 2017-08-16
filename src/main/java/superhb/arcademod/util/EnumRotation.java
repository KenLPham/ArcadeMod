package superhb.arcademod.util;

import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.Vec3i;

public enum EnumRotation implements IStringSerializable {
    NORTH(0, 4, "north", new Vec3i(0, 0, -1)),
    NORTHEAST(1, 5, "northeast", new Vec3i(1, 0, -1)),
    EAST(2, 6, "east", new Vec3i(1, 0, 0)),
    SOUTHEAST(3, 7, "southeast", new Vec3i(1, 0, 1)),
    SOUTH(4, 0, "south", new Vec3i(0, 0, 1)),
    SOUTHWEST(5, 1, "southwest", new Vec3i(-1, 0, 1)),
    WEST(6, 2, "west", new Vec3i(-1, 0, 0)),
    NORTHWEST(7, 3, "northwest", new Vec3i(-1, 0, 1));

    private final int index;
    private final int opposite;
    private final String name;
    private final Vec3i direction;

    private EnumRotation(int index, int opposite, String name, Vec3i direction) {
        this.index = index;
        this.opposite = opposite;
        this.name = name;
        this.direction = direction;
    }

    @Override
    public String getName () {
        return name;
    }
}
