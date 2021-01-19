package com.noahlavelle.ultimatehoppers.events;

import com.noahlavelle.ultimatehoppers.Main;
import com.noahlavelle.utils.CreateGui;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InventoryClick implements Listener {

    private Main plugin;

    public InventoryClick (Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick (InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (plugin.playerInventories.containsKey(player.getUniqueId()) && plugin.playerInventories.get(player.getUniqueId()) == event.getInventory()) {
            event.setCancelled(true);

            String itemClickedPath = "";
            String clickedType = "";
            ItemStack clickedItem = event.getInventory().getItem(event.getRawSlot());

            switch (event.getView().getTitle().toLowerCase()) {
                case "vacuum hopper":
                    itemClickedPath = "vacuum.slots." + (event.getRawSlot() + 1);
                    clickedType = "vacuum";
                break;
            }

            switch (plugin.getConfig().getString(itemClickedPath + ".interact")) {
                case "toggle":
                    ItemMeta newMeta;

                    if (clickedItem.getType() == Material.valueOf(plugin.getConfig().getString(itemClickedPath + ".toggle.item"))) {
                        clickedItem.setType(Material.valueOf(plugin.getConfig().getString(itemClickedPath + ".item")));
                        newMeta = clickedItem.getItemMeta();
                        newMeta.setDisplayName(ChatColor.RESET + plugin.getConfig().getString(itemClickedPath + ".name"));
                    } else {
                        clickedItem.setType(Material.valueOf(plugin.getConfig().getString(itemClickedPath + ".toggle.item")));
                        newMeta = clickedItem.getItemMeta();
                        newMeta.setDisplayName(ChatColor.RESET + plugin.getConfig().getString(itemClickedPath + ".toggle.name"));
                    }

                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F);
                    clickedItem.setItemMeta(newMeta);
                break;
                case "upgrade":
                    Location location = plugin.playerBlockSelected.get(player.getUniqueId());
                    try {
                        PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT * FROM " + plugin.getServer().getName() + " WHERE (X=" + location.getBlockX() + " AND Y=" + location.getBlockY() + " AND Z=" + location.getBlockZ() + ")");
                        ResultSet resultSet = ps.executeQuery();
                        resultSet.next();

                        int P1 = Integer.parseInt(resultSet.getString(7));
                        int P2 = Integer.parseInt(resultSet.getString(8));

                        int cost = Integer.parseInt(plugin.getConfig().getConfigurationSection(clickedType + ".slots." + (event.getRawSlot() + 1) + ".info.Cost").get(String.valueOf(P1 + P2 - 1)).toString().split(" ")[1].substring(1));
                        if (cost > plugin.getEconomy().getBalance(player)) {
                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 1F);
                            return;
                        }


                        if (P1 + P2 - 1 < plugin.getConfig().getConfigurationSection(clickedType + ".slots." + (event.getRawSlot() + 1) + ".info.Cost").getKeys(false).size() - 1) {
                            if (P1 > P2) {
                                P2++;
                                ps = plugin.SQL.getConnection().prepareStatement("UPDATE " + plugin.getServer().getName() + " SET P2=" + P2 + " WHERE (X=" + location.getBlockX() + " AND Y=" + location.getBlockY() + " AND Z=" + location.getBlockZ() + ")");
                                ps.executeUpdate();
                            } else if (P1 == P2) {
                                P1++;
                                ps = plugin.SQL.getConnection().prepareStatement("UPDATE " + plugin.getServer().getName() + " SET P1=" + P1 + " WHERE (X=" + location.getBlockX() + " AND Y=" + location.getBlockY() + " AND Z=" + location.getBlockZ() + ")");
                                ps.executeUpdate();
                            }
                        } else {
                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 1F);
                            return;
                        }

                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1F, 1F);
                        plugin.getEconomy().withdrawPlayer(player, cost);
                        clickedItem.setItemMeta(CreateGui.updateInfo(plugin, plugin.getConfig(), player, clickedType, String.valueOf(event.getRawSlot() + 1), plugin.getEconomy(), clickedItem.getItemMeta()));

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                break;
            }
        }
    }
}
