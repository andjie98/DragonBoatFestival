package com.andjie.dragonboatfestival.data;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private final String name;
    private int points;
    private int normalMade;
    private int luxuryMade;
    private int fishRewards;
    private int shopPurchases;
    private int signDays;
    private String lastSign;
    private boolean dirty;

    public PlayerData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        int newPoints = Math.max(0, points);
        if (this.points != newPoints) {
            this.points = newPoints;
            dirty = true;
        }
    }

    public void addPoints(int amount) {
        setPoints(points + amount);
    }

    public int getNormalMade() {
        return normalMade;
    }

    public void setNormalMade(int normalMade) {
        int newNormalMade = Math.max(0, normalMade);
        if (this.normalMade != newNormalMade) {
            this.normalMade = newNormalMade;
            dirty = true;
        }
    }

    public int getLuxuryMade() {
        return luxuryMade;
    }

    public void setLuxuryMade(int luxuryMade) {
        int newLuxuryMade = Math.max(0, luxuryMade);
        if (this.luxuryMade != newLuxuryMade) {
            this.luxuryMade = newLuxuryMade;
            dirty = true;
        }
    }

    public int getFishRewards() {
        return fishRewards;
    }

    public void setFishRewards(int fishRewards) {
        int newFishRewards = Math.max(0, fishRewards);
        if (this.fishRewards != newFishRewards) {
            this.fishRewards = newFishRewards;
            dirty = true;
        }
    }

    public int getShopPurchases() {
        return shopPurchases;
    }

    public void setShopPurchases(int shopPurchases) {
        int newShopPurchases = Math.max(0, shopPurchases);
        if (this.shopPurchases != newShopPurchases) {
            this.shopPurchases = newShopPurchases;
            dirty = true;
        }
    }

    public int getSignDays() {
        return signDays;
    }

    public void setSignDays(int signDays) {
        int newSignDays = Math.max(0, signDays);
        if (this.signDays != newSignDays) {
            this.signDays = newSignDays;
            dirty = true;
        }
    }

    public void addNormalMade() {
        addNormalMade(1);
    }

    public void addNormalMade(int amount) {
        setNormalMade(normalMade + amount);
    }

    public void addLuxuryMade() {
        addLuxuryMade(1);
    }

    public void addLuxuryMade(int amount) {
        setLuxuryMade(luxuryMade + amount);
    }

    public void addFishRewards() {
        setFishRewards(fishRewards + 1);
    }

    public void addShopPurchase() {
        setShopPurchases(shopPurchases + 1);
    }

    public void addSignDay() {
        setSignDays(signDays + 1);
    }

    public String getLastSign() {
        return lastSign;
    }

    public void setLastSign(String lastSign) {
        if (this.lastSign == null ? lastSign != null : !this.lastSign.equals(lastSign)) {
            this.lastSign = lastSign;
            dirty = true;
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    public void clearDirty() {
        dirty = false;
    }

    public boolean hasSignedToday() {
        return LocalDate.now().toString().equals(lastSign);
    }

    public boolean hasSignedToday(ZoneId zoneId) {
        return LocalDate.now(zoneId).toString().equals(lastSign);
    }
}
