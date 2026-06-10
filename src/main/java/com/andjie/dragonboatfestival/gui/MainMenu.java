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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MainMenu implements Listener {

    private static final String TITLE = "§a端午活动中心";

    private final DragonBoatFestivalPlugin plugin;

    public MainMenu(DragonBoatFestivalPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(new MenuHolder("main"), 27, TITLE);
        inventory.setItem(4, item(Material.BOOK, "&a我的活动状态", statusLore(player)));
        inventory.setItem(10, item(Material.WORKBENCH, "&e制作粽子", list("&7查看材料是否足够", "&7点击打开粽子制作菜单")));
        inventory.setItem(11, item(Material.EMERALD, "&6活动商店", list("&7使用节日积分兑换奖励")));
        inventory.setItem(12, item(Material.SIGN, "&b每日签到", list("&7每天领取一次活动奖励")));
        inventory.setItem(14, item(Material.FISHING_ROD, "&3幸运摸鱼", list("&7钓鱼可获得材料和礼包", "&7奖励概率可由服主配置")));
        inventory.setItem(15, item(Material.PAPER, "&f新手指南", list("&7不知道怎么玩？", "&7点击查看完整流程")));
        player.openInventory(inventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof MenuHolder)) {
            return;
        }
        MenuHolder holder = (MenuHolder) event.getInventory().getHolder();
        if (!"main".equals(holder.getId())) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();
        if (slot == 4) {
            player.closeInventory();
            plugin.getDuanwuCommand().sendStatus(player);
        } else if (slot == 10) {
            plugin.getMakeMenu().open(player);
        } else if (slot == 11) {
            plugin.getShopMenu().open(player);
            player.sendMessage(plugin.message("shop-open"));
        } else if (slot == 12) {
            player.closeInventory();
            player.performCommand("duanwu sign");
        } else if (slot == 14) {
            player.closeInventory();
            plugin.getDuanwuCommand().sendFishGuide(player);
        } else if (slot == 15) {
            player.closeInventory();
            plugin.getDuanwuCommand().sendGuide(player);
        }
    }

    private List<String> statusLore(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        List<String> lore = new ArrayList<String>();
        lore.add("&7积分：&e" + data.getPoints());
        lore.add("&7糯米：&e" + MaterialItems.count(player, MaterialType.RICE));
        lore.add("&7粽叶：&e" + MaterialItems.count(player, MaterialType.LEAF));
        lore.add("&7红枣：&e" + MaterialItems.count(player, MaterialType.JUJUBE));
        lore.add("&7鲜肉：&e" + MaterialItems.count(player, MaterialType.MEAT));
        lore.add(data.hasSignedToday() ? "&a今日已签到" : "&c今日未签到");
        lore.add("&7点击查看详细状态");
        return lore;
    }

    private ItemStack item(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Color.text(name));
        meta.setLore(Color.list(lore));
        item.setItemMeta(meta);
        return item;
    }

    private List<String> list(String first) {
        List<String> lore = new ArrayList<String>();
        lore.add(first);
        return lore;
    }

    private List<String> list(String first, String second) {
        List<String> lore = list(first);
        lore.add(second);
        return lore;
    }
}
