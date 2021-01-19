package com.noahlavelle.utils;

import com.noahlavelle.ultimatehoppers.Main;
import com.noahlavelle.ultimatehoppers.hoppers.VacuumHopper;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CreateGui {

    public static Inventory createGui (Main plugin, String path, Player player) {
        Economy economy = plugin.getEconomy();
        Configuration config = plugin.getConfig();
        Inventory inventory = Bukkit.createInventory(null, Integer.parseInt(config.getString(path + ".size")), config.getString(path + ".title"));

        for (String key : config.getConfigurationSection(path + ".slots").getKeys(false)) {
            ItemStack item = new ItemStack(Material.valueOf(config.getString(path + ".slots" + "." + key + ".item")), 1);
            ItemMeta meta = item.getItemMeta();

            meta.setDisplayName(ChatColor.RESET + config.getString(path + ".slots." + key + ".name"));


            if (config.getString(path + ".slots." + key + ".type").equals("info")) {
                meta = updateInfo(plugin, config, player, path, key, economy, meta);
            }

            item.setItemMeta(meta);
            inventory.setItem(Integer.parseInt(key) - 1, item);
        }

        return inventory;
    }

    public static ItemMeta updateInfo(Main plugin, Configuration config, Player player, String path, String key, Economy economy, ItemMeta meta) {
        Location location = plugin.playerBlockSelected.get(player.getUniqueId());
        int P1 = 0;
        int P2 = 0;

        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT * FROM " + plugin.getServer().getName() + " WHERE (X=" + location.getBlockX() + " AND Y=" + location.getBlockY() + " AND Z=" + location.getBlockZ() + ")");
            ResultSet resultSet = ps.executeQuery();
            resultSet.next();

            P1 = Integer.parseInt(resultSet.getString(7));
            P2 = Integer.parseInt(resultSet.getString(8));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<String> lore = new ArrayList<>();
        String infoPath;
        for (String infoKey : config.getConfigurationSection(path + ".slots." + key + ".info").getKeys(false)) {
            infoPath = path + ".slots." + key + ".info." + infoKey;
            ChatColor color;
            String text;
            ConfigurationSection configurationSection = config.getConfigurationSection(infoPath);
            assert configurationSection != null;
            color = ChatColor.valueOf(configurationSection.get("1").toString().split(" ")[0]);
            text = configurationSection.get("1").toString().split(" ")[1];

            if (infoKey.equals(config.getString(path + ".slots." + key + ".p1"))) {
                color = ChatColor.valueOf(configurationSection.get(String.valueOf(P1)).toString().split(" ")[0]);
                text = configurationSection.get(String.valueOf(P1)).toString().split(" ")[1];
            } else if (infoKey.equals(config.getString(path + ".slots." + key + ".p2"))) {
                color = ChatColor.valueOf(configurationSection.get(String.valueOf(P2)).toString().split(" ")[0]);
                text = configurationSection.get(String.valueOf(P2)).toString().split(" ")[1];
            } else if (infoKey.equals("Cost")) {
                color = ChatColor.valueOf(configurationSection.get(String.valueOf(P1 + P2 - 1)).toString().split(" ")[0]);
                text = configurationSection.get(String.valueOf(P1 + P2 - 1)).toString().split(" ")[1];
            }

            for (VacuumHopper hopper : plugin.vacuumHoppers) {
                if (hopper.location.equals(plugin.playerBlockSelected.get(player.getUniqueId()))) {
                    hopper.delay = Integer.parseInt(Objects.requireNonNull(config.getString(path + ".slots." + key + ".delay." + P1)));
                    hopper.radius = Integer.parseInt(Objects.requireNonNull(config.getString(path + ".slots." + key + ".info.Radius." + P2)).split(" ")[1]);
                    hopper.createHopper();
                }
            }

            lore.add(ChatColor.GRAY + infoKey + ": " + color + text);
        }

        int cost = Integer.parseInt(config.getConfigurationSection(path + ".slots." + key + ".info.Cost").get(String.valueOf((P1 + P2) - 1)).toString().split(" ")[1].substring(1));
        lore.add("");

        if (economy.getBalance(player) >= cost) {
            lore.add(ChatColor.YELLOW + "Click to upgrade");
        } else {
            lore.add(ChatColor.RED + "You cannot afford this upgrade");
        }

        if (P1 + P2 - 1 == config.getConfigurationSection(path + ".slots." + key + ".info.Cost").getKeys(false).size()) {
            lore.remove(lore.size() - 1);
            lore.remove(lore.size() - 1);
            lore.remove(lore.size() - 1);
            lore.add("");
            lore.add(ChatColor.GREEN + "Hopper max level!");
        }


        meta.setLore(lore);
        return meta;
    }
}
