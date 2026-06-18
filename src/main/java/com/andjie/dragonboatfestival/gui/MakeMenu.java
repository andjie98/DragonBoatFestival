package com.andjie.dragonboatfestival.gui;

import com.andjie.dragonboatfestival.Color;
import com.andjie.dragonboatfestival.DragonBoatFestivalPlugin;
import com.andjie.dragonboatfestival.data.MaterialItems;
import com.andjie.dragonboatfestival.data.MaterialType;
import com.andjie.dragonboatfestival.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class MakeMenu implements Listener {

    private static final String TITLE = "§a端午粽子制作";

    private final DragonBoatFestivalPlugin plugin;
    private final java.util.Map<java.util.UUID, Long> cooldowns = new java.util.HashMap<>();

    public MakeMenu(DragonBoatFestivalPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean checkCooldown(Player player) {
        long now = System.currentTimeMillis();
        long last = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        int cd = plugin.getConfig().getInt("make.cooldown-seconds", 1);
        if (cd <= 0) return true;
        if (now - last < cd * 1000L) return false;
        cooldowns.put(player.getUniqueId(), now);
        return true;
    }

    public void open(Player player) {
        MenuHolder holder = new MenuHolder("make");
        Inventory inventory = Bukkit.createInventory(holder, 27, TITLE);
        holder.setInventory(inventory);
        inventory.setItem(11, createZongziItem(player, "normal", "&a普通粽子", Material.BREAD));
        inventory.setItem(15, createZongziItem(player, "luxury", "&6豪华粽子", Material.CAKE));
        player.openInventory(inventory);
        player.sendMessage(plugin.message("make-menu-open"));
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
        // 检查 lore 标记，防止误食普通面包/蛋糕
        List<String> lore = meta.getLore();
        if (lore.size() < 2 || !lore.get(0).equals(Color.text("&7端午佳节亲手制作的粽子"))) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        // 减少一个物品
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        // 恢复饥饿值
        int foodLevel = Math.min(player.getFoodLevel() + (luxury ? 10 : 6), 20);
        player.setFoodLevel(foodLevel);
        float saturation = Math.min(player.getSaturation() + (luxury ? 8 : 4), foodLevel);
        player.setSaturation(saturation);
        player.sendMessage(Color.text("&a你吃下了" + (luxury ? "&6豪华粽子" : "&a普通粽子") + "&a，恢复了饥饿值。"));
        player.playSound(player.getLocation(), "ENTITY_PLAYER_BURP", 1.0f, 1.0f);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof MenuHolder)) {
            return;
        }
        MenuHolder holder = (MenuHolder) top.getHolder();
        if (!"make".equals(holder.getId())) {
            return;
        }
        if (event.getRawSlot() >= top.getSize()) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        String type;
        if (event.getRawSlot() == 11) {
            type = "normal";
        } else if (event.getRawSlot() == 15) {
            type = "luxury";
        } else {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        if (!player.hasPermission("duanwu.make")) {
            player.sendMessage(plugin.message("no-permission"));
            return;
        }
        if (!checkCooldown(player)) {
            int cd = plugin.getConfig().getInt("make.cooldown-seconds", 1);
            player.sendMessage(plugin.message("cooldown").replace("{seconds}", String.valueOf(cd)));
            return;
        }
        if (event.isRightClick()) {
            int max = getMaxCraftable(player, type);
            if (max <= 0) {
                player.sendMessage(plugin.message("not-enough-materials"));
                return;
            }
            craftMultiple(player, type, max);
        } else {
            craft(player, type);
        }
        event.getInventory().setItem(11, createZongziItem(player, "normal", "&a普通粽子", Material.BREAD));
        event.getInventory().setItem(15, createZongziItem(player, "luxury", "&6豪华粽子", Material.CAKE));
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
        // 给予实体粽子物品（可交易）
        ItemStack zongzi = createZongziItemStack(type);
        Map<Integer, ItemStack> remaining = player.getInventory().addItem(zongzi);
        for (ItemStack item : remaining.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
        return true;
    }

    /**
     * 获取玩家最多能制作多少个指定类型的粽子
     */
    public int getMaxCraftable(Player player, String type) {
        Map<MaterialType, Integer> cost = makeCost(type);
        int max = Integer.MAX_VALUE;
        for (Map.Entry<MaterialType, Integer> entry : cost.entrySet()) {
            int need = entry.getValue();
            if (need <= 0) continue;
            int has = MaterialItems.count(player, entry.getKey());
            max = Math.min(max, has / need);
        }
        return max == Integer.MAX_VALUE ? 0 : max;
    }

    /**
     * 批量制作指定数量的粽子（按住 Shift 时调用）
     */
    public boolean craftMultiple(Player player, String type, int count) {
        if (count <= 0) {
            player.sendMessage(plugin.message("not-enough-materials"));
            return false;
        }
        Map<MaterialType, Integer> singleCost = makeCost(type);
        Map<MaterialType, Integer> totalCost = new EnumMap<>(MaterialType.class);
        for (MaterialType mt : MaterialType.values()) {
            totalCost.put(mt, singleCost.get(mt) * count);
        }
        if (!MaterialItems.has(player, totalCost)) {
            player.sendMessage(plugin.message("not-enough-materials"));
            return false;
        }
        MaterialItems.take(player, totalCost);
        PlayerData data = plugin.getPlayerDataManager().get(player);
        int perPoints = plugin.getConfig().getInt("points." + ("luxury".equals(type) ? "luxury-zongzi" : "normal-zongzi"));
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
        ItemStack zongzi = createZongziItemStack(type);
        zongzi.setAmount(count);
        Map<Integer, ItemStack> remaining = player.getInventory().addItem(zongzi);
        for (ItemStack item : remaining.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
        return true;
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

    private ItemStack createZongziItem(Player player, String type, String name, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Color.text(name));
        List<String> lore = new ArrayList<String>();
        Map<MaterialType, Integer> cost = makeCost(type);
        int points = plugin.getConfig().getInt("points." + ("luxury".equals(type) ? "luxury-zongzi" : "normal-zongzi"));
        lore.add(Color.text("&7点击制作，获得 &e" + points + " &7节日积分"));
        lore.add(Color.text("&7材料需求："));
        for (MaterialType materialType : MaterialType.values()) {
            int need = cost.get(materialType);
            if (need <= 0) {
                continue;
            }
            int has = MaterialItems.count(player, materialType);
            lore.add(Color.text((has >= need ? "&a" : "&c") + materialType.getDisplayName() + "：" + has + "/" + need));
        }
        lore.add(Color.text(""));
        int max = getMaxCraftable(player, type);
        lore.add(Color.text("&7右键点击 &f一次性制作全部 &e(&7最多 &f" + max + " &7个&e)"));
        lore.add(Color.text(""));
        lore.add(Color.text(MaterialItems.has(player, cost) ? "&a左键制作 1 个" : "&c材料不足"));
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
