package com.andjie.dragonboatfestival.hook;

import com.andjie.dragonboatfestival.DragonBoatFestivalPlugin;
import com.andjie.dragonboatfestival.data.MaterialItems;
import com.andjie.dragonboatfestival.data.MaterialType;
import com.andjie.dragonboatfestival.data.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * PlaceholderAPI 扩展 — 提供 %duanwu_*% 变量。
 * 需要服务器安装 PlaceholderAPI 插件。
 *
 * <p>统计类占位符通过 getOrLoad 支持离线玩家读取（适配 ajLeaderboards 排行榜）；
 * 材料类占位符依赖在线背包，仅在线时返回值。</p>
 */
public class DuanwuExpansion extends PlaceholderExpansion {

    private final DragonBoatFestivalPlugin plugin;

    public DuanwuExpansion(DragonBoatFestivalPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "duanwu";
    }

    @Override
    public String getAuthor() {
        return "andjie";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    /**
     * 主入口，支持在线与离线玩家。
     * PlaceholderAPI 优先调用本方法；离线玩家（如 ajLeaderboards 轮询）会走到这里。
     */
    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null || params == null) {
            return "";
        }
        String param = params.toLowerCase();

        // 材料类：依赖在线背包，离线玩家返回空。
        if (isMaterialPlaceholder(param)) {
            if (!(player instanceof Player)) {
                return "";
            }
            Player online = (Player) player;
            return resolveMaterial(online, param);
        }

        // 统计类：在线离线均可读取。
        PlayerData data = plugin.getPlayerDataManager().getOrLoad(player);
        return resolveStats(data, param);
    }

    /**
     * 旧版调用入口（仅在线 Player），转发到 onRequest 以保持兼容。
     */
    @Override
    public String onPlaceholderRequest(Player player, String params) {
        return onRequest(player, params);
    }

    private boolean isMaterialPlaceholder(String param) {
        return "rice".equals(param) || "leaf".equals(param)
            || "jujube".equals(param) || "meat".equals(param)
            || "materials_total".equals(param);
    }

    private String resolveStats(PlayerData data, String param) {
        if ("points".equals(param)) {
            return String.valueOf(data.getPoints());
        }
        if ("normal_made".equals(param)) {
            return String.valueOf(data.getNormalMade());
        }
        if ("luxury_made".equals(param)) {
            return String.valueOf(data.getLuxuryMade());
        }
        // 粽子总数（普通+豪华）— 适合排行榜按总制作量排序
        if ("zongzi_total".equals(param)) {
            return String.valueOf(data.getNormalMade() + data.getLuxuryMade());
        }
        if ("sign_days".equals(param)) {
            return String.valueOf(data.getSignDays());
        }
        if ("signed_today".equals(param)) {
            return data.hasSignedToday(plugin.getSignZoneId()) ? "yes" : "no";
        }
        if ("fish_rewards".equals(param)) {
            return String.valueOf(data.getFishRewards());
        }
        if ("shop_purchases".equals(param)) {
            return String.valueOf(data.getShopPurchases());
        }
        return "";
    }

    private String resolveMaterial(Player player, String param) {
        if ("rice".equals(param)) {
            return String.valueOf(MaterialItems.count(player, MaterialType.RICE));
        }
        if ("leaf".equals(param)) {
            return String.valueOf(MaterialItems.count(player, MaterialType.LEAF));
        }
        if ("jujube".equals(param)) {
            return String.valueOf(MaterialItems.count(player, MaterialType.JUJUBE));
        }
        if ("meat".equals(param)) {
            return String.valueOf(MaterialItems.count(player, MaterialType.MEAT));
        }
        // 材料总数 — 仅在线时有意义
        if ("materials_total".equals(param)) {
            int total = 0;
            for (MaterialType type : MaterialType.values()) {
                total += MaterialItems.count(player, type);
            }
            return String.valueOf(total);
        }
        return "";
    }
}
