package com.andjie.dragonboatfestival.goal;

import com.andjie.dragonboatfestival.Color;
import com.andjie.dragonboatfestival.DragonBoatFestivalPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class GoalManager {

    private final DragonBoatFestivalPlugin plugin;
    private final File file;
    private FileConfiguration data;
    private boolean dirty;

    public GoalManager(DragonBoatFestivalPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "goals.yml");
        reload();
    }

    public void reload() {
        data = YamlConfiguration.loadConfiguration(file);
        dirty = false;
    }

    public void save() {
        try {
            data.save(file);
            dirty = false;
        } catch (IOException exception) {
            plugin.getLogger().warning("无法保存 goals.yml: " + exception.getMessage());
        }
    }

    /**
     * 延迟批量保存：标记脏数据后，等 5 秒内没有新的更新再写入磁盘。
     */
    private void markDirty() {
        dirty = true;
        // 取消已有延迟任务，重新调度
        plugin.getServer().getScheduler().runTask(plugin, this::flush);
    }

    /**
     * 执行实际保存（供延迟任务调用）。
     */
    private void flush() {
        if (dirty) {
            save();
        }
    }

    public void addProgress(String key, int amount) {
        if (amount <= 0 || !plugin.getConfig().getBoolean("server-goals." + key + ".enabled", false)) {
            return;
        }
        if (data.getBoolean("goals." + key + ".completed", false)) {
            return;
        }
        int progress = data.getInt("goals." + key + ".progress", 0) + amount;
        int target = plugin.getConfig().getInt("server-goals." + key + ".target", 0);
        data.set("goals." + key + ".progress", progress);
        if (target > 0 && progress >= target) {
            complete(key, target);
        } else {
            markDirty();
        }
    }

    public int getProgress(String key) {
        return data.getInt("goals." + key + ".progress", 0);
    }

    public boolean isCompleted(String key) {
        return data.getBoolean("goals." + key + ".completed", false);
    }

    public Set<String> keys() {
        Set<String> keys = new HashSet<String>();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("server-goals");
        if (section != null) {
            keys.addAll(section.getKeys(false));
        }
        return keys;
    }

    private void complete(String key, int target) {
        data.set("goals." + key + ".progress", target);
        data.set("goals." + key + ".completed", true);
        String name = plugin.getConfig().getString("server-goals." + key + ".name", key);
        String message = plugin.getConfig().getString("server-goals." + key + ".complete-message", "&a全服任务完成：&e{name}");
        plugin.getServer().broadcastMessage(Color.text(message.replace("{name}", name).replace("{target}", String.valueOf(target))));
        for (String command : plugin.getConfig().getStringList("server-goals." + key + ".commands")) {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command.replace("{name}", name).replace("{target}", String.valueOf(target)));
        }
        save();
    }
}
