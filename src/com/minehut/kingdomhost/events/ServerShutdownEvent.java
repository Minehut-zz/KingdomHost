package com.minehut.kingdomhost.events;

import com.minehut.kingdomhost.server.Server;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by Luke on 10/18/14.
 */
public class ServerShutdownEvent extends Event {
    private Server server;

    public ServerShutdownEvent(Server server) {
        this.server = server;
    }

    public Server getServer() {
        return server;
    }

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
