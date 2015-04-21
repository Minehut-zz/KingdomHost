package com.minehut.kingdomhost.commands.admin;

import com.minehut.api.managers.command.Command;
import com.minehut.api.util.player.Rank;
import com.minehut.commons.common.chat.C;
import com.minehut.kingdomhost.KingdomHost;
import com.minehut.kingdomhost.server.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

/**
 * Created by luke on 4/11/15.
 */
public class PortsCommand extends Command {

    public PortsCommand(JavaPlugin plugin) {
        super(plugin, "ports", Rank.admin);
    }

    @Override
    public boolean call(Player player, ArrayList<String> args) {

        player.sendMessage("");
        player.sendMessage(C.gray + "Current ports in use: ");
        player.sendMessage("");

        for (int i = 0; i < KingdomHost.getPlugin().getServerManager().getServers().size(); i++) {
            Server server = KingdomHost.getPlugin().getServerManager().getServers().get(i);

            player.sendMessage(Integer.toString(i + 1) + ") " + C.yellow + Integer.toString(server.getPort()) + C.gray + " [PID: " + C.white + Integer.toString(server.getPid()) + C.gray + ", Name: " + C.white + server.getKingdomName() + C.gray + "]");
        }

        return false;
    }
}

