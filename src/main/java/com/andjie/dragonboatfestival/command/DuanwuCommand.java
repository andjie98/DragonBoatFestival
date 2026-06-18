package com.andjie.dragonboatfestival.command;

import com.andjie.dragonboatfestival.Color;
import com.andjie.dragonboatfestival.DragonBoatFestivalPlugin;
import com.andjie.dragonboatfestival.data.MaterialItems;
import com.andjie.dragonboatfestival.data.MaterialType;
import com.andjie.dragonboatfestival.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class DuanwuCommand implements CommandExecutor, TabCompleter {

    private final DragonBoatFestivalPlugin plugin;
    private final java.util.Map<java.util.UUID, Long> makeCooldowns = new java.util.HashMap<>();

    public DuanwuCommand(DragonBoatFestivalPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean checkMakeCooldown(Player player) {
        long now = System.currentTimeMillis();
        long last = makeCooldowns.getOrDefault(player.getUniqueId(), 0L);
        int cd = plugin.getConfig().getInt("make.cooldown-seconds", 1);
        if (cd <= 0) return true;
        long elapsed = now - last;
        if (elapsed < cd * 1000L) {
            player.sendMessage(plugin.message("cooldown")
                .replace("{seconds}", String.valueOf(cd - elapsed / 1000)));
            return false;
        }
        makeCooldowns.put(player.getUniqueId(), now);
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return main(sender);
        }
        if ("help".equalsIgnoreCase(args[0])) {
            sendHelp(sender);
            return true;
        }
        if (!plugin.isFestivalEnabled() && !"reload".equalsIgnoreCase(args[0])) {
            sender.sendMessage(plugin.message("festival-disabled"));
            return true;
        }
        if ("points".equalsIgnoreCase(args[0])) {
            return points(sender);
        }
        if ("status".equalsIgnoreCase(args[0])) {
            return status(sender);
        }
        if ("guide".equalsIgnoreCase(args[0])) {
            return guide(sender);
        }
        if ("fish".equalsIgnoreCase(args[0])) {
            return fish(sender);
        }
        if ("goals".equalsIgnoreCase(args[0])) {
            return goals(sender);
        }
        if ("boss".equalsIgnoreCase(args[0])) {
            return boss(sender, args);
        }
        if ("materials".equalsIgnoreCase(args[0])) {
            return materials(sender);
        }
        if ("make".equalsIgnoreCase(args[0])) {
            return make(sender, args);
        }
        if ("shop".equalsIgnoreCase(args[0])) {
            return shop(sender);
        }
        if ("buy".equalsIgnoreCase(args[0])) {
            return buy(sender, args);
        }
        if ("sign".equalsIgnoreCase(args[0])) {
            return sign(sender);
        }
        if ("reload".equalsIgnoreCase(args[0])) {
            return reload(sender);
        }
        if ("open".equalsIgnoreCase(args[0])) {
            return open(sender, args);
        }
        if ("give".equalsIgnoreCase(args[0])) {
            return give(sender, args);
        }
        if ("addpoint".equalsIgnoreCase(args[0])) {
            return changePoint(sender, args, true);
        }
        if ("setpoint".equalsIgnoreCase(args[0])) {
            return changePoint(sender, args, false);
        }
        sender.sendMessage(plugin.message("unknown-command"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("help", "guide", "status", "points", "materials", "make", "shop", "buy", "sign", "fish", "goals", "boss", "reload", "open", "give", "addpoint", "setpoint"), args[0]);
        }
        if (args.length == 2 && ("addpoint".equalsIgnoreCase(args[0]) || "setpoint".equalsIgnoreCase(args[0]) || "open".equalsIgnoreCase(args[0]) || "give".equalsIgnoreCase(args[0]))) {
            List<String> players = new ArrayList<String>();
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                players.add(player.getName());
            }
            return filter(players, args[1]);
        }
        if (args.length == 2 && "make".equalsIgnoreCase(args[0])) {
            return filter(Arrays.asList("normal", "luxury"), args[1]);
        }
        if (args.length == 2 && "boss".equalsIgnoreCase(args[0])) {
            return filter(Arrays.asList("spawn"), args[1]);
        }
        if (args.length == 2 && "buy".equalsIgnoreCase(args[0])) {
            return filter(plugin.getShopMenu().keys(), args[1]);
        }
        if (args.length == 3 && "give".equalsIgnoreCase(args[0])) {
            return filter(Arrays.asList("rice", "leaf", "jujube", "meat"), args[2]);
        }
        return new ArrayList<String>();
    }

    private boolean main(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null) {
            return true;
        }
        if (!plugin.isFestivalEnabled()) {
            player.sendMessage(plugin.message("festival-disabled"));
            return true;
        }
        plugin.openMainMenu(player);
        return true;
    }

    private boolean points(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null) {
            return true;
        }
        PlayerData data = plugin.getPlayerDataManager().get(player);
        player.sendMessage(plugin.message("points").replace("{points}", String.valueOf(data.getPoints())));
        return true;
    }

    private boolean status(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null) {
            return true;
        }
        sendStatus(player);
        return true;
    }

    private boolean guide(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null) {
            return true;
        }
        sendGuide(player);
        return true;
    }

    private boolean fish(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null) {
            return true;
        }
        sendFishGuide(player);
        return true;
    }

    private boolean goals(CommandSender sender) {
        sender.sendMessage(Color.text("&a===== 全服任务进度 ====="));
        Set<String> keys = plugin.getGoalManager().keys();
        if (keys.isEmpty()) {
            sender.sendMessage(Color.text("&7暂无开启的全服任务。"));
            return true;
        }
        for (String key : keys) {
            if (!plugin.getConfig().getBoolean("server-goals." + key + ".enabled", false)) {
                continue;
            }
            String name = plugin.getConfig().getString("server-goals." + key + ".name", key);
            int target = plugin.getConfig().getInt("server-goals." + key + ".target", 0);
            int progress = plugin.getGoalManager().getProgress(key);
            String done = plugin.getGoalManager().isCompleted(key) ? " &a已完成" : "";
            sender.sendMessage(Color.text("&e" + name + "&7：&b" + Math.min(progress, target) + "&7/&b" + target + done));
        }
        return true;
    }

    private boolean boss(CommandSender sender, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null || !hasPermission(player, "duanwu.admin")) {
            return true;
        }
        if (args.length < 2 || !"spawn".equalsIgnoreCase(args[1])) {
            player.sendMessage(Color.text("&c用法：/duanwu boss spawn"));
            return true;
        }
        plugin.getBossManager().spawn(player);
        return true;
    }

    private boolean materials(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null) {
            return true;
        }
        player.sendMessage(plugin.message("materials")
            .replace("{rice}", String.valueOf(MaterialItems.count(player, MaterialType.RICE)))
            .replace("{leaf}", String.valueOf(MaterialItems.count(player, MaterialType.LEAF)))
            .replace("{jujube}", String.valueOf(MaterialItems.count(player, MaterialType.JUJUBE)))
            .replace("{meat}", String.valueOf(MaterialItems.count(player, MaterialType.MEAT))));
        return true;
    }

    private boolean make(CommandSender sender, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null || !hasPermission(player, "duanwu.make")) {
            return true;
        }
        if (args.length < 2) {
            plugin.openMakeMenu(player);
            return true;
        }
        String type = args[1].toLowerCase();
        if (!"normal".equals(type) && !"luxury".equals(type)) {
            sender.sendMessage(Color.text("&c用法：/duanwu make [normal|luxury]"));
            return true;
        }
        if (!checkMakeCooldown(player)) {
            return true;
        }
        plugin.getMakeMenu().craft(player, type);
        return true;
    }

    private boolean shop(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null || !hasPermission(player, "duanwu.shop")) {
            return true;
        }
        plugin.openShopMenu(player);
        player.sendMessage(plugin.message("shop-open"));
        return true;
    }

    private boolean buy(CommandSender sender, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null || !hasPermission(player, "duanwu.shop")) {
            return true;
        }
        if (args.length < 2) {
            player.sendMessage(Color.text("&c用法：/duanwu buy 商品ID"));
            return true;
        }
        plugin.getShopMenu().buy(player, args[1], false);
        return true;
    }

    private boolean sign(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null || !hasPermission(player, "duanwu.sign")) {
            return true;
        }
        PlayerData data = plugin.getPlayerDataManager().get(player);
        if (data.hasSignedToday(plugin.getSignZoneId())) {
            player.sendMessage(plugin.message("sign-already"));
            return true;
        }
        int day = (data.getSignDays() % 5) + 1;
        String path = "sign-rewards.day" + day;
        data.setLastSign(LocalDate.now(plugin.getSignZoneId()).toString());
        data.addSignDay();
        plugin.getGoalManager().addProgress("sign", 1);
        data.addPoints(Math.max(0, plugin.getConfig().getInt(path + ".points", 0)));
        int exp = plugin.getConfig().getInt(path + ".exp", 0);
        if (exp > 0) {
            player.giveExp(exp);
        }
        for (String command : plugin.getConfig().getStringList(path + ".commands")) {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command.replace("{player}", player.getName()));
        }
        player.sendMessage(Color.text(plugin.getMessages().getString("prefix", "") + plugin.getConfig().getString(path + ".message", "&a签到成功。")));
        return true;
    }

    private boolean reload(CommandSender sender) {
        if (!hasPermission(sender, "duanwu.admin")) {
            return true;
        }
        plugin.reloadPlugin();
        sender.sendMessage(plugin.message("reloaded"));
        return true;
    }

    private boolean open(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "duanwu.admin")) {
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(Color.text("&c用法：/duanwu open 玩家"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(Color.text("&c玩家不在线。"));
            return true;
        }
        plugin.openMainMenu(target);
        return true;
    }

    private boolean give(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "duanwu.admin")) {
            return true;
        }
        if (args.length < 4) {
            sender.sendMessage(Color.text("&c用法：/duanwu give 玩家 材料 数量"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        MaterialType type = MaterialType.fromKey(args[2]);
        Integer amount = parseInt(args[3]);
        if (target == null || type == null || amount == null || amount < 1) {
            sender.sendMessage(Color.text("&c参数错误。"));
            return true;
        }
        MaterialItems.give(target, type, amount);
        sender.sendMessage(plugin.message("give-material")
            .replace("{player}", target.getName())
            .replace("{material}", type.getDisplayName())
            .replace("{amount}", String.valueOf(amount)));
        if (target.isOnline()) {
            target.sendMessage(Color.text("&a[端午节]&r &a你收到了 &e" + type.getDisplayName() + " x" + amount + " &a（管理员给予）。"));
        }
        return true;
    }

    private boolean changePoint(CommandSender sender, String[] args, boolean add) {
        if (!hasPermission(sender, "duanwu.admin")) {
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(Color.text(add ? "&c用法：/duanwu addpoint 玩家 数量" : "&c用法：/duanwu setpoint 玩家 数量"));
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        Integer amount = parseInt(args[2]);
        if (target == null || amount == null) {
            sender.sendMessage(Color.text("&c参数错误。"));
            return true;
        }
        PlayerData data = plugin.getPlayerDataManager().get(target);
        if (add) {
            data.addPoints(amount);
            sender.sendMessage(plugin.message("add-points").replace("{player}", target.getName()).replace("{amount}", String.valueOf(amount)));
        } else {
            data.setPoints(amount);
            sender.sendMessage(plugin.message("set-points").replace("{player}", target.getName()).replace("{amount}", String.valueOf(amount)));
        }
        return true;
    }

    private Player requirePlayer(CommandSender sender) {
        if (sender instanceof Player) {
            return (Player) sender;
        }
        sender.sendMessage(plugin.message("player-only"));
        return null;
    }

    private boolean hasPermission(CommandSender sender, String permission) {
        if (sender.isOp() || sender.hasPermission(permission)) {
            return true;
        }
        sender.sendMessage(plugin.message("no-permission"));
        return false;
    }

    private Integer parseInt(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private List<String> filter(List<String> values, String prefix) {
        List<String> result = new ArrayList<String>();
        for (String value : values) {
            if (value.toLowerCase().startsWith(prefix.toLowerCase())) {
                result.add(value);
            }
        }
        return result;
    }

    public void sendStatus(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        player.sendMessage(Color.text("&a===== 我的端午活动状态 ====="));
        player.sendMessage(Color.text("&7节日积分：&e" + data.getPoints()));
        player.sendMessage(Color.text("&7材料：糯米 &e" + MaterialItems.count(player, MaterialType.RICE)
            + "&7，粽叶 &e" + MaterialItems.count(player, MaterialType.LEAF)
            + "&7，红枣 &e" + MaterialItems.count(player, MaterialType.JUJUBE)
            + "&7，鲜肉 &e" + MaterialItems.count(player, MaterialType.MEAT)));
        player.sendMessage(Color.text("&7统计：普通粽子 &e" + data.getNormalMade()
            + "&7，豪华粽子 &e" + data.getLuxuryMade()
            + "&7，签到 &e" + data.getSignDays()
            + "&7，兑换 &e" + data.getShopPurchases()
            + "&7，摸鱼奖励 &e" + data.getFishRewards()));
        player.sendMessage(Color.text(data.hasSignedToday(plugin.getSignZoneId()) ? "&a今日已签到" : "&e今日还没签到，输入 /duanwu sign 领取奖励。"));
        player.sendMessage(Color.text(nextStep(player, data)));
    }

    public void sendGuide(Player player) {
        player.sendMessage(Color.text("&a===== 端午活动新手指南 ====="));
        player.sendMessage(Color.text("&e1. 收集材料：&7挖矿/打怪拿糯米，破坏树叶拿粽叶，收作物拿红枣，杀动物拿鲜肉。"));
        player.sendMessage(Color.text("&e2. 查看材料：&7输入 &f/duanwu materials &7看看背包里有多少材料。"));
        player.sendMessage(Color.text("&e3. 制作粽子：&7输入 &f/duanwu make &7打开制作面板。"));
        player.sendMessage(Color.text("&e   ▸ &a左键点击 → 做 1 个"));
        player.sendMessage(Color.text("&e   ▸ &e右键点击 → 材料够的话一次性全部做完"));
        player.sendMessage(Color.text("&e4. 获得积分：&7制作粽子就能拿到节日积分。"));
        player.sendMessage(Color.text("&e5. 兑换奖励：&7输入 &f/duanwu shop &7用积分换东西。"));
        player.sendMessage(Color.text("&e额外玩法：&7每天 &f/duanwu sign &7签到，钓鱼也能拿到材料。"));
        player.sendMessage(Color.text("&c注意：&7只有插件发的端午材料才算，普通原版物品不算。"));
    }

    public void sendFishGuide(Player player) {
        player.sendMessage(Color.text("&a===== 幸运摸鱼说明 ====="));
        player.sendMessage(Color.text("&7钓鱼成功时，会按配置随机获得端午材料或礼包。"));
        player.sendMessage(Color.text("&7默认奖励：糯米、粽叶、红枣、鲜肉、幸运礼包、传说礼包。"));
        player.sendMessage(Color.text("&7奖励概率和礼包命令由服主在 config.yml 中配置。"));
    }

    private String nextStep(Player player, PlayerData data) {
        if (!data.hasSignedToday(plugin.getSignZoneId())) {
            return "&e推荐下一步：输入 /duanwu sign 先领取今日签到奖励。";
        }
        int rice = MaterialItems.count(player, MaterialType.RICE);
        int leaf = MaterialItems.count(player, MaterialType.LEAF);
        if (rice >= plugin.getConfig().getInt("make.normal.rice", 5) && leaf >= plugin.getConfig().getInt("make.normal.leaf", 2)) {
            return "&e推荐下一步：输入 /duanwu make 打开制作面板，左键做1个，右键全部做完。";
        }
        if (data.getPoints() > 0) {
            return "&e推荐下一步：输入 /duanwu shop 看看能兑换什么奖励。";
        }
        return "&e推荐下一步：先去挖矿、破坏树叶、收作物、打怪或钓鱼收集材料。";
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Color.text("&a===== 端午节活动 ====="));
        sender.sendMessage(Color.text("&e/duanwu &7打开活动中心"));
        sender.sendMessage(Color.text("&e/duanwu guide &7新手玩法指南"));
        sender.sendMessage(Color.text("&e/duanwu status &7查看我的活动状态"));
        sender.sendMessage(Color.text("&e/duanwu points &7查看积分"));
        sender.sendMessage(Color.text("&e/duanwu materials &7查看材料"));
        sender.sendMessage(Color.text("&e/duanwu make &7打开制作面板（左键做1个，右键全部做完）"));
        sender.sendMessage(Color.text("&e/duanwu shop &7活动商店"));
        sender.sendMessage(Color.text("&e/duanwu sign &7每日签到"));
        sender.sendMessage(Color.text("&e/duanwu fish &7幸运摸鱼说明"));
        sender.sendMessage(Color.text("&e/duanwu goals &7查看全服任务"));
        if (sender.hasPermission("duanwu.admin")) {
            sender.sendMessage(Color.text("&c/duanwu reload/open/give/addpoint/setpoint/boss spawn"));
        }
    }
}
