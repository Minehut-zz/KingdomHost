package com.minehut.kingdomhost.offline;

import java.util.UUID;

/**
 * Created by luke on 4/11/15.
 */
public class OfflineServer {
    private int id;
    private String kingdomName;
    private UUID ownerUUID;
    private int maxPlayers;
    private int borderSize;
    private int maxPlugins;
    private int ram;

    public OfflineServer(int id, String kingdomName, UUID ownerUUID, int maxPlayers, int borderSize, int maxPlugins, int ram) {
        this.id = id;
        this.kingdomName = kingdomName;
        this.ownerUUID = ownerUUID;
        this.maxPlayers = maxPlayers;
        this.borderSize = borderSize;
        this.maxPlugins = maxPlugins;
        this.ram = ram;
    }

    public int getId() {
        return id;
    }

    public String getKingdomName() {
        return kingdomName;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getBorderSize() {
        return borderSize;
    }

    public int getMaxPlugins() {
        return maxPlugins;
    }

    public void setKingdomName(String kingdomName) {
        this.kingdomName = kingdomName;
    }

    public void setBorderSize(int borderSize) {
        this.borderSize = borderSize;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public void setMaxPlugins(int maxPlugins) {
        this.maxPlugins = maxPlugins;
    }

    public int getRam() {
        return ram;
    }

    public void setRam(int ram) {
        this.ram = ram;
    }
}
