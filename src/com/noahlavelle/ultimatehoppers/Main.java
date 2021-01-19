package com.noahlavelle.ultimatehoppers;

import com.noahlavelle.ultimatehoppers.events.BlockBreak;
import com.noahlavelle.ultimatehoppers.events.BlockPlace;
import com.noahlavelle.ultimatehoppers.events.InventoryClick;
import com.noahlavelle.ultimatehoppers.events.PlayerInteract;
import com.noahlavelle.ultimatehoppers.hoppers.VacuumHopper;
import com.noahlavelle.ultimatehoppers.items.ItemManager;
import com.noahlavelle.ultimatehoppers.commands.CreateItem;
import com.noahlavelle.ultimatehoppers.sql.MySQL;
import com.noahlavelle.ultimatehoppers.sql.SQLGetter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class Main extends JavaPlugin {

    public MySQL SQL;
    public SQLGetter data;
    public Map<UUID, Inventory> playerInventories = new HashMap<>();
    public Map<UUID, Location> playerBlockSelected = new HashMap<>();

    public ArrayList<VacuumHopper> vacuumHoppers = new ArrayList<>();

    private static final Logger log = Logger.getLogger("Minecraft");
    private static Economy econ = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.SQL = new MySQL(this);
        this.data = new SQLGetter(this);

        if (!setupEconomy() ) {
            log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

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
        pluginManager.registerEvents(new PlayerInteract(this), this);
        pluginManager.registerEvents(new InventoryClick(this), this);

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

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public Economy getEconomy() {
        return econ;
    }
}
