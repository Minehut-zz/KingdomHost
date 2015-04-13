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
public class CreateCommand extends Command {

    public CreateCommand(JavaPlugin plugin) {
        super(plugin, "create", Rank.regular);
    }

    @Override
    public boolean call(Player player, ArrayList<String> args) {

        if (args.size() == 1) {

            OfflineServer offlineServer = KingdomHost.getPlugin().getServerManager().getServer(player);
            if (offlineServer == null) {
                KingdomHost.getPlugin().getServerManager().createServer(player, args.get(0));
            } else {
                player.sendMessage("");
                player.sendMessage("You already have a kingdom called " + C.aqua + offlineServer.getKingdomName());
                player.sendMessage("");
            }
        } else {
            player.sendMessage("Please use the format " + C.aqua + "/create (name)");
        }

        return false;
    }
}
