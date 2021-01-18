package com.noahlavelle.ultimatehoppers;

import com.noahlavelle.events.BlockPlace;
import com.noahlavelle.items.ItemManager;
import com.noahlavelle.ultimatehoppers.commands.CreateItem;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        PluginManager pluginManager = getServer().getPluginManager();

        // Creating ItemManager
        ItemManager itemManager = new ItemManager();
        itemManager.init();

        // Creating Events
        pluginManager.registerEvents(new BlockPlace(this), this);

        // Creating Commands
        new CreateItem(this);

        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[UltimateHoppers] Plugin is enabled");
    }

    public void onDisable() {
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[UltimateHoppers] Plugin is disabled");
    }
}
