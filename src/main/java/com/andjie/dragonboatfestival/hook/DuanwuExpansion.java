package com.andjie.dragonboatfestival.hook;

import com.andjie.dragonboatfestival.DragonBoatFestivalPlugin;
import com.andjie.dragonboatfestival.data.MaterialItems;
import com.andjie.dragonboatfestival.data.MaterialType;
import com.andjie.dragonboatfestival.data.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

/**
 * PlaceholderAPI 扩展 — 提供 %duanwu_*% 变量。
 * 需要服务器安装 PlaceholderAPI 插件。
 *
 * <p>注意：onPlaceholderRequest 可能被 PlaceholderAPI 的线程池调用，
 * 因此使用 getIfLoaded() 避免异步加载玩家数据。</p>
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

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null || params == null) {
            return "";
        }
        PlayerData data = plugin.getPlayerDataManager().getIfLoaded(player);
        if (data == null) {
            return "";
        }

        String param = params.toLowerCase();

        // 基础数据
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
            return data.hasSignedToday() ? "yes" : "no";
        }
        if ("fish_rewards".equals(param)) {
            return String.valueOf(data.getFishRewards());
        }
        if ("shop_purchases".equals(param)) {
            return String.valueOf(data.getShopPurchases());
        }

        // 材料数量（实时从背包读取）
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
        // 材料总数 — 适合排行榜按收集总量排序
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
