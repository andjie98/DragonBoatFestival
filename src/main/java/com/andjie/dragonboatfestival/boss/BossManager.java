package com.andjie.dragonboatfestival.boss;

import com.andjie.dragonboatfestival.Color;
import com.andjie.dragonboatfestival.DragonBoatFestivalPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

public class BossManager {

    private static final String BOSS_METADATA_KEY = "DragonBoatFestival_Boss";

    private final DragonBoatFestivalPlugin plugin;

    public BossManager(DragonBoatFestivalPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean spawn(Player player) {
        if (!plugin.getConfig().getBoolean("boss.enabled", true)) {
            player.sendMessage(plugin.message("boss-disabled"));
            return false;
        }
        EntityType type;
        try {
            type = EntityType.valueOf(plugin.getConfig().getString("boss.type", "ZOMBIE").toUpperCase());
        } catch (IllegalArgumentException exception) {
            type = EntityType.ZOMBIE;
        }
        Location location = player.getLocation();
        Entity entity = player.getWorld().spawnEntity(location, type);
        if (!(entity instanceof LivingEntity)) {
            entity.remove();
            player.sendMessage(Color.text("&c该实体类型不能作为 BOSS。"));
            return false;
        }
        LivingEntity boss = (LivingEntity) entity;
        String name = Color.text(plugin.getConfig().getString("boss.name", "&2端午粽王"));
        boss.setCustomName(name);
        boss.setCustomNameVisible(true);
        double health = plugin.getConfig().getDouble("boss.health", 200.0D);
        health = Math.max(1.0D, health);
        boss.setMaxHealth(health);
        boss.setHealth(boss.getMaxHealth());
        boss.setMetadata(BOSS_METADATA_KEY, new FixedMetadataValue(plugin, true));
        plugin.getServer().broadcastMessage(plugin.message("boss-spawned").replace("{name}", name));
        return true;
    }

    public boolean isBoss(Entity entity) {
        if (!entity.hasMetadata(BOSS_METADATA_KEY)) {
            return false;
        }
        for (MetadataValue value : entity.getMetadata(BOSS_METADATA_KEY)) {
            if (value.getOwningPlugin() == plugin && value.asBoolean()) {
                return true;
            }
        }
        return false;
    }

    public void reward(Player killer) {
        int points = Math.max(0, plugin.getConfig().getInt("boss.reward-points", 20));
        if (points > 0) {
            plugin.getPlayerDataManager().get(killer).addPoints(points);
        }
        for (String command : plugin.getConfig().getStringList("boss.commands")) {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command.replace("{player}", killer.getName()));
        }
        plugin.getGoalManager().addProgress("boss-kill", 1);
        plugin.getServer().broadcastMessage(plugin.message("boss-killed")
            .replace("{player}", killer.getName())
            .replace("{points}", String.valueOf(points)));
    }
}
