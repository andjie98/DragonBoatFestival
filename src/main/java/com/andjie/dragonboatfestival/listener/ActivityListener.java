package com.andjie.dragonboatfestival.listener;

import com.andjie.dragonboatfestival.Color;
import com.andjie.dragonboatfestival.DragonBoatFestivalPlugin;
import com.andjie.dragonboatfestival.data.MaterialItems;
import com.andjie.dragonboatfestival.data.MaterialType;
import com.andjie.dragonboatfestival.data.PlayerData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.CropState;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.material.Crops;

import java.util.Random;

public class ActivityListener implements Listener {

    private final DragonBoatFestivalPlugin plugin;
    private final Random random;

    public ActivityListener(DragonBoatFestivalPlugin plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        plugin.getPlayerDataManager().load(event.getPlayer());
        if (!plugin.isFestivalEnabled() || !plugin.getConfig().getBoolean("guide.join-message", true)) {
            return;
        }
        plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                Player player = event.getPlayer();
                if (!player.isOnline()) {
                    return;
                }
                for (String line : plugin.getConfig().getStringList("guide.join-lines")) {
                    player.sendMessage(Color.text(line));
                }
            }
        }, Math.max(1L, plugin.getConfig().getLong("guide.join-delay-seconds", 3L)) * 20L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getPlayerDataManager().unload(event.getPlayer());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        try {
            if (!plugin.isFestivalEnabled() || event.isCancelled()) {
                return;
            }
            Block block = event.getBlock();
            MaterialType reward = null;
            if (isOre(block.getType())) {
                reward = MaterialType.RICE;
            } else if (block.getType().name().contains("LEAVES")) {
                reward = MaterialType.LEAF;
            } else if (isMatureCrop(block)) {
                reward = MaterialType.JUJUBE;
            }
            if (reward != null) {
                tryGiveDrop(event.getPlayer(), reward);
            }
        } catch (Exception exception) {
            plugin.getLogger().warning("BlockBreak 事件处理异常: " + exception.getMessage());
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        try {
            if (!plugin.isFestivalEnabled() || event.getEntity().getKiller() == null) {
                return;
            }
            if (plugin.getBossManager().isBoss(event.getEntity())) {
                plugin.getBossManager().reward(event.getEntity().getKiller());
                return;
            }
            if (event.getEntity() instanceof Monster) {
                tryGiveDrop(event.getEntity().getKiller(), MaterialType.RICE);
            } else if (event.getEntity() instanceof Animals) {
                tryGiveDrop(event.getEntity().getKiller(), MaterialType.MEAT);
            }
        } catch (Exception exception) {
            plugin.getLogger().warning("EntityDeath 事件处理异常: " + exception.getMessage());
        }
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        try {
            if (!plugin.isFestivalEnabled() || event.isCancelled() || event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
                return;
            }
            Player player = event.getPlayer();
            int chance = Math.max(0, Math.min(100, plugin.getConfig().getInt("fish-rewards.chance", 100)));
            if (chance <= 0 || random.nextInt(100) >= chance) {
                return;
            }
            int rice = plugin.getConfig().getInt("fish-rewards.rice", 0);
            int leaf = plugin.getConfig().getInt("fish-rewards.leaf", 0);
            int jujube = plugin.getConfig().getInt("fish-rewards.jujube", 0);
            int meat = plugin.getConfig().getInt("fish-rewards.meat", 0);
            int lucky = plugin.getConfig().getInt("fish-rewards.lucky", 0);
            int legend = plugin.getConfig().getInt("fish-rewards.legend", 0);
            int total = rice + leaf + jujube + meat + lucky + legend;
            if (total <= 0) {
                return;
            }
            int roll = random.nextInt(total) + 1;
            if (roll <= rice) {
                giveMaterial(player, MaterialType.RICE, 1);
                addFishReward(player);
                return;
            }
            roll -= rice;
            if (roll <= leaf) {
                giveMaterial(player, MaterialType.LEAF, 1);
                addFishReward(player);
                return;
            }
            roll -= leaf;
            if (roll <= jujube) {
                giveMaterial(player, MaterialType.JUJUBE, 1);
                addFishReward(player);
                return;
            }
            roll -= jujube;
            if (roll <= meat) {
                giveMaterial(player, MaterialType.MEAT, 1);
                addFishReward(player);
                return;
            }
            roll -= meat;
            if (roll <= lucky) {
                runFishCommands(player, "lucky");
                addFishReward(player);
                player.sendMessage(plugin.message("fish-lucky"));
                return;
            }
            runFishCommands(player, "legend");
            addFishReward(player);
            player.sendMessage(plugin.message("fish-legend"));
        } catch (Exception exception) {
            plugin.getLogger().warning("Fish 事件处理异常: " + exception.getMessage());
        }
    }

    private void tryGiveDrop(Player player, MaterialType type) {
        int chance = plugin.getConfig().getInt("drop." + type.getKey(), 0);
        if (chance > 0 && random.nextInt(100) < chance) {
            giveMaterial(player, type, 1);
        }
    }

    private void giveMaterial(Player player, MaterialType type, int amount) {
        MaterialItems.give(player, type, amount);
        player.sendMessage(Color.text("&a[端午节]&r &a获得材料：&e" + type.getDisplayName() + " x" + amount));
    }

    private void addFishReward(Player player) {
        PlayerData data = plugin.getPlayerDataManager().get(player);
        data.addFishRewards();
        plugin.getGoalManager().addProgress("fish-reward", 1);
    }

    private void runFishCommands(Player player, String key) {
        for (String command : plugin.getConfig().getStringList("fish-commands." + key)) {
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command.replace("{player}", player.getName()));
        }
    }

    private boolean isOre(Material material) {
        return material.name().endsWith("_ORE");
    }

    private boolean isCrop(Material material) {
        return material == Material.CROPS
            || material == Material.CARROT
            || material == Material.POTATO
            || material == Material.BEETROOT_BLOCK
            || material == Material.MELON_BLOCK
            || material == Material.PUMPKIN;
    }

    private boolean isMatureCrop(Block block) {
        Material material = block.getType();
        if (material == Material.MELON_BLOCK || material == Material.PUMPKIN) {
            return true;
        }
        if (!isCrop(material)) {
            return false;
        }
        if (block.getState().getData() instanceof Crops) {
            Crops crops = (Crops) block.getState().getData();
            return crops.getState() == CropState.RIPE;
        }
        return block.getData() >= 7;
    }
}
