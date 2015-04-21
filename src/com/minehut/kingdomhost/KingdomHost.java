package com.minehut.kingdomhost;

import com.minehut.kingdomhost.commands.CreateCommand;
import com.minehut.kingdomhost.commands.JoinCommand;
import com.minehut.kingdomhost.commands.RenameCommand;
import com.minehut.kingdomhost.commands.ResetCommand;
import com.minehut.kingdomhost.commands.admin.PortsCommand;
import com.minehut.kingdomhost.manager.ServerManager;
import com.minehut.kingdomhost.menu.CurrentServersManager;
import com.minehut.kingdomhost.menu.MyKingdom;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by luke on 4/9/15.
 */
public class KingdomHost extends JavaPlugin {
    private ServerManager serverManager;
    private static KingdomHost kingdomHost;
    private CurrentServersManager currentServersManager;
    private MyKingdom myKingdom;

    @Override
    public void onEnable() {
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.kingdomHost = this;
        this.serverManager = new ServerManager(this);
        this.currentServersManager = new CurrentServersManager(serverManager);
        this.myKingdom = new MyKingdom(serverManager);

        /* Commands */
        new CreateCommand(this);
        new JoinCommand(this);
        new RenameCommand(this);
        new ResetCommand(this);
        new PortsCommand(this);

    }

    @EventHandler
    public void onDisable() {
        for (com.minehut.kingdomhost.server.Server server : this.serverManager.getServers()) {
            server.forceShutdown();
        }
    }

    public static KingdomHost getPlugin() {
        return kingdomHost;
    }

    public ServerManager getServerManager() {
        return serverManager;
    }
}