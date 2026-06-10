package com.andjie.dragonboatfestival.data;

import com.andjie.dragonboatfestival.DragonBoatFestivalPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private final DragonBoatFestivalPlugin plugin;
    private final File dataFolder;
    private final Map<UUID, PlayerData> dataByUuid;

    public PlayerDataManager(DragonBoatFestivalPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        this.dataByUuid = new HashMap<UUID, PlayerData>();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public void loadOnlinePlayers() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            load(player);
        }
    }

    public PlayerData get(Player player) {
        PlayerData data = dataByUuid.get(player.getUniqueId());
        if (data == null) {
            data = load(player);
        }
        return data;
    }

    public PlayerData load(Player player) {
        try {
            File file = fileOf(player.getUniqueId());
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            PlayerData data = new PlayerData(player.getUniqueId(), player.getName());
            data.setPoints(config.getInt("points", 0));
            data.setNormalMade(config.getInt("stats.normalMade", 0));
            data.setLuxuryMade(config.getInt("stats.luxuryMade", 0));
            data.setFishRewards(config.getInt("stats.fishRewards", 0));
            data.setShopPurchases(config.getInt("stats.shopPurchases", 0));
            data.setSignDays(config.getInt("stats.signDays", 0));
            data.setLastSign(config.getString("lastSign"));
            dataByUuid.put(player.getUniqueId(), data);
            return data;
        } catch (Exception exception) {
            plugin.getLogger().warning("加载玩家数据失败 " + player.getUniqueId() + ": " + exception.getMessage());
            PlayerData data = new PlayerData(player.getUniqueId(), player.getName());
            dataByUuid.put(player.getUniqueId(), data);
            return data;
        }
    }

    public void unload(Player player) {
        PlayerData data = dataByUuid.remove(player.getUniqueId());
        if (data != null) {
            save(data);
        }
    }

    public void saveAll() {
        Collection<PlayerData> values = dataByUuid.values();
        for (PlayerData data : values) {
            save(data);
        }
    }

    public void save(PlayerData data) {
        File file = fileOf(data.getUuid());
        FileConfiguration config = new YamlConfiguration();
        config.set("name", data.getName());
        config.set("points", data.getPoints());
        config.set("stats.normalMade", data.getNormalMade());
        config.set("stats.luxuryMade", data.getLuxuryMade());
        config.set("stats.fishRewards", data.getFishRewards());
        config.set("stats.shopPurchases", data.getShopPurchases());
        config.set("stats.signDays", data.getSignDays());
        config.set("lastSign", data.getLastSign());
        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().warning("无法保存玩家数据 " + data.getUuid() + ": " + exception.getMessage());
        }
    }

    private File fileOf(UUID uuid) {
        return new File(dataFolder, uuid.toString() + ".yml");
    }
}
