package com.minehut.kingdomhost.menu;

import com.minehut.commons.common.chat.C;
import com.minehut.commons.common.items.ItemStackFactory;
import com.minehut.kingdomhost.KingdomHost;
import com.minehut.kingdomhost.manager.ServerManager;
import com.minehut.kingdomhost.offline.OfflineServer;
import com.minehut.kingdomhost.server.Server;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Created by luke on 4/11/15.
 */
public class MyKingdom implements Listener {
    private KingdomHost host;
    private ServerManager serverManager;

    public MyKingdom(ServerManager serverManager) {
        this.serverManager = serverManager;
        this.host = serverManager.getHost();
        Bukkit.getServer().getPluginManager().registerEvents(this, this.host);

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getItemInHand() != null && player.getItemInHand().getItemMeta() != null) {
            if (player.getItemInHand().getItemMeta().getDisplayName() != null) {
                if (player.getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(getItem().getItemMeta().getDisplayName())) {
                    event.setCancelled(true);

                    OfflineServer server = serverManager.getServer(player);
                    if (serverManager.getServer(player) == null) {
                        /* Doesn't have a kingdom */
                        player.sendMessage("");
                        player.sendMessage("");
                        player.sendMessage(C.white + "You have not created a Kingdom yet.");
                        player.sendMessage(C.white + "Create one with " + C.aqua + "/create (name)");
                    } else {
                        player.sendMessage("");
                        player.sendMessage("Your kingdom is named " + C.aqua + server.getKingdomName());
                        player.sendMessage("Join your kingdom with " + C.aqua + "/join " + server.getKingdomName());
                        player.sendMessage("");
                    }
                }
            }
        }
    }

    public static ItemStack getItem() {
        return ItemStackFactory.createItem(Material.SIGN, C.yellow + C.bold + "Your Kingdom Info");
    }
}
