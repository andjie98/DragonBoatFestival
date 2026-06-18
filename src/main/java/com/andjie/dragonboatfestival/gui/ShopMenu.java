package com.andjie.dragonboatfestival.gui;

import com.andjie.dragonboatfestival.Color;
import com.andjie.dragonboatfestival.DragonBoatFestivalPlugin;
import com.andjie.dragonboatfestival.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShopMenu implements Listener {

    private static final String TITLE = "§8⚘ §6端午活动商店 §8⚘";
    private static final int ROWS = 6;
    private static final int[] SHOP_SLOTS = new int[]{
        19, 20, 21, 23, 24, 25,
        28, 29, 30, 32, 33, 34
    };

    private final DragonBoatFestivalPlugin plugin;
    private List<ShopItem> items;

    public ShopMenu(DragonBoatFestivalPlugin plugin) {
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
                plugin.getShopConfig().getStringList(path + "lore"),
                plugin.getShopConfig().getStringList(path + "commands")));
        }
    }

    private boolean isValidKey(String key) {
        return key != null && key.matches("[A-Za-z0-9_-]+");
    }

    public void open(Player player) {
        MenuHolder holder = new MenuHolder("shop");
        Inventory inventory = Bukkit.createInventory(holder, ROWS * 9, TITLE);
        holder.setInventory(inventory);
        drawBorder(inventory);
        drawPlayerStatus(inventory, player);
        drawItems(inventory, player);
        player.openInventory(inventory);
    }

    public List<String> keys() {
        List<String> keys = new ArrayList<String>();
        for (ShopItem item : items) {
            keys.add(item.key);
        }
        return keys;
    }

    public boolean buy(Player player, String key, boolean refreshMenu) {
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
        if (refreshMenu) {
            open(player);
        }
        return true;
    }

    private void drawBorder(Inventory inventory) {
        ItemStack border = createBorderItem();
        // 顶部两行边框
        for (int i = 0; i < 18; i++) {
            inventory.setItem(i, border);
        }
        // 底部两行边框
        for (int i = ROWS * 9 - 9; i < ROWS * 9; i++) {
            inventory.setItem(i, border);
        }
        // 左右侧边框（第3-4行）
        for (int row = 2; row < 4; row++) {
            int base = row * 9;
            inventory.setItem(base, border);
            inventory.setItem(base + 8, border);
        }
        // 中间列分隔
        inventory.setItem(22, border);
        inventory.setItem(31, border);
    }

    private ItemStack createBorderItem() {
        ItemStack item = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§r");
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private void drawPlayerStatus(Inventory inventory, Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        // 玩家积分显示
        ItemStack pointsItem = new ItemStack(Material.EMERALD);
        ItemMeta pointsMeta = pointsItem.getItemMeta();
        pointsMeta.setDisplayName("§6✦ 我的积分");
        pointsMeta.setLore(Arrays.asList(
            "§7当前拥有：§e" + data.getPoints() + " §7节日积分",
            "§7点击商品即可兑换"
        ));
        pointsMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        pointsItem.setItemMeta(pointsMeta);
        inventory.setItem(4, pointsItem);

        // 装饰性物品
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        infoMeta.setDisplayName("§6✦ 兑换说明");
        infoMeta.setLore(Arrays.asList(
            "§7选择下方商品，点击兑换",
            "§7积分不足时无法购买"
        ));
        infoMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        infoItem.setItemMeta(infoMeta);
        inventory.setItem(13, infoItem);
    }

    private void drawItems(Inventory inventory, Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        for (int i = 0; i < SHOP_SLOTS.length && i < items.size(); i++) {
            inventory.setItem(SHOP_SLOTS[i], items.get(i).toItemStack(player, data));
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof MenuHolder)) {
            return;
        }
        MenuHolder holder = (MenuHolder) top.getHolder();
        if (!"shop".equals(holder.getId())) {
            return;
        }
        if (event.getRawSlot() >= top.getSize()) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        int index = -1;
        for (int i = 0; i < SHOP_SLOTS.length; i++) {
            if (SHOP_SLOTS[i] == slot) {
                index = i;
                break;
            }
        }
        if (index < 0 || index >= items.size()) {
            return;
        }

        buy(player, items.get(index).key, true);
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
        private final List<String> rawLore;
        private final List<String> commands;

        private ShopItem(String key, String name, Material material, int points, double money, List<String> rawLore, List<String> commands) {
            this.key = key;
            this.name = name;
            this.material = material;
            this.points = points;
            this.money = money;
            this.rawLore = rawLore;
            this.commands = commands;
        }

        private ItemStack toItemStack(Player player, PlayerData data) {
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(name);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);

            List<String> lore = new ArrayList<String>();
            lore.add("§7━━━━━━━━━━━━━━");
            // 配置的 lore
            for (String line : rawLore) {
                lore.add(Color.text(line));
            }
            lore.add("§7━━━━━━━━━━━━━━");
            boolean canAfford = data.getPoints() >= points;
            lore.add((canAfford ? "§a✔ 积分充足" : "§c✘ 积分不足") + " §8| §6" + points + " §7积分");
            lore.add("");
            lore.add(canAfford ? "§e点击兑换" : "§c积分不足");
            if (canAfford) {
                lore.add("§8剩余积分：§e" + (data.getPoints() - points));
            }

            meta.setLore(lore);
            item.setItemMeta(meta);

            // 积分足够时加附魔光效
            if (canAfford) {
                item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY, 1);
            }
            return item;
        }
    }
}
