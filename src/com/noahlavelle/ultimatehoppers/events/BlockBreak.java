package com.noahlavelle.ultimatehoppers.events;

import com.noahlavelle.ultimatehoppers.Main;
import com.noahlavelle.ultimatehoppers.hoppers.Crate;
import com.noahlavelle.ultimatehoppers.hoppers.VacuumHopper;
import com.noahlavelle.ultimatehoppers.items.ItemManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

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
    public void onEntityExplode (EntityExplodeEvent event) {
        for (Location location : plugin.hopperLocations) {
            Block block = location.getWorld().getBlockAt(location);
            event.blockList().remove(block);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Crate crate = null;
        VacuumHopper vacuumHopper = null;

        for (Crate c : plugin.crates) {
            if (c.location.equals(event.getBlock().getLocation())) crate = c;
        }

        for (VacuumHopper v : plugin.vacuumHoppers) {
            if (v.location.equals(event.getBlock().getLocation())) vacuumHopper = v;
        }

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
                    case "mob":
                        location.getWorld().dropItemNaturally(location, ItemManager.mobHopper);
                        for (LivingEntity entity : vacuumHopper.noAiMobs) {
                            vacuumHopper.noAiMobs.remove(entity);
                            entity.setAI(true);

                        }
                    break;
                    case "crate":
                        if (plugin.cratesConfig.getConfigurationSection(crate.key).getKeys(false).size() != 0) {
                            event.setCancelled(true);
                            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1F, 1F);
                            player.sendMessage(ChatColor.RED + "You cannot break a crate with items in");
                            return;
                        }

                        location.getWorld().dropItemNaturally(location, ItemManager.crate);
                        plugin.cratesConfig.set(crate.key, null);
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
