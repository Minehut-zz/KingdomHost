package com.minehut.kingdomhost.commands;

import com.minehut.commons.common.chat.C;
import com.minehut.commons.common.items.ItemStackFactory;
import com.minehut.core.command.Command;
import com.minehut.core.player.Rank;
import com.minehut.kingdomhost.KingdomHost;
import com.minehut.kingdomhost.offline.OfflineServer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

/**
 * Created by luke on 4/11/15.
 */
public class ResetCommand extends Command {
    private Inventory confirm;

    public ResetCommand(JavaPlugin plugin) {
        super(plugin, "reset", Rank.regular);

        this.createConfirmPage();
    }

    @Override
    public boolean call(Player player, ArrayList<String> args) {

        ArrayList<OfflineServer> ownedServers = KingdomHost.getPlugin().getServerManager().getServer(player);

            /* Owns multiple servers */
        if(ownedServers.size() > 1) {
            if(args.size() != 1) {
                player.sendMessage("");
                player.sendMessage("You must specify which of your servers to reset.");
                player.sendMessage("Example: " + C.aqua + "/reset (name)");
                player.sendMessage("");
            } else {
                KingdomHost.getPlugin().getServerManager().resetServer(player, args.get(0));
            }

            /* Only owns one server */
        } else {
            player.openInventory(confirm);
        }

        return true;
    }

    @EventHandler
    public void onPageClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (event.getInventory().getName().equalsIgnoreCase(this.confirm.getName())) {
            event.setCancelled(true);

            /* Empty Slot Click */
            if(event.getCurrentItem() == null) return;
            if(event.getCurrentItem().getType() == null) return;

            /* Plugins Button */
            if (event.getCurrentItem().getType() == Material.EMERALD_BLOCK) {
                KingdomHost.getPlugin().getServerManager().resetServer(player);
                player.closeInventory();
                return;
            }
            else if (event.getCurrentItem().getType() == Material.REDSTONE_BLOCK) {
                player.closeInventory();
                return;
            }

        }
    }

    private void createConfirmPage() {
        this.confirm = Bukkit.getServer().createInventory(null, 9, "Reset Confirmation");

        /* Yes */
        this.confirm.setItem(2, ItemStackFactory.createItem(Material.EMERALD_BLOCK, C.green + "Yes, reset my server!"));

        /* No */
        this.confirm.setItem(6, ItemStackFactory.createItem(Material.REDSTONE_BLOCK, C.red + "No, don't reset my server!"));
    }
}
