package com.andjie.dragonboatfestival.data;

import org.bukkit.Material;

public enum MaterialType {
    RICE("rice", "糯米", Material.SUGAR),
    LEAF("leaf", "粽叶", Material.LEAVES),
    JUJUBE("jujube", "红枣", Material.APPLE),
    MEAT("meat", "鲜肉", Material.PORK);

    private final String key;
    private final String displayName;
    private final Material itemMaterial;

    MaterialType(String key, String displayName, Material itemMaterial) {
        this.key = key;
        this.displayName = displayName;
        this.itemMaterial = itemMaterial;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Material getItemMaterial() {
        return itemMaterial;
    }

    public static MaterialType fromKey(String key) {
        for (MaterialType type : values()) {
            if (type.key.equalsIgnoreCase(key)) {
                return type;
            }
        }
        return null;
    }
}
