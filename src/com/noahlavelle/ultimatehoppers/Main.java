package com.noahlavelle.ultimatehoppers;

import com.noahlavelle.ultimatehoppers.events.*;
import com.noahlavelle.ultimatehoppers.hoppers.Crate;
import com.noahlavelle.ultimatehoppers.hoppers.VacuumHopper;
import com.noahlavelle.ultimatehoppers.items.ItemManager;
import com.noahlavelle.ultimatehoppers.commands.CreateItem;
import com.noahlavelle.ultimatehoppers.sql.MySQL;
import com.noahlavelle.ultimatehoppers.sql.SQLGetter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
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
    public FileConfiguration cratesConfig = new YamlConfiguration();
    public File cratesConfigFile;

    public ArrayList<Location> hopperLocations = new ArrayList<>();
    public ArrayList<VacuumHopper> vacuumHoppers = new ArrayList<>();
    public ArrayList<Crate> crates = new ArrayList<>();

    private static final Logger log = Logger.getLogger("Minecraft");
    private static Economy econ = null;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("crates.yml", false);

        cratesConfigFile = new File(getDataFolder(), "crates.yml");
        cratesConfig = YamlConfiguration.loadConfiguration(cratesConfigFile);
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
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "[UltimateHoppers] Database is not connected");
        }

        if (SQL.isConnected()) {
            getServer().getConsoleSender().sendMessage("[UltimateHoppers] Database is connected");
            data.createTable();
            data.createAllBlocks();
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
        pluginManager.registerEvents(new InventoryMoveItem(this), this);

        // Creating Commands
        new CreateItem(this);

        getServer().getConsoleSender().sendMessage("[UltimateHoppers] Plugin is enabled");
    }

    @Override
    public void onDisable() {
        SQL.disconnect();
        getServer().getConsoleSender().sendMessage( "[UltimateHoppers] Plugin is disabled");
    }

    public void reloadCratesConfig() {
        try {
            cratesConfig.load(cratesConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
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
