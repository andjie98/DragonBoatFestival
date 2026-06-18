package com.andjie.dragonboatfestival.hook;

import com.andjie.dragonboatfestival.DragonBoatFestivalPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.entity.Player;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class TrMenuHook {

    public static final String MAIN_MENU = "duanwu-main";
    public static final String MAKE_MENU = "duanwu-make";
    public static final String SHOP_MENU = "duanwu-shop";

    private final DragonBoatFestivalPlugin plugin;
    private final Plugin trMenu;
    private boolean enabled;

    public TrMenuHook(DragonBoatFestivalPlugin plugin) {
        this.plugin = plugin;
        this.trMenu = plugin.getServer().getPluginManager().getPlugin("TrMenu");
        this.enabled = trMenu != null && trMenu.isEnabled();
        if (enabled) {
            installMenus();
            reloadMenus();
            plugin.getLogger().info("已接入 TrMenu 菜单");
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean open(Player player, String menuId) {
        if (!enabled) {
            return false;
        }
        try {
            Bukkit.dispatchCommand(plugin.getServer().getConsoleSender(), "trmenu open " + menuId + " " + player.getName());
            return true;
        } catch (Throwable throwable) {
            plugin.getLogger().warning("打开 TrMenu 菜单失败(" + menuId + "): " + throwable.getMessage());
            return false;
        }
    }

    public void reload() {
        if (!enabled) {
            return;
        }
        installMenus();
        reloadMenus();
    }

    private void installMenus() {
        File menuFolder = new File(trMenu.getDataFolder(), "menus/DragonBoatFestival");
        if (!menuFolder.exists() && !menuFolder.mkdirs()) {
            plugin.getLogger().warning("无法创建 TrMenu 菜单目录: " + menuFolder.getPath());
            return;
        }
        writeMenu(new File(menuFolder, "duanwu-main.yml"), mainMenuYaml());
        writeMenu(new File(menuFolder, "duanwu-make.yml"), makeMenuYaml());
        writeMenu(new File(menuFolder, "duanwu-shop.yml"), shopMenuYaml());
    }

    private void writeMenu(File target, String content) {
        if (target.exists() && !plugin.getConfig().getBoolean("trmenu.overwrite-menus", false)) {
            plugin.getLogger().info("已保留现有 TrMenu 菜单文件: " + target.getName());
            return;
        }
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(target), "UTF-8"));
            writer.write(content);
        } catch (IOException exception) {
            plugin.getLogger().warning("无法写入 TrMenu 菜单文件 " + target.getName() + ": " + exception.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void reloadMenus() {
        try {
            Bukkit.dispatchCommand(plugin.getServer().getConsoleSender(), "trmenu reload");
        } catch (Throwable throwable) {
            plugin.getLogger().warning("重载 TrMenu 菜单失败: " + throwable.getMessage());
        }
    }

    private String mainMenuYaml() {
        return "Title: '&a端午活动中心'\n"
            + "\n"
            + "Layout:\n"
            + "  - '#########'\n"
            + "  - '#SMKGLF#'\n"
            + "  - '####B####'\n"
            + "\n"
            + "Options:\n"
            + "  Min-Click-Delay: 200\n"
            + "  Depend-Expansions: [ 'player', 'duanwu' ]\n"
            + "\n"
            + "Events:\n"
            + "  Open:\n"
            + "    - 'sound: BLOCK_CHEST_OPEN-1-1'\n"
            + "  Close:\n"
            + "    - 'sound: BLOCK_CHEST_CLOSE-1-1'\n"
            + "\n"
            + "Icons:\n"
            + "  '#':\n"
            + "    display:\n"
            + "      material: STAINED_GLASS_PANE:5\n"
            + "      name: '&r'\n"
            + "\n"
            + "  S:\n"
            + "    display:\n"
            + "      material: BOOK\n"
            + "      name: '&a我的活动状态'\n"
            + "      lore:\n"
            + "        - '&7积分：&e%duanwu_points%'\n"
            + "        - '&7糯米：&e%duanwu_rice%'\n"
            + "        - '&7粽叶：&e%duanwu_leaf%'\n"
            + "        - '&7红枣：&e%duanwu_jujube%'\n"
            + "        - '&7鲜肉：&e%duanwu_meat%'\n"
            + "        - '&7签到天数：&e%duanwu_sign_days%'\n"
            + "        - ''\n"
            + "        - '&e点击查看详细状态'\n"
            + "    actions:\n"
            + "      all:\n"
            + "        - close\n"
            + "        - 'command: duanwu status'\n"
            + "\n"
            + "  M:\n"
            + "    display:\n"
            + "      material: WORKBENCH\n"
            + "      name: '&e制作粽子'\n"
            + "      lore:\n"
            + "        - '&7普通粽子和豪华粽子都在这里制作。'\n"
            + "        - '&7当前材料总数：&e%duanwu_materials_total%'\n"
            + "        - ''\n"
            + "        - '&e点击打开制作菜单'\n"
            + "    actions:\n"
            + "      all:\n"
            + "        - 'open: duanwu-make'\n"
            + "        - 'sound: BLOCK_NOTE_PLING-1-1'\n"
            + "\n"
            + "  K:\n"
            + "    display:\n"
            + "      material: EMERALD\n"
            + "      name: '&6活动商店'\n"
            + "      lore:\n"
            + "        - '&7使用节日积分兑换奖励。'\n"
            + "        - '&7当前积分：&e%duanwu_points%'\n"
            + "        - ''\n"
            + "        - '&e点击打开活动商店'\n"
            + "    actions:\n"
            + "      all:\n"
            + "        - 'open: duanwu-shop'\n"
            + "        - 'sound: BLOCK_NOTE_PLING-1-1'\n"
            + "\n"
            + "  G:\n"
            + "    display:\n"
            + "      material: SIGN\n"
            + "      name: '&b每日签到'\n"
            + "      lore:\n"
            + "        - '&7每天领取一次活动奖励。'\n"
            + "        - '&7今日状态：&e%duanwu_signed_today%'\n"
            + "        - ''\n"
            + "        - '&e点击签到'\n"
            + "    actions:\n"
            + "      all:\n"
            + "        - close\n"
            + "        - 'command: duanwu sign'\n"
            + "\n"
            + "  L:\n"
            + "    display:\n"
            + "      material: FISHING_ROD\n"
            + "      name: '&3幸运摸鱼'\n"
            + "      lore:\n"
            + "        - '&7钓鱼可获得材料和礼包。'\n"
            + "        - '&7摸鱼奖励次数：&e%duanwu_fish_rewards%'\n"
            + "        - ''\n"
            + "        - '&e点击查看说明'\n"
            + "    actions:\n"
            + "      all:\n"
            + "        - close\n"
            + "        - 'command: duanwu fish'\n"
            + "\n"
            + "  F:\n"
            + "    display:\n"
            + "      material: PAPER\n"
            + "      name: '&f新手指南'\n"
            + "      lore:\n"
            + "        - '&7不知道怎么玩？'\n"
            + "        - '&7点击查看完整流程。'\n"
            + "    actions:\n"
            + "      all:\n"
            + "        - close\n"
            + "        - 'command: duanwu guide'\n"
            + "\n"
            + "  B:\n"
            + "    display:\n"
            + "      material: NETHER_STAR\n"
            + "      name: '&d全服目标'\n"
            + "      lore:\n"
            + "        - '&7查看全服任务进度。'\n"
            + "        - ''\n"
            + "        - '&e点击查看'\n"
            + "    actions:\n"
            + "      all:\n"
            + "        - close\n"
            + "        - 'command: duanwu goals'\n";
    }

    private String makeMenuYaml() {
        int normalRice = plugin.getConfig().getInt("make.normal.rice", 0);
        int normalLeaf = plugin.getConfig().getInt("make.normal.leaf", 0);
        int normalPoints = plugin.getConfig().getInt("points.normal-zongzi", 0);
        int luxuryRice = plugin.getConfig().getInt("make.luxury.rice", 0);
        int luxuryLeaf = plugin.getConfig().getInt("make.luxury.leaf", 0);
        int luxuryJujube = plugin.getConfig().getInt("make.luxury.jujube", 0);
        int luxuryMeat = plugin.getConfig().getInt("make.luxury.meat", 0);
        int luxuryPoints = plugin.getConfig().getInt("points.luxury-zongzi", 0);
        return "Title: '&a端午粽子制作'\n"
            + "\n"
            + "Layout:\n"
            + "  - '#########'\n"
            + "  - '#  AB  #'\n"
            + "  - '#  ##  #'\n"
            + "  - '####C####'\n"
            + "\n"
            + "Options:\n"
            + "  Min-Click-Delay: 200\n"
            + "  Depend-Expansions: [ 'player', 'duanwu' ]\n"
            + "\n"
            + "Events:\n"
            + "  Open:\n"
            + "    - 'sound: BLOCK_CHEST_OPEN-1-1'\n"
            + "\n"
            + "Icons:\n"
            + "  '#':\n"
            + "    display:\n"
            + "      material: STAINED_GLASS_PANE:13\n"
            + "      name: '&r'\n"
            + "\n"
            + "  A:\n"
            + "    display:\n"
            + "      material: BREAD\n"
            + "      name: '&a普通粽子'\n"
            + "      lore:\n"
            + "        - '&7点击制作 &e1 &7个，获得 &e" + normalPoints + " &7积分'\n"
            + "        - '&7Shift+点击 &f一次性制作全部'\n"
            + "        - ''\n"
            + "        - '&7消耗材料（每组）：'\n"
            + "        - '&a糯米：&e%duanwu_rice%&7/" + normalRice + "'\n"
            + "        - '&a粽叶：&e%duanwu_leaf%&7/" + normalLeaf + "'\n"
            + "        - ''\n"
            + "        - '&e点击制作 | &6Shift+点击全部'\n"
            + "    actions:\n"
            + "      all:\n"
            + "        - close\n"
            + "        - 'command: duanwu make'\n"
            + "\n"
            + "  B:\n"
            + "    display:\n"
            + "      material: CAKE\n"
            + "      name: '&6豪华粽子'\n"
            + "      lore:\n"
            + "        - '&7点击制作 &e1 &7个，获得 &e" + luxuryPoints + " &7积分'\n"
            + "        - '&7Shift+点击 &f一次性制作全部'\n"
            + "        - ''\n"
            + "        - '&7消耗材料（每组）：'\n"
            + "        - '&6糯米：&e%duanwu_rice%&7/" + luxuryRice + "'\n"
            + "        - '&6粽叶：&e%duanwu_leaf%&7/" + luxuryLeaf + "'\n"
            + "        - '&6红枣：&e%duanwu_jujube%&7/" + luxuryJujube + "'\n"
            + "        - '&6鲜肉：&e%duanwu_meat%&7/" + luxuryMeat + "'\n"
            + "        - ''\n"
            + "        - '&e点击制作 | &6Shift+点击全部'\n"
            + "    actions:\n"
            + "      all:\n"
            + "        - close\n"
            + "        - 'command: duanwu make'\n"
            + "\n"
            + "  C:\n"
            + "    display:\n"
            + "      material: ARROW\n"
            + "      name: '&f返回活动中心'\n"
            + "    actions:\n"
            + "      all:\n"
            + "        - 'open: duanwu-main'\n"
            + "        - 'sound: BLOCK_NOTE_PLING-1-1'\n";
    }

    private String shopMenuYaml() {
        StringBuilder yaml = new StringBuilder();
        yaml.append("Title: '&6端午活动商店'\n\n");
        yaml.append("Layout:\n");
        yaml.append("  - '#########'\n");
        yaml.append("  - '#   P   #'\n");
        yaml.append("  - '").append(shopLayoutRow(0)).append("'\n");
        yaml.append("  - '").append(shopLayoutRow(3)).append("'\n");
        yaml.append("  - '####B####'\n\n");
        yaml.append("Options:\n");
        yaml.append("  Min-Click-Delay: 200\n");
        yaml.append("  Depend-Expansions: [ 'player', 'duanwu' ]\n\n");
        yaml.append("Events:\n");
        yaml.append("  Open:\n");
        yaml.append("    - 'sound: BLOCK_CHEST_OPEN-1-1'\n\n");
        yaml.append("Icons:\n");
        yaml.append("  '#':\n");
        yaml.append("    display:\n");
        yaml.append("      material: STAINED_GLASS_PANE:1\n");
        yaml.append("      name: '&r'\n\n");
        yaml.append("  P:\n");
        yaml.append("    display:\n");
        yaml.append("      material: EMERALD\n");
        yaml.append("      name: '&6我的积分'\n");
        yaml.append("      lore:\n");
        yaml.append("        - '&7当前拥有：&e%duanwu_points% &7节日积分'\n");
        yaml.append("        - '&7点击商品即可兑换。'\n\n");
        appendShopIcons(yaml);
        yaml.append("  B:\n");
        yaml.append("    display:\n");
        yaml.append("      material: ARROW\n");
        yaml.append("      name: '&f返回活动中心'\n");
        yaml.append("    actions:\n");
        yaml.append("      all:\n");
        yaml.append("        - 'open: duanwu-main'\n");
        yaml.append("        - 'sound: BLOCK_NOTE_PLING-1-1'\n");
        return yaml.toString();
    }

    private String shopLayoutRow(int offset) {
        List<Character> icons = shopIconIds();
        char first = offset < icons.size() ? icons.get(offset).charValue() : ' ';
        char second = offset + 1 < icons.size() ? icons.get(offset + 1).charValue() : ' ';
        char third = offset + 2 < icons.size() ? icons.get(offset + 2).charValue() : ' ';
        return "# " + first + " " + second + " " + third + " #";
    }

    private List<Character> shopIconIds() {
        List<Character> icons = new ArrayList<Character>();
        String candidates = shopIconCandidates();
        ConfigurationSection section = plugin.getShopConfig().getConfigurationSection("items");
        if (section == null) {
            return icons;
        }
        int index = 0;
        for (String ignored : section.getKeys(false)) {
            if (index >= candidates.length()) {
                plugin.getLogger().warning("TrMenu 商店模板最多显示 " + candidates.length() + " 个商品，超出的商品仅在内置 GUI 中显示。");
                break;
            }
            icons.add(Character.valueOf(candidates.charAt(index)));
            index++;
        }
        return icons;
    }

    private void appendShopIcons(StringBuilder yaml) {
        ConfigurationSection section = plugin.getShopConfig().getConfigurationSection("items");
        if (section == null) {
            return;
        }
        String candidates = shopIconCandidates();
        int index = 0;
        for (String key : section.getKeys(false)) {
            if (index >= candidates.length()) {
                break;
            }
            char icon = candidates.charAt(index);
            String path = "items." + key + ".";
            String material = safeMaterial(plugin.getShopConfig().getString(path + "material", "STONE"));
            String name = quote(plugin.getShopConfig().getString(path + "name", key));
            int points = plugin.getShopConfig().getInt(path + "points", 0);
            yaml.append("  ").append(icon).append(":\n");
            yaml.append("    display:\n");
            yaml.append("      material: ").append(material).append("\n");
            yaml.append("      name: '").append(name).append("'\n");
            yaml.append("      lore:\n");
            for (String line : plugin.getShopConfig().getStringList(path + "lore")) {
                yaml.append("        - '").append(quote(line)).append("'\n");
            }
            yaml.append("        - '&6价格：&e").append(points).append(" &7积分'\n");
            yaml.append("        - ''\n");
            yaml.append("        - '&e点击兑换'\n");
            yaml.append("    actions:\n");
            yaml.append("      all:\n");
            yaml.append("        - close\n");
            yaml.append("        - 'command: duanwu buy ").append(key).append("'\n\n");
            index++;
        }
    }

    private String shopIconCandidates() {
        return "CKFHTY";
    }

    private String safeMaterial(String materialName) {
        Material material = Material.matchMaterial(materialName);
        return material == null ? "STONE" : material.name();
    }

    private String quote(String value) {
        return value == null ? "" : value.replace("'", "''");
    }
}