package superhb.arcademod.util;

import net.minecraft.util.IStringSerializable;

public enum EnumGame implements IStringSerializable {
	SNAKE(0, "snake"),
	TETROMINOES(1, "tetrominoes"),
	PACMAN(2, "pacman"),
	PONG(3, "pong"),
	KONG(4, "kong");
	
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
	
	public int getId () {
		return id;
	}
	
	public static EnumGame getValue (int id) {
		return values()[id];
	}
	
	public static String getRegistryName (int id) {
		return values()[id].registryName;
	}
}
