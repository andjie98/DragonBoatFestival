package com.andjie.dragonboatfestival.service;

import com.andjie.dragonboatfestival.Color;
import com.andjie.dragonboatfestival.DragonBoatFestivalPlugin;
import com.andjie.dragonboatfestival.data.MaterialItems;
import com.andjie.dragonboatfestival.data.MaterialType;
import com.andjie.dragonboatfestival.data.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ZongziService implements Listener {

    private final DragonBoatFestivalPlugin plugin;

    public ZongziService(DragonBoatFestivalPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onZongziEat(PlayerInteractEvent event) {
        if (!event.getAction().name().startsWith("RIGHT_")) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName() || !meta.hasLore()) {
            return;
        }
        String name = meta.getDisplayName();
        boolean luxury;
        if (name.equals(Color.text("&a普通粽子"))) {
            luxury = false;
        } else if (name.equals(Color.text("&6豪华粽子"))) {
            luxury = true;
        } else {
            return;
        }
        List<String> lore = meta.getLore();
        if (lore.size() < 2 || !lore.get(0).equals(Color.text("&7端午佳节亲手制作的粽子"))) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        int foodLevel = Math.min(player.getFoodLevel() + (luxury ? 10 : 6), 20);
        player.setFoodLevel(foodLevel);
        float saturation = Math.min(player.getSaturation() + (luxury ? 8 : 4), foodLevel);
        player.setSaturation(saturation);
        player.sendMessage(Color.text("&a你吃下了" + (luxury ? "&6豪华粽子" : "&a普通粽子") + "&a，恢复了饥饿值。"));
        player.playSound(player.getLocation(), "ENTITY_PLAYER_BURP", 1.0f, 1.0f);
    }

    public boolean craft(Player player, String type) {
        Map<MaterialType, Integer> cost = makeCost(type);
        if (!MaterialItems.has(player, cost)) {
            player.sendMessage(plugin.message("not-enough-materials"));
            return false;
        }
        MaterialItems.take(player, cost);
        PlayerData data = plugin.getPlayerDataManager().get(player);
        int points = Math.max(0, plugin.getConfig().getInt("points." + ("luxury".equals(type) ? "luxury-zongzi" : "normal-zongzi")));
        data.addPoints(points);
        if ("luxury".equals(type)) {
            data.addLuxuryMade();
        } else {
            data.addNormalMade();
        }
        plugin.getGoalManager().addProgress("make-zongzi", 1);
        plugin.getGoalManager().addProgress("make-" + type, 1);
        player.sendMessage(plugin.message("make-" + type).replace("{points}", String.valueOf(points)).replace("{count}", "1"));
        giveZongzi(player, type, 1);
        return true;
    }

    public int getMaxCraftable(Player player, String type) {
        Map<MaterialType, Integer> cost = makeCost(type);
        int max = Integer.MAX_VALUE;
        for (Map.Entry<MaterialType, Integer> entry : cost.entrySet()) {
            int need = entry.getValue();
            if (need <= 0) {
                continue;
            }
            int has = MaterialItems.count(player, entry.getKey());
            max = Math.min(max, has / need);
        }
        return max == Integer.MAX_VALUE ? 0 : max;
    }

    public boolean craftMultiple(Player player, String type, int count) {
        if (count <= 0) {
            player.sendMessage(plugin.message("not-enough-materials"));
            return false;
        }
        Map<MaterialType, Integer> singleCost = makeCost(type);
        Map<MaterialType, Integer> totalCost = new EnumMap<MaterialType, Integer>(MaterialType.class);
        for (MaterialType materialType : MaterialType.values()) {
            totalCost.put(materialType, singleCost.get(materialType) * count);
        }
        if (!MaterialItems.has(player, totalCost)) {
            player.sendMessage(plugin.message("not-enough-materials"));
            return false;
        }
        MaterialItems.take(player, totalCost);
        PlayerData data = plugin.getPlayerDataManager().get(player);
        int perPoints = Math.max(0, plugin.getConfig().getInt("points." + ("luxury".equals(type) ? "luxury-zongzi" : "normal-zongzi")));
        data.addPoints(perPoints * count);
        if ("luxury".equals(type)) {
            data.addLuxuryMade(count);
        } else {
            data.addNormalMade(count);
        }
        plugin.getGoalManager().addProgress("make-zongzi", count);
        plugin.getGoalManager().addProgress("make-" + type, count);
        player.sendMessage(plugin.message("make-" + type)
            .replace("{points}", String.valueOf(perPoints * count))
            .replace("{count}", String.valueOf(count)));
        giveZongzi(player, type, count);
        return true;
    }

    private void giveZongzi(Player player, String type, int count) {
        ItemStack zongzi = createZongziItemStack(type);
        zongzi.setAmount(count);
        Map<Integer, ItemStack> remaining = player.getInventory().addItem(zongzi);
        for (ItemStack item : remaining.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
    }

    private ItemStack createZongziItemStack(String type) {
        boolean luxury = "luxury".equals(type);
        ItemStack item = new ItemStack(luxury ? Material.CAKE : Material.BREAD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Color.text(luxury ? "&6豪华粽子" : "&a普通粽子"));
        List<String> lore = new ArrayList<String>();
        lore.add(Color.text("&7端午佳节亲手制作的粽子"));
        lore.add(Color.text("&7可与其他玩家交易"));
        lore.add(Color.text(""));
        lore.add(Color.text("&e▸ 右键食用恢复饥饿值"));
        lore.add(Color.text("&e▸ 也可收藏或交易给朋友"));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private Map<MaterialType, Integer> makeCost(String type) {
        Map<MaterialType, Integer> cost = new EnumMap<MaterialType, Integer>(MaterialType.class);
        String path = "make." + type + ".";
        cost.put(MaterialType.RICE, Math.max(0, plugin.getConfig().getInt(path + "rice", 0)));
        cost.put(MaterialType.LEAF, Math.max(0, plugin.getConfig().getInt(path + "leaf", 0)));
        cost.put(MaterialType.JUJUBE, Math.max(0, plugin.getConfig().getInt(path + "jujube", 0)));
        cost.put(MaterialType.MEAT, Math.max(0, plugin.getConfig().getInt(path + "meat", 0)));
        return cost;
    }
}
