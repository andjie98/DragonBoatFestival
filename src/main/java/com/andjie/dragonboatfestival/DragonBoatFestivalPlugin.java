package com.andjie.dragonboatfestival;

import com.andjie.dragonboatfestival.boss.BossManager;
import com.andjie.dragonboatfestival.command.DuanwuCommand;
import com.andjie.dragonboatfestival.data.PlayerDataManager;
import com.andjie.dragonboatfestival.goal.GoalManager;
import com.andjie.dragonboatfestival.gui.MainMenu;
import com.andjie.dragonboatfestival.gui.MakeMenu;
import com.andjie.dragonboatfestival.gui.ShopMenu;
import com.andjie.dragonboatfestival.hook.PlaceholderAPIHook;
import com.andjie.dragonboatfestival.hook.VaultHook;
import com.andjie.dragonboatfestival.listener.ActivityListener;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;

public class DragonBoatFestivalPlugin extends JavaPlugin {

    private PlayerDataManager playerDataManager;
    private ShopMenu shopMenu;
    private MakeMenu makeMenu;
    private MainMenu mainMenu;
    private GoalManager goalManager;
    private BossManager bossManager;
    private DuanwuCommand duanwuCommand;
    private PlaceholderAPIHook placeholderAPIHook;
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
        shopMenu = new ShopMenu(this);
        makeMenu = new MakeMenu(this);
        mainMenu = new MainMenu(this);

        duanwuCommand = new DuanwuCommand(this);
        getCommand("duanwu").setExecutor(duanwuCommand);
        getCommand("duanwu").setTabCompleter(duanwuCommand);
        getServer().getPluginManager().registerEvents(new ActivityListener(this), this);
        getServer().getPluginManager().registerEvents(shopMenu, this);
        getServer().getPluginManager().registerEvents(makeMenu, this);
        getServer().getPluginManager().registerEvents(mainMenu, this);

        placeholderAPIHook = new PlaceholderAPIHook(this);
        vaultHook = new VaultHook(this);
        if (placeholderAPIHook.isEnabled()) {
            getLogger().info("已接入 PlaceholderAPI");
        }
        if (vaultHook.isEnabled()) {
            getLogger().info("已接入 Vault 经济");
        }

        long period = Math.max(60L, getConfig().getLong("auto-save-seconds", 300L)) * 20L;
        autoSaveTask = getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {
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
        shopMenu.reload();
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

    public ShopMenu getShopMenu() {
        return shopMenu;
    }

    public MakeMenu getMakeMenu() {
        return makeMenu;
    }

    public MainMenu getMainMenu() {
        return mainMenu;
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

    public FileConfiguration getMessages() {
        return messages;
    }

    public FileConfiguration getShopConfig() {
        return shopConfig;
    }

    public boolean isFestivalEnabled() {
        return getConfig().getBoolean("festival.enabled", true);
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
