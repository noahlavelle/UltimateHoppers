package com.noahlavelle.ultimatehoppers.events;

import com.noahlavelle.ultimatehoppers.Main;
import com.noahlavelle.utils.CreateGui;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerInteract implements Listener {

    private Main plugin;

    public PlayerInteract (Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlaced(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        Block block = event.getClickedBlock();

        if (block != null) {
            Location location = block.getLocation();

            if (player.isSneaking() && player.getInventory().getItemInMainHand().getType() == Material.AIR && block.getType() == Material.HOPPER) {
                event.setCancelled(true);
                if (plugin.data.hopperLocations.contains(location)) {
                    try {
                        PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT * FROM " + plugin.getServer().getName()
                                + " WHERE (X=" + location.getBlockX() + " AND Y=" + location.getBlockY() + " AND Z=" + location.getBlockZ() + ")");

                        ResultSet resultSet = ps.executeQuery();
                        while (resultSet.next()) {
                            switch (resultSet.getString(5)) {
                                case "vacuum":
                                    plugin.playerBlockSelected.put(player.getUniqueId(), block.getLocation());
                                    Inventory inventory = CreateGui.createGui(plugin, "vacuum", player);
                                    player.openInventory(inventory);
                                    plugin.playerInventories.put(player.getUniqueId(), inventory);
                                break;
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
