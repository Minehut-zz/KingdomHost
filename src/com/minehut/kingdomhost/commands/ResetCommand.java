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
public class ResetCommand extends Command {

    public ResetCommand(JavaPlugin plugin) {
        super(plugin, "reset", Rank.regular);
    }

    @Override
    public boolean call(Player player, ArrayList<String> args) {

        KingdomHost.getPlugin().getServerManager().resetServer(player);

        return false;
    }
}
