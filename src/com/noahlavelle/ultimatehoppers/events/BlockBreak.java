package com.noahlavelle.ultimatehoppers.events;

import com.noahlavelle.ultimatehoppers.Main;
import com.noahlavelle.ultimatehoppers.hoppers.Crate;
import com.noahlavelle.ultimatehoppers.items.ItemManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BlockBreak implements Listener {

    private Main plugin;

    public BlockBreak (Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (plugin.data.hopperLocations.contains(event.getBlock().getLocation())) {
            try {
                Location location = event.getBlock().getLocation();
                PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT * FROM " + plugin.getServer().getName()
                        + " WHERE (X=" + location.getBlockX() + " AND Y=" + location.getBlockY() + " AND Z=" + location.getBlockZ() + ")");
                ResultSet resultSet = ps.executeQuery();
                resultSet.next();

                switch (resultSet.getString(5)) {
                    case "vacuum":
                        location.getWorld().dropItemNaturally(location, ItemManager.vacuumHopper);
                    break;
                    case "crate":
                        if (plugin.cratesConfig.getString(location.toString() + ".0") != null) {
                            event.setCancelled(true);
                            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1F, 1F);
                            player.sendMessage(ChatColor.RED + "You cannot break a crate with items in");
                            return;
                        }

                        location.getWorld().dropItemNaturally(location, ItemManager.crate);
                        plugin.cratesConfig.set(location.toString(), null);
                        try {
                            plugin.cratesConfig.save(plugin.cratesConfigFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        break;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            plugin.vacuumHoppers.removeIf(hopper -> hopper.location == event.getBlock().getLocation());

            plugin.data.removeBlock(event.getBlock().getLocation(),  player, "vacuum");
            plugin.data.hopperLocations.remove(event.getBlock().getLocation());

            event.getBlock().setType(Material.AIR);
        }
    }
}
