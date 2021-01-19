package com.noahlavelle.ultimatehoppers.events;

import com.noahlavelle.ultimatehoppers.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteract implements Listener {

    private Main plugin;

    public PlayerInteract (Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlaced(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Location location = block.getLocation();

        if (player.isSneaking() && block.getType() == Material.HOPPER) {

        }
    }
}
