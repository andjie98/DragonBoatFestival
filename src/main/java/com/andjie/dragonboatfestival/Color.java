package com.andjie.dragonboatfestival;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public final class Color {

    private Color() {
    }

    public static String text(String value) {
        return ChatColor.translateAlternateColorCodes('&', value == null ? "" : value);
    }

    public static List<String> list(List<String> values) {
        List<String> result = new ArrayList<String>();
        for (String value : values) {
            result.add(text(value));
        }
        return result;
    }
}
