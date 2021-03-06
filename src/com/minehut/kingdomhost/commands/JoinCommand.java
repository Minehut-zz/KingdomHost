package com.minehut.kingdomhost.commands;

import com.minehut.commons.common.chat.C;
import com.minehut.core.command.Command;
import com.minehut.core.player.Rank;
import com.minehut.kingdomhost.KingdomHost;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

/**
 * Created by luke on 4/11/15.
 */
public class JoinCommand extends Command {

    public JoinCommand(JavaPlugin plugin) {
        super(plugin, "join", Rank.regular);
    }

    @Override
    public boolean call(Player player, ArrayList<String> args) {

        if (args.size() == 1) {
            player.sendMessage("");
            player.sendMessage("Sending you to your requested server.");
            player.sendMessage("This may take " + C.aqua + "several moments" + C.white + " to complete.");
            player.sendMessage("");
            KingdomHost.getPlugin().getServerManager().connect(player, args.get(0));
        } else {
            player.sendMessage("Please use the format " + C.aqua + "/join (name)");
        }

        return false;
    }
}
