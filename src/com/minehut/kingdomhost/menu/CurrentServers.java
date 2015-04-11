package com.minehut.kingdomhost.menu;

import com.minehut.commons.common.chat.C;
import com.minehut.commons.common.items.ItemStackFactory;
import com.minehut.kingdomhost.manager.ServerManager;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

/**
 * Created by luke on 4/11/15.
 */
public class CurrentServers {
    private ServerManager serverManager;
    private Inventory menu;

    public CurrentServers(ServerManager serverManager) {
        this.serverManager = serverManager;
    }

    private void updateMenu() {
        this.menu.clear();

//        for (ServerInfo serverInfo : this.serverManager.getServers()) {
//            if (serverInfo()) {
//                ItemStack item = ItemStackFactory.createItem(Material.PAPER, serverInfo.getName(),
//                        Arrays.asList(
//                                "",
//                                C.gray + "Map: " + C.yellow + serverInfo.getMap(),
//                                C.gray + "Players: " + C.yellow + Integer.toString(serverInfo.getPlayers()) + "/" + Integer.toString(serverInfo.getMaxPlayers()),
//                                ""
//                        ));
//
//                this.menu.addItem(item);
//            }
//        }

        this.menu.setItem(16, ItemStackFactory.createItem(Material.GOLD_HELMET, C.yellow + "Kingdoms"));
        this.menu.setItem(17, ItemStackFactory.createItem(Material.FIREBALL, C.green + "Back to Hub"));
    }
}
