package com.minehut.kingdomhost.commands;

import com.minehut.api.managers.command.Command;
import com.minehut.api.util.player.Rank;
import com.minehut.commons.common.chat.C;
import com.minehut.kingdomhost.KingdomHost;
import com.minehut.kingdomhost.offline.OfflineServer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

/**
 * Created by luke on 4/11/15.
 */
public class RenameCommand extends Command {

    public RenameCommand(JavaPlugin plugin) {
        super(plugin, "rename", Rank.regular);
    }

    @Override
    public boolean call(Player player, ArrayList<String> args) {

        ArrayList<OfflineServer> ownedServers = KingdomHost.getPlugin().getServerManager().getServer(player);
        if(ownedServers.size() <= 1) {
            if (args.size() == 1) {
                KingdomHost.getPlugin().getServerManager().changeName(player, args.get(0));
            } else {
                player.sendMessage("Please use the format " + C.red + "/rename (name)");
            }
        } else {
            if (args.size() == 2) {
                KingdomHost.getPlugin().getServerManager().changeName(player, args.get(0), args.get(1));
            } else {
                player.sendMessage("Please use the format " + C.red + "/rename (server) (new-name)");
            }
        }

        return true;
    }
}
