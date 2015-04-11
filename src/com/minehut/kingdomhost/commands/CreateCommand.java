package com.minehut.kingdomhost.commands;

import com.minehut.api.managers.command.Command;
import com.minehut.api.util.player.Rank;
import com.minehut.commons.common.chat.C;
import com.minehut.kingdomhost.KingdomHost;
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
            KingdomHost.getPlugin().getServerManager().createServer(player, args.get(0));
        } else {
            player.sendMessage("Please use the format " + C.red + "/create (name)");
        }

        return false;
    }
}
