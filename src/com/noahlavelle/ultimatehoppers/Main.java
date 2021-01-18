package com.noahlavelle.ultimatehoppers;

import com.noahlavelle.ultimatehoppers.commands.CreateItem;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        new CreateItem(this);

        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "[UltimateHoppers] Plugin is enabled");
    }

    public void onDisable() {
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "[UltimateHoppers] Plugin is disabled");
    }
}
