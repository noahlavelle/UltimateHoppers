package com.noahlavelle.ultimatehoppers.events;

import com.noahlavelle.ultimatehoppers.Main;
import com.noahlavelle.ultimatehoppers.hoppers.Crate;
import com.noahlavelle.ultimatehoppers.hoppers.VacuumHopper;
import com.noahlavelle.ultimatehoppers.utils.GuiTools;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

public class PlayerInteract implements Listener {

    private Main plugin;

    public PlayerInteract (Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract (PlayerInteractEvent event) {

        Player player = event.getPlayer();

        Block block = event.getClickedBlock();

        if (block != null) {
            Location location = block.getLocation();

            if (player.isSneaking() && player.getInventory().getItemInMainHand().getType() == Material.AIR &&
                    block.getType() == Material.HOPPER && plugin.data.hopperLocations.contains(location)) {
                event.setCancelled(true);
                try {
                    PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT * FROM " + plugin.getServer().getName()
                            + " WHERE (X=" + location.getBlockX() + " AND Y=" + location.getBlockY() + " AND Z=" + location.getBlockZ() + ")");

                    ResultSet resultSet = ps.executeQuery();
                    while (resultSet.next()) {
                        Inventory inventory;
                        if ("vacuum".equals(resultSet.getString(5))) {
                            plugin.playerBlockSelected.put(player.getUniqueId(), block.getLocation());
                            inventory = GuiTools.createGui(plugin, "vacuum", player);
                            player.openInventory(inventory);
                            plugin.playerInventories.put(player.getUniqueId(), inventory);
                        } else if ("mob".equals(resultSet.getString(5))) {
                            plugin.playerBlockSelected.put(player.getUniqueId(), block.getLocation());
                            inventory = GuiTools.createGui(plugin, "mob", player);
                            player.openInventory(inventory);
                            plugin.playerInventories.put(player.getUniqueId(), inventory);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            if ( (player.isSneaking() && player.getInventory().getItemInMainHand().getType() == Material.AIR || !player.isSneaking())
                    && block.getType() == Material.CHEST && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                    && plugin.data.hopperLocations.contains(location)) {
                event.setCancelled(true);

                try {
                    PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT * FROM " + plugin.getServer().getName()
                            + " WHERE (X=" + location.getBlockX() + " AND Y=" + location.getBlockY() + " AND Z=" + location.getBlockZ() + ")");

                    ResultSet resultSet = ps.executeQuery();
                    while (resultSet.next()) {
                        if ("crate".equals(resultSet.getString(5))) {
                            plugin.playerBlockSelected.put(player.getUniqueId(), block.getLocation());
                            Inventory inventory = GuiTools.createGui(plugin, "crate", player);
                            player.updateInventory();
                            player.openInventory(inventory);
                            plugin.playerInventories.put(player.getUniqueId(), inventory);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
