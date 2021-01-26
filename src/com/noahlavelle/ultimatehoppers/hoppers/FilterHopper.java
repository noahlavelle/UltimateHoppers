package com.noahlavelle.ultimatehoppers.hoppers;

import com.noahlavelle.ultimatehoppers.Main;
import org.bukkit.Location;
import org.bukkit.block.Hopper;

import java.util.ArrayList;

public class FilterHopper {
    public ArrayList<String> filters = new ArrayList<>();
    public Location location;
    public Hopper hopper;

    private Main plugin;

    public FilterHopper (Main plugin, Location location) {
        this.plugin = plugin;
        this.location = location;

        this.hopper = (Hopper) location.getWorld().getBlockAt(location).getState();
    }

}
