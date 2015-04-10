package com.minehut.kingdomhost;

import java.util.UUID;

/**
 * Created by Luke on 1/22/15.
 */
public class KingdomInfo {
	int id;
	String name;
	int maxPlayers;
	int borderSize;
	int maxPlugins;
	UUID ownerUUID;

	public KingdomInfo(UUID ownerUUID, int id, String name, int maxPlayers, int borderSize, int maxPlugins) {
		this.ownerUUID = ownerUUID;
		this.id = id;
		this.name = name;
		this.maxPlayers = maxPlayers;
		this.borderSize = borderSize;
		this.maxPlugins = maxPlugins;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UUID getOwnerUUID() {
		return ownerUUID;
	}

	public void setOwnerUUID(UUID ownerUUID) {
		this.ownerUUID = ownerUUID;
	}

	public int getMaxPlayers() {
		return maxPlayers;
	}

	public void setMaxPlayers(int maxPlayers) {
		this.maxPlayers = maxPlayers;
	}

	public int getBorderSize() {
		return borderSize;
	}

	public void setBorderSize(int borderSize) {
		this.borderSize = borderSize;
	}

	public int getMaxPlugins() {
		return maxPlugins;
	}

	public void setMaxPlugins(int maxPlugins) {
		this.maxPlugins = maxPlugins;
	}
}
