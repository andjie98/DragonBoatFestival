package com.andjie.dragonboatfestival.hook;

import com.andjie.dragonboatfestival.DragonBoatFestivalPlugin;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class VaultHook {

    private final DragonBoatFestivalPlugin plugin;
    private Object economy;
    private boolean enabled;
    private Method withdrawMethod;
    private Method depositMethod;
    private Method balanceMethod;
    private Method transactionSuccessMethod;

    public VaultHook(DragonBoatFestivalPlugin plugin) {
        this.plugin = plugin;
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            this.enabled = false;
            return;
        }
        try {
            Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
            Class<?> economyResponseClass = Class.forName("net.milkbowl.vault.economy.EconomyResponse");
            Object provider = plugin.getServer().getServicesManager().getRegistration(economyClass);
            if (provider == null) {
                this.enabled = false;
                return;
            }
            Method getProvider = provider.getClass().getMethod("getProvider");
            this.economy = getProvider.invoke(provider);
            this.balanceMethod = economyClass.getMethod("getBalance", org.bukkit.OfflinePlayer.class);
            this.withdrawMethod = economyClass.getMethod("withdrawPlayer", org.bukkit.OfflinePlayer.class, double.class);
            this.depositMethod = economyClass.getMethod("depositPlayer", org.bukkit.OfflinePlayer.class, double.class);
            this.transactionSuccessMethod = economyResponseClass.getMethod("transactionSuccess");
            this.enabled = true;
        } catch (Exception exception) {
            this.enabled = false;
            plugin.getLogger().warning("Vault 经济接入失败: " + exception.getMessage());
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public double getBalance(Player player) {
        if (!enabled) {
            return 0;
        }
        try {
            return (double) balanceMethod.invoke(economy, player);
        } catch (Exception exception) {
            plugin.getLogger().warning("Vault 查询余额失败(" + player.getName() + "): " + exception.getMessage());
            return 0;
        }
    }

    public boolean withdraw(Player player, double amount) {
        if (!enabled) {
            return false;
        }
        try {
            Object result = withdrawMethod.invoke(economy, player, amount);
            return (boolean) transactionSuccessMethod.invoke(result);
        } catch (Exception exception) {
            plugin.getLogger().warning("Vault 扣款失败(" + player.getName() + ", " + amount + "): " + exception.getMessage());
            return false;
        }
    }

    public boolean deposit(Player player, double amount) {
        if (!enabled) {
            return false;
        }
        try {
            Object result = depositMethod.invoke(economy, player, amount);
            return (boolean) transactionSuccessMethod.invoke(result);
        } catch (Exception exception) {
            plugin.getLogger().warning("Vault 存款失败(" + player.getName() + ", " + amount + "): " + exception.getMessage());
            return false;
        }
    }
}
