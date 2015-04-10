package com.minehut.kingdomhost;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by Luke on 10/18/14.
 */
public class KingdomShutdownEvent extends Event {
    int id;

    public KingdomShutdownEvent(int id) {
        this.id = id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

	private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
