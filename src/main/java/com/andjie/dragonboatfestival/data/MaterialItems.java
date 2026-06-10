package com.andjie.dragonboatfestival.data;

import com.andjie.dragonboatfestival.Color;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MaterialItems {

    private static final String MARKER_PREFIX = ChatColor.DARK_GRAY + "DragonBoatFestival:";

    private MaterialItems() {
    }

    public static ItemStack create(MaterialType type, int amount) {
        ItemStack item = new ItemStack(type.getItemMaterial(), amount);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Color.text("&a端午材料：&e" + type.getDisplayName()));
        List<String> lore = new ArrayList<String>();
        lore.add(Color.text("&7用于制作端午粽子"));
        lore.add(MARKER_PREFIX + type.getKey());
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static void give(Player player, MaterialType type, int amount) {
        Map<Integer, ItemStack> remaining = player.getInventory().addItem(create(type, amount));
        for (ItemStack item : remaining.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
    }

    public static int count(Player player, MaterialType type) {
        int total = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (isMaterial(item, type)) {
                total += item.getAmount();
            }
        }
        return total;
    }

    public static boolean has(Player player, Map<MaterialType, Integer> cost) {
        for (Map.Entry<MaterialType, Integer> entry : cost.entrySet()) {
            if (count(player, entry.getKey()) < entry.getValue()) {
                return false;
            }
        }
        return true;
    }

    public static void take(Player player, Map<MaterialType, Integer> cost) {
        for (Map.Entry<MaterialType, Integer> entry : cost.entrySet()) {
            take(player, entry.getKey(), entry.getValue());
        }
        player.updateInventory();
    }

    private static void take(Player player, MaterialType type, int amount) {
        PlayerInventory inventory = player.getInventory();
        int remaining = amount;
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (!isMaterial(item, type)) {
                continue;
            }
            if (item.getAmount() <= remaining) {
                remaining -= item.getAmount();
                inventory.setItem(i, null);
            } else {
                item.setAmount(item.getAmount() - remaining);
                remaining = 0;
            }
        }
    }

    private static boolean isMaterial(ItemStack item, MaterialType type) {
        if (item == null || item.getType() != type.getItemMaterial() || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) {
            return false;
        }
        return meta.getLore().contains(MARKER_PREFIX + type.getKey());
    }
}
