package com.noahlavelle.events;

import com.noahlavelle.items.ItemManager;
import com.noahlavelle.ultimatehoppers.Main;
import com.noahlavelle.ultimatehoppers.hoppers.VacuumHopper;
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

        if (Objects.equals(player.getInventory().getItemInMainHand().getItemMeta(), vacuumMeta) || Objects.equals(player.getInventory().getItemInOffHand().getItemMeta(), vacuumMeta)) {
            new VacuumHopper(plugin, event.getBlock().getLocation());
        }
    }
}
