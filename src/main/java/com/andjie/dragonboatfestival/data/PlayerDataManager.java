package com.andjie.dragonboatfestival.data;

import com.andjie.dragonboatfestival.DragonBoatFestivalPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private final DragonBoatFestivalPlugin plugin;
    private final File dataFolder;
    private final Map<UUID, PlayerData> dataByUuid;

    public PlayerDataManager(DragonBoatFestivalPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        this.dataByUuid = new ConcurrentHashMap<UUID, PlayerData>();
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

    /**
     * 只返回已加载的数据，不触发文件读取（线程安全，供 PlaceholderAPI 等异步调用使用）。
     */
    public PlayerData getIfLoaded(Player player) {
        return dataByUuid.get(player.getUniqueId());
    }

    /**
     * 获取或加载玩家数据，支持离线玩家。
     * 优先返回内存缓存；未命中时从 playerdata/&lt;UUID&gt;.yml 加载并放入缓存。
     * 文件不存在时返回全 0 默认数据且不缓存，避免无意义条目堆积。
     * 供 PlaceholderAPI / ajLeaderboards 等需要读取离线玩家数据的场景使用。
     */
    public PlayerData getOrLoad(OfflinePlayer player) {
        UUID uuid = player.getUniqueId();
        PlayerData cached = dataByUuid.get(uuid);
        if (cached != null) {
            return cached;
        }
        File file = fileOf(uuid);
        if (!file.exists()) {
            return new PlayerData(uuid, player.getName());
        }
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            PlayerData data = new PlayerData(uuid, config.getString("name", player.getName()));
            data.setPoints(config.getInt("points", 0));
            data.setNormalMade(config.getInt("stats.normalMade", 0));
            data.setLuxuryMade(config.getInt("stats.luxuryMade", 0));
            data.setFishRewards(config.getInt("stats.fishRewards", 0));
            data.setShopPurchases(config.getInt("stats.shopPurchases", 0));
            data.setSignDays(config.getInt("stats.signDays", 0));
            data.setLastSign(config.getString("lastSign"));
            data.clearDirty();
            dataByUuid.put(uuid, data);
            return data;
        } catch (Exception exception) {
            plugin.getLogger().warning("离线加载玩家数据失败 " + uuid + ": " + exception.getMessage());
            return new PlayerData(uuid, player.getName());
        }
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
            data.clearDirty();
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
        if (data != null && data.isDirty()) {
            save(data);
        }
    }

    public void saveAll() {
        Collection<PlayerData> values = dataByUuid.values();
        for (PlayerData data : values) {
            if (data.isDirty()) {
                save(data);
            }
        }
        // 清理离线且非脏的缓存条目，避免历史玩家无限堆积。
        // 离线玩家被 PlaceholderAPI/ajLeaderboards 请求时会在下次 getOrLoad 重新加载。
        values.removeIf(data -> !data.isDirty() && Bukkit.getPlayer(data.getUuid()) == null);
    }

    public void save(PlayerData data) {
        File file = fileOf(data.getUuid());
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
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
            data.clearDirty();
        } catch (IOException exception) {
            plugin.getLogger().warning("无法保存玩家数据 " + data.getUuid() + ": " + exception.getMessage());
        }
    }

    private File fileOf(UUID uuid) {
        return new File(dataFolder, uuid.toString() + ".yml");
    }
}
