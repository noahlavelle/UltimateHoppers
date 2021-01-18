package com.noahlavelle.ultimatehoppers.hoppers;

import com.noahlavelle.ultimatehoppers.Main;

public class VacuumHopper {

    private Main plugin;

    public VacuumHopper (Main plugin) {
        this.plugin = plugin;
    }

    public void createHopper() {
        System.out.println(plugin);
    }
}
