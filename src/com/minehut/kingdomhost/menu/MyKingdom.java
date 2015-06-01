package com.minehut.kingdomhost.menu;

import com.minehut.commons.common.chat.C;
import com.minehut.commons.common.items.ItemStackFactory;
import com.minehut.core.Core;
import com.minehut.core.player.Rank;
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

import java.util.ArrayList;

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

                    ArrayList<OfflineServer> ownedServers = serverManager.getServer(player);
                    if (ownedServers.isEmpty()) {
                        /* Doesn't have a kingdom */
                        player.sendMessage("");
                        player.sendMessage(C.white + "You have not created a server yet.");
                        player.sendMessage(C.white + "Create one with " + C.aqua + "/create (name)");
                    } else {
                        if(Core.getInstance().getPlayerInfo(player).getRank().has(null, Rank.Ref, false)) {
                            player.sendMessage("");
                            player.sendMessage("Your owned servers: ");

                            for (int i = 0; i < ownedServers.size(); i++) {
                                player.sendMessage(Integer.toString(i + 1) + ") " + C.aqua + ownedServers.get(i).getKingdomName());
                            }
                            player.sendMessage("");

                        } else {
                            player.sendMessage("");
                            player.sendMessage("Your server is named " + C.aqua + ownedServers.get(0).getKingdomName());
                            player.sendMessage("Join your server with " + C.aqua + "/join " + ownedServers.get(0).getKingdomName());
                            player.sendMessage("");
                        }
                    }
                }
            }
        }
    }

    public static ItemStack getItem() {
        return ItemStackFactory.createItem(Material.SIGN, C.yellow + C.bold + "My Server Info");
    }
}
