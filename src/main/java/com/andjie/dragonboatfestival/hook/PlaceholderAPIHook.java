package com.andjie.dragonboatfestival.hook;

import com.andjie.dragonboatfestival.DragonBoatFestivalPlugin;

/**
 * PlaceholderAPI 软依赖接入。
 * 运行时检测 PlaceholderAPI 是否存在，若存在则注册 DuanwuExpansion。
 */
public class PlaceholderAPIHook {

    private final DragonBoatFestivalPlugin plugin;
    private DuanwuExpansion expansion;
    private boolean enabled;

    public PlaceholderAPIHook(DragonBoatFestivalPlugin plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
        if (enabled) {
            register();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public DuanwuExpansion getExpansion() {
        return expansion;
    }

    private void register() {
        try {
            expansion = new DuanwuExpansion(plugin);
            expansion.register();
            plugin.getLogger().info("已注册 PlaceholderAPI 变量 (duanwu_*)");
        } catch (Exception exception) {
            plugin.getLogger().warning("PlaceholderAPI 注册失败: " + exception.getMessage());
            enabled = false;
        }
    }

    public void unregister() {
        if (expansion != null) {
            try {
                expansion.unregister();
                plugin.getLogger().info("已注销 PlaceholderAPI 变量 (duanwu_*)");
            } catch (Exception exception) {
                plugin.getLogger().warning("PlaceholderAPI 注销失败: " + exception.getMessage());
            }
        }
    }
}
