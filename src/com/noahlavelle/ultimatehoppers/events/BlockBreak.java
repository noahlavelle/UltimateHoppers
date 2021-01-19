package com.noahlavelle.ultimatehoppers.events;

import com.noahlavelle.ultimatehoppers.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreak implements Listener {

    private Main plugin;

    public BlockBreak (Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (plugin.data.hopperLocations.contains(event.getBlock().getLocation())) {
            plugin.data.removeBlock(event.getBlock().getLocation(),  player, "vacuum");
            plugin.data.hopperLocations.remove(event.getBlock().getLocation());
        }
    }
}
