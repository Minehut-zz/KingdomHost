package com.minehut.kingdomhost;

import com.minehut.kingdomhost.commands.CreateCommand;
import com.minehut.kingdomhost.commands.JoinCommand;
import com.minehut.kingdomhost.commands.RenameCommand;
import com.minehut.kingdomhost.manager.ServerManager;
import com.minehut.kingdomhost.menu.CurrentServersManager;
import com.minehut.kingdomhost.menu.MyKingdom;
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
        this.kingdomHost = this;
        this.serverManager = new ServerManager(this);
        this.currentServersManager = new CurrentServersManager(serverManager);
        this.myKingdom = new MyKingdom(serverManager);

        /* Commands */
        new CreateCommand(this);
        new JoinCommand(this);
        new RenameCommand(this);
    }

    public static KingdomHost getPlugin() {
        return kingdomHost;
    }

    public ServerManager getServerManager() {
        return serverManager;
    }
}