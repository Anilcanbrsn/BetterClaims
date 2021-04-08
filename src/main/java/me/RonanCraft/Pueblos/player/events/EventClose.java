package me.RonanCraft.Pueblos.player.events;

import me.RonanCraft.Pueblos.Pueblos;
import me.RonanCraft.Pueblos.inventory.PueblosInventory;
import me.RonanCraft.Pueblos.player.PlayerInfo;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class EventClose {

    void exit(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        Pueblos pl = Pueblos.getInstance();
        PlayerInfo pInfo = pl.getSystems().getPlayerInfo();
        if (pInfo.getInventory(p) != null && e.getInventory().equals(pInfo.getInventory(p))) {
            if (pInfo.getInventoryPlugin(p) != null) {
                pInfo.getInventoryPlugin(p).closeEvent(p); //Close Inventory event
                pInfo.clearInventory(p);
            }
        } else if (pInfo.getInventory(p) != null) { //Inventory was lost to something else
            pInfo.clearInventory(p);
        }
    }

}
