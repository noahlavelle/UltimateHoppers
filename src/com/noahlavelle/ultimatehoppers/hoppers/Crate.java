package com.noahlavelle.ultimatehoppers.hoppers;

import com.noahlavelle.ultimatehoppers.Main;
import org.bukkit.Location;

public class Crate {

    public Location location;
    public Boolean enabled = true;

    private Main plugin;

    public Crate (Main plugin, Location location) {
        this.plugin = plugin;
        this.location = location;

        createCrate();
    }

    public void createCrate() {
    }
}
