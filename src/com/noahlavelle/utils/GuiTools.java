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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

public class GuiTools {

    public static Inventory createGui (Main plugin, String path, Player player) {
        Economy economy = plugin.getEconomy();
        Configuration config = plugin.getConfig();
        Inventory inventory = Bukkit.createInventory(null, Integer.parseInt(config.getString(path + ".size")), config.getString(path + ".title"));

        for (String key : config.getConfigurationSection(path + ".slots").getKeys(false)) {
            ItemStack item = new ItemStack(Material.valueOf(config.getString(path + ".slots" + "." + key + ".item")), 1);
            ItemMeta meta = item.getItemMeta();

            ChatColor color = ChatColor.WHITE;
            if (!Objects.requireNonNull(config.getString(path + ".slots." + key + ".type")).equals("filler")) {
                color = ChatColor.valueOf(config.getString(path + ".slots." + key + ".color"));
            }
            meta.setDisplayName(color + config.getString(path + ".slots." + key + ".name"));

            switch (Objects.requireNonNull(config.getString(path + ".slots." + key + ".type"))) {
                case "info":
                    meta = updateInfo(plugin, config, player, path, key, economy, meta);
                    break;
                case "toggle":

                    VacuumHopper vh = null;

                    for (VacuumHopper hopper : plugin.vacuumHoppers) {
                        if (hopper.location.equals(plugin.playerBlockSelected.get(player.getUniqueId()))) {
                            vh = hopper;
                        }
                    }

                    switch  (Objects.requireNonNull(config.getString(path + ".slots." + key + ".toggle_property"))) {
                        case "enable":
                            if (vh.enabled) {
                                item.setType(Material.valueOf(config.getString(path + ".slots." + key + ".item")));
                                meta.setDisplayName(ChatColor.valueOf(config.getString(path + ".slots." + key + ".color")) + config.getString(path + ".slots." + key + ".name"));
                            } else {
                                item.setType(Material.valueOf(config.getString(path + ".slots." + key + ".toggle.item")));
                                meta.setDisplayName(ChatColor.valueOf(config.getString(path + ".slots." + key + ".toggle.color")) + config.getString(path + ".slots." + key + ".toggle.name"));
                            }
                            break;
                        case "filtering":
                            if (vh.filtering) {
                                item.setType(Material.valueOf(config.getString(path + ".slots." + key + ".item")));
                                meta.setDisplayName(ChatColor.valueOf(config.getString(path + ".slots." + key + ".color")) + config.getString(path + ".slots." + key + ".name"));
                            } else {
                                item.setType(Material.valueOf(config.getString(path + ".slots." + key + ".toggle.item")));
                                meta.setDisplayName(ChatColor.valueOf(config.getString(path + ".slots." + key + ".toggle.color")) + config.getString(path + ".slots." + key + ".toggle.name"));
                            }
                            break;
                    }
                    break;
            }

            item.setItemMeta(meta);
            inventory.setItem(Integer.parseInt(key) - 1, item);
        }

        try {
            Location location = plugin.playerBlockSelected.get(player.getUniqueId());
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT * FROM " + plugin.getServer().getName()
                    + " WHERE (X=" + location.getBlockX() + " AND Y=" + location.getBlockY() + " AND Z=" + location.getBlockZ() + ")");
            ResultSet resultSet = ps.executeQuery();

            resultSet.next();

            if (resultSet.getString(9) == null) return inventory;

            for (String itemStackString : resultSet.getString(9).split(Pattern.quote("*"))) {
                try {
                    inventory.addItem(new ItemStack(Material.valueOf(itemStackString)));
                } catch (Exception e) {}
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
                    hopper.delay = Integer.parseInt(Objects.requireNonNull(config.getString(path + ".delay." + P1)));
                    hopper.radius = Integer.parseInt(Objects.requireNonNull(config.getString(path + ".radius." + P2)));
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

        if (P1 + P2 - 1 == config.getConfigurationSection(path + ".slots." + key + ".info.Cost").getKeys(false).size() ) {
            lore.remove(lore.size() - 1);
            lore.remove(lore.size() - 1);
            lore.remove(lore.size() - 1);
            lore.add("");
            lore.add(ChatColor.GREEN + "Hopper max level");
        }


        meta.setLore(lore);
        return meta;
    }
}
