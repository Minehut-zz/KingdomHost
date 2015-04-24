package com.minehut.kingdomhost.menu;

import com.minehut.commons.common.chat.C;
import com.minehut.commons.common.items.ItemStackFactory;
import com.minehut.kingdomhost.KingdomHost;
import com.minehut.kingdomhost.manager.ServerManager;
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

import java.util.Arrays;

/**
 * Created by luke on 4/11/15.
 */
public class CurrentServersManager implements Listener {
    private KingdomHost host;
    private ServerManager serverManager;
    private Inventory menu;

    public CurrentServersManager(ServerManager serverManager) {
        this.serverManager = serverManager;
        this.host = serverManager.getHost();
        Bukkit.getServer().getPluginManager().registerEvents(this, this.host);

        this.menu = Bukkit.getServer().createInventory(null, 36, C.underline + "Player Servers");
        this.updateMenu();
    }

    private void updateMenu() {
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.host, new Runnable() {
            @Override
            public void run() {
                menu.clear();

                for (Server server : serverManager.getServers()) {
                    ItemStack item = ItemStackFactory.createItem(Material.SIGN, server.getKingdomName(),
                            Arrays.asList(
                                    "",
                                    C.gray + "Name: " + C.yellow + server.getKingdomName(),
                                    C.gray + "Players: " + C.yellow + Integer.toString(server.getCurrentPlayers()) + "/" + Integer.toString(server.getMaxPlayers()),
                                    ""
                            ));

                    menu.addItem(item);
                }
            }
        }, 0, 20);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getName().equalsIgnoreCase(this.menu.getName())) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) {
                return;
            }

            if (event.getCurrentItem().getType() == null) {
                return;
            }

            if (event.getCurrentItem().getItemMeta() == null || event.getCurrentItem().getItemMeta().getDisplayName() == null) {
                return;
            }

            serverManager.connect((Player) event.getWhoClicked(), event.getCurrentItem().getItemMeta().getDisplayName());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getItemInHand() != null && player.getItemInHand().getItemMeta() != null) {
            if (player.getItemInHand().getItemMeta().getDisplayName() != null) {
                if (player.getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(getItem().getItemMeta().getDisplayName())) {
                    event.setCancelled(true);
                    player.openInventory(this.menu);
                }
            }
        }
    }

    public static ItemStack getItem() {
        return ItemStackFactory.createItem(Material.CHEST, C.yellow + C.bold + "Player Servers");
    }

}
