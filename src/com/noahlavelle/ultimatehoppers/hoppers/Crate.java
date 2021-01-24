package com.noahlavelle.ultimatehoppers.hoppers;

import com.noahlavelle.ultimatehoppers.Main;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.scheduler.BukkitTask;

public class Crate {

    public Location location;
    public boolean enabled = true;
    public Chest chest;
    public int storage = 100;
    public int inventorySize = 27;

    private Main plugin;
    private BukkitTask task = null;

    public Crate (Main plugin, Location location) {
        this.plugin = plugin;
        this.location = location;

        chest = (Chest) location.getWorld().getBlockAt(location).getState();
    }

    public void createCrate() {
    }
}
