package com.noahlavelle.ultimatehoppers;

import com.noahlavelle.ultimatehoppers.events.BlockBreak;
import com.noahlavelle.ultimatehoppers.events.BlockPlace;
import com.noahlavelle.ultimatehoppers.items.ItemManager;
import com.noahlavelle.ultimatehoppers.commands.CreateItem;
import com.noahlavelle.ultimatehoppers.sql.MySQL;
import com.noahlavelle.ultimatehoppers.sql.SQLGetter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class Main extends JavaPlugin {

    public MySQL SQL;
    public SQLGetter data;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.SQL = new MySQL(this);
        this.data = new SQLGetter(this);

        try {
            SQL.connect();
        } catch (ClassNotFoundException | SQLException e) {
            Bukkit.getLogger().info("[UltimateHoppers] Database is not connected");
        }

        if (SQL.isConnected()) {
            Bukkit.getLogger().info("[UltimateHoppers] Database is connected");
        }

        PluginManager pluginManager = getServer().getPluginManager();

        // Creating ItemManager
        ItemManager itemManager = new ItemManager();
        itemManager.init();

        // Creating Events
        pluginManager.registerEvents(new BlockPlace(this), this);
        pluginManager.registerEvents(new BlockBreak(this), this);

        // Creating Commands
        new CreateItem(this);

        // Creating Table and blocks
        data.createTable();
        data.createAllBlocks();

        getServer().getConsoleSender().sendMessage("[UltimateHoppers] Plugin is enabled");
    }

    public void onDisable() {
        SQL.disconnect();
        getServer().getConsoleSender().sendMessage( "[UltimateHoppers] Plugin is disabled");
    }
}
