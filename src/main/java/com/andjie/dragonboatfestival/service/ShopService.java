package com.andjie.dragonboatfestival.service;

import com.andjie.dragonboatfestival.Color;
import com.andjie.dragonboatfestival.DragonBoatFestivalPlugin;
import com.andjie.dragonboatfestival.data.PlayerData;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ShopService {

    private final DragonBoatFestivalPlugin plugin;
    private List<ShopItem> items;

    public ShopService(DragonBoatFestivalPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        items = new ArrayList<ShopItem>();
        ConfigurationSection section = plugin.getShopConfig().getConfigurationSection("items");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            if (!isValidKey(key)) {
                plugin.getLogger().warning("已跳过非法商店商品ID: " + key);
                continue;
            }
            String path = "items." + key + ".";
            Material material = Material.matchMaterial(plugin.getShopConfig().getString(path + "material", "STONE"));
            if (material == null) {
                material = Material.STONE;
            }
            int points = plugin.getShopConfig().getInt(path + "points", 0);
            if (points < 1) {
                plugin.getLogger().warning("已跳过价格无效的商店商品: " + key + " (points=" + points + ")");
                continue;
            }
            double money = Math.max(0D, plugin.getShopConfig().getDouble(path + "money", 0D));
            items.add(new ShopItem(
                key,
                Color.text(plugin.getShopConfig().getString(path + "name", key)),
                material,
                points,
                money,
                plugin.getShopConfig().getStringList(path + "commands")));
        }
    }

    public List<String> keys() {
        List<String> keys = new ArrayList<String>();
        for (ShopItem item : items) {
            keys.add(item.key);
        }
        return keys;
    }

    public boolean buy(Player player, String key) {
        ShopItem item = findItem(key);
        if (item == null) {
            player.sendMessage(Color.text("&c商品不存在。"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
            return false;
        }
        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data.getPoints() < item.points) {
            player.sendMessage(plugin.message("shop-not-enough"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
            return false;
        }
        data.addPoints(-item.points);
        if (item.money > 0) {
            if (plugin.getVaultHook() == null || !plugin.getVaultHook().isEnabled()) {
                data.addPoints(item.points);
                player.sendMessage(Color.text("&c兑换失败：服务器未接入 Vault 经济。"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                return false;
            }
            if (!plugin.getVaultHook().deposit(player, item.money)) {
                data.addPoints(item.points);
                player.sendMessage(Color.text("&c兑换失败：经济奖励发放失败，请联系管理员。"));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                return false;
            }
        }
        data.addShopPurchase();
        plugin.getGoalManager().addProgress("shop-purchase", 1);
        for (String command : item.commands) {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command.replace("{player}", player.getName()));
        }
        player.sendMessage(plugin.message("shop-bought").replace("{name}", item.name));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
        return true;
    }

    private boolean isValidKey(String key) {
        return key != null && key.matches("[A-Za-z0-9_-]+");
    }

    private ShopItem findItem(String key) {
        for (ShopItem item : items) {
            if (item.key.equalsIgnoreCase(key)) {
                return item;
            }
        }
        return null;
    }

    private static class ShopItem {

        private final String key;
        private final String name;
        private final Material material;
        private final int points;
        private final double money;
        private final List<String> commands;

        private ShopItem(String key, String name, Material material, int points, double money, List<String> commands) {
            this.key = key;
            this.name = name;
            this.material = material;
            this.points = points;
            this.money = money;
            this.commands = commands;
        }
    }
}
