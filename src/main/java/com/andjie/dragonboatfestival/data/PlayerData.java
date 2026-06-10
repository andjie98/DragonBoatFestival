package com.andjie.dragonboatfestival.data;

import java.time.LocalDate;
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
        this.points = Math.max(0, points);
    }

    public void addPoints(int amount) {
        setPoints(points + amount);
    }

    public int getNormalMade() {
        return normalMade;
    }

    public void setNormalMade(int normalMade) {
        this.normalMade = Math.max(0, normalMade);
    }

    public int getLuxuryMade() {
        return luxuryMade;
    }

    public void setLuxuryMade(int luxuryMade) {
        this.luxuryMade = Math.max(0, luxuryMade);
    }

    public int getFishRewards() {
        return fishRewards;
    }

    public void setFishRewards(int fishRewards) {
        this.fishRewards = Math.max(0, fishRewards);
    }

    public int getShopPurchases() {
        return shopPurchases;
    }

    public void setShopPurchases(int shopPurchases) {
        this.shopPurchases = Math.max(0, shopPurchases);
    }

    public int getSignDays() {
        return signDays;
    }

    public void setSignDays(int signDays) {
        this.signDays = Math.max(0, signDays);
    }

    public void addNormalMade() {
        normalMade++;
    }

    public void addLuxuryMade() {
        luxuryMade++;
    }

    public void addFishRewards() {
        fishRewards++;
    }

    public void addShopPurchase() {
        shopPurchases++;
    }

    public void addSignDay() {
        signDays++;
    }

    public String getLastSign() {
        return lastSign;
    }

    public void setLastSign(String lastSign) {
        this.lastSign = lastSign;
    }

    public boolean hasSignedToday() {
        return LocalDate.now().toString().equals(lastSign);
    }
}
