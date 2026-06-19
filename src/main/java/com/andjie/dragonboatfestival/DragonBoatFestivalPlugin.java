package com.andjie.dragonboatfestival;

import com.andjie.dragonboatfestival.boss.BossManager;
import com.andjie.dragonboatfestival.command.DuanwuCommand;
import com.andjie.dragonboatfestival.data.PlayerDataManager;
import com.andjie.dragonboatfestival.goal.GoalManager;
import com.andjie.dragonboatfestival.hook.PlaceholderAPIHook;
import com.andjie.dragonboatfestival.hook.TrMenuHook;
import com.andjie.dragonboatfestival.hook.VaultHook;
import com.andjie.dragonboatfestival.listener.ActivityListener;
import com.andjie.dragonboatfestival.service.ShopService;
import com.andjie.dragonboatfestival.service.ZongziService;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.time.ZoneId;

public class DragonBoatFestivalPlugin extends JavaPlugin {

    private PlayerDataManager playerDataManager;
    private ShopService shopService;
    private ZongziService zongziService;
    private GoalManager goalManager;
    private BossManager bossManager;
    private DuanwuCommand duanwuCommand;
    private PlaceholderAPIHook placeholderAPIHook;
    private TrMenuHook trMenuHook;
    private VaultHook vaultHook;
    private BukkitTask autoSaveTask;
    private FileConfiguration messages;
    private FileConfiguration shopConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResourceIfMissing("messages.yml");
        saveResourceIfMissing("shop.yml");

        reloadLocalConfigs();

        playerDataManager = new PlayerDataManager(this);
        playerDataManager.loadOnlinePlayers();
        goalManager = new GoalManager(this);
        bossManager = new BossManager(this);
        shopService = new ShopService(this);
        zongziService = new ZongziService(this);

        duanwuCommand = new DuanwuCommand(this);
        getCommand("duanwu").setExecutor(duanwuCommand);
        getCommand("duanwu").setTabCompleter(duanwuCommand);
        getServer().getPluginManager().registerEvents(new ActivityListener(this), this);
        getServer().getPluginManager().registerEvents(zongziService, this);

        placeholderAPIHook = new PlaceholderAPIHook(this);
        trMenuHook = new TrMenuHook(this);
        vaultHook = new VaultHook(this);
        if (placeholderAPIHook.isEnabled()) {
            getLogger().info("已接入 PlaceholderAPI");
        }
        if (trMenuHook.isEnabled()) {
            getLogger().info("已启用 TrMenu 菜单界面");
        }
        if (vaultHook.isEnabled()) {
            getLogger().info("已接入 Vault 经济");
        }

        long period = Math.max(60L, getConfig().getLong("auto-save-seconds", 300L)) * 20L;
        autoSaveTask = getServer().getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                playerDataManager.saveAll();
            }
        }, period, period);

        getLogger().info("DragonBoatFestival 已启用");
    }

    @Override
    public void onDisable() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }
        if (placeholderAPIHook != null) {
            placeholderAPIHook.unregister();
        }
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }
        if (goalManager != null) {
            goalManager.save();
        }
        getLogger().info("DragonBoatFestival 已禁用");
    }

    public void reloadPlugin() {
        reloadConfig();
        reloadLocalConfigs();
        if (trMenuHook != null) {
            trMenuHook.reload();
        }
        shopService.reload();
        goalManager.reload();
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public GoalManager getGoalManager() {
        return goalManager;
    }

    public BossManager getBossManager() {
        return bossManager;
    }

    public ShopService getShopService() {
        return shopService;
    }

    public ZongziService getZongziService() {
        return zongziService;
    }

    public DuanwuCommand getDuanwuCommand() {
        return duanwuCommand;
    }

    public PlaceholderAPIHook getPlaceholderAPIHook() {
        return placeholderAPIHook;
    }

    public VaultHook getVaultHook() {
        return vaultHook;
    }

    public TrMenuHook getTrMenuHook() {
        return trMenuHook;
    }

    public void openMainMenu(org.bukkit.entity.Player player) {
        openTrMenu(player, TrMenuHook.MAIN_MENU);
    }

    public void openMakeMenu(org.bukkit.entity.Player player) {
        openTrMenu(player, TrMenuHook.MAKE_MENU);
    }

    public void openShopMenu(org.bukkit.entity.Player player) {
        openTrMenu(player, TrMenuHook.SHOP_MENU);
    }

    private void openTrMenu(org.bukkit.entity.Player player, String menuId) {
        if (trMenuHook == null || !trMenuHook.open(player, menuId)) {
            player.sendMessage(Color.text("&c端午活动菜单需要安装并启用 TrMenu。"));
        }
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public FileConfiguration getShopConfig() {
        return shopConfig;
    }

    public boolean isFestivalEnabled() {
        return getConfig().getBoolean("festival.enabled", true);
    }

    public ZoneId getSignZoneId() {
        String zone = getConfig().getString("sign-time-zone", ZoneId.systemDefault().getId());
        try {
            return ZoneId.of(zone);
        } catch (Exception exception) {
            getLogger().warning("sign-time-zone 配置无效，已使用服务器默认时区: " + zone);
            return ZoneId.systemDefault();
        }
    }

    public String message(String path) {
        String raw = messages.getString(path, path);
        return Color.text(messages.getString("prefix", "") + raw);
    }

    public String rawMessage(String path) {
        return Color.text(messages.getString(path, path));
    }

    private void reloadLocalConfigs() {
        messages = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
        shopConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "shop.yml"));
    }

    private void saveResourceIfMissing(String name) {
        File file = new File(getDataFolder(), name);
        if (!file.exists()) {
            saveResource(name, false);
        }
    }
}
