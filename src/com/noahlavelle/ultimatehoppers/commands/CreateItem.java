package com.noahlavelle.ultimatehoppers.commands;

import com.noahlavelle.items.ItemManager;
import com.noahlavelle.ultimatehoppers.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

public class CreateItem implements CommandExecutor {

    private Configuration config;
    private Main plugin;

    public CreateItem (Main plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();

        plugin.getCommand("create").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        // Player Checks
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(config.getString("messages.not_player"));
            return false;
        }

        Player player = (Player) commandSender;

        // Args checks
        if (strings.length == 0) {
            player.sendMessage(config.getString("nai"));
            return false;
        }

        switch (strings[0]) {
            case "vacuum":
                player.getInventory().addItem(ItemManager.vacuumHopper);
            break;
            default:
                player.sendMessage(config.getString("nai"));
            break;
        }

        return false;
    }
}
