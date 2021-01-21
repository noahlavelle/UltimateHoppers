package com.noahlavelle.ultimatehoppers.events;

import com.noahlavelle.ultimatehoppers.hoppers.Crate;
import com.noahlavelle.ultimatehoppers.items.ItemManager;
import com.noahlavelle.ultimatehoppers.Main;
import com.noahlavelle.ultimatehoppers.hoppers.VacuumHopper;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

public class BlockPlace implements Listener {

    private Main plugin;

    public BlockPlace (Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlaced(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemMeta vacuumMeta = ItemManager.vacuumHopper.getItemMeta();
        ItemMeta crateMeta = ItemManager.crate.getItemMeta();

        if (Objects.equals(player.getInventory().getItemInMainHand().getItemMeta(), vacuumMeta) || Objects.equals(player.getInventory().getItemInOffHand().getItemMeta(), vacuumMeta)) {
            VacuumHopper vh = new VacuumHopper(plugin, event.getBlock().getLocation());
            plugin.vacuumHoppers.add(vh);
            plugin.data.createBlock(event.getBlock().getLocation(), player, "vacuum");
            plugin.data.hopperLocations.add(event.getBlock().getLocation());
        }

        if (Objects.equals(player.getInventory().getItemInMainHand().getItemMeta(), crateMeta) || Objects.equals(player.getInventory().getItemInOffHand().getItemMeta(), crateMeta)) {
            Crate crate = new Crate(plugin, event.getBlock().getLocation());
            plugin.data.createBlock(event.getBlock().getLocation(), player, "crate");
            plugin.data.hopperLocations.add(event.getBlock().getLocation());
        }
    }
}

