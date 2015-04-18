package com.minehut.kingdomhost.commands;

import com.minehut.api.managers.command.Command;
import com.minehut.api.util.player.Rank;
import com.minehut.commons.common.chat.C;
import com.minehut.commons.common.items.ItemStackFactory;
import com.minehut.kingdomhost.KingdomHost;
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



        return false;
    }

    @EventHandler
    public void onPageClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (event.getInventory().getName().equalsIgnoreCase(this.confirm.getName())) {
            event.setCancelled(true);

            /* Empty Slot Click */
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
