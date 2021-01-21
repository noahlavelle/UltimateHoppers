package com.noahlavelle.ultimatehoppers.events;

import com.noahlavelle.ultimatehoppers.Main;
import com.noahlavelle.ultimatehoppers.hoppers.VacuumHopper;
import com.noahlavelle.utils.GuiTools;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

public class InventoryClick implements Listener {

    private Main plugin;

    public InventoryClick (Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (plugin.playerInventories.get(event.getWhoClicked().getUniqueId()) == event.getInventory()) {
            event.setCancelled(true);
        }
    }

    @EventHandler public void onInventoryClose(InventoryCloseEvent event) {
        if (plugin.playerInventories.containsKey(event.getPlayer().getUniqueId())) {
            UUID uuid = event.getPlayer().getUniqueId();
            plugin.playerInventories.remove(uuid);
            plugin.playerBlockSelected.remove(uuid);
        }
    }

    @EventHandler
    public void onInventoryClick (InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (plugin.playerInventories.containsKey(player.getUniqueId())) {
            if (event.isShiftClick()) {
                event.setCancelled(true);
                return;
            }
        }

        if (plugin.playerInventories.get(player.getUniqueId()) == event.getClickedInventory()) {
            if (event.getInventory().getItem(event.getRawSlot()) == null) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    ItemStack itemStack = event.getClickedInventory().getItem(event.getRawSlot());

                    event.getClickedInventory().setItem(event.getRawSlot(), new ItemStack(itemStack.getType()));
                    player.getInventory().addItem(itemStack);

                    if (itemStack != null) {
                        ResultSet resultSet;
                        Location location = plugin.playerBlockSelected.get(player.getUniqueId());
                        String inventory = null;
                        try {
                            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT * FROM " + plugin.getServer().getName()
                                    + " WHERE (X=" + location.getBlockX() + " AND Y=" + location.getBlockY() + " AND Z=" + location.getBlockZ() + ")");
                            resultSet = ps.executeQuery();
                            resultSet.next();
                            inventory = resultSet.getString(9);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        if (inventory == null) inventory = "";
                        String newInventory = inventory + "*" + itemStack.getType();
                        for (VacuumHopper vacuumHopper : plugin.vacuumHoppers) {
                            if (location.equals(vacuumHopper.location)) {
                                vacuumHopper.filters.add(itemStack.getType().toString());
                            }
                        }

                        try {
                            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("UPDATE " + plugin.getServer().getName()
                                    + " SET INVENTORY=? WHERE (X=" + location.getBlockX() + " AND Y=" + location.getBlockY() + " AND Z=" + location.getBlockZ() + ")");
                            ps.setString(1, newInventory);
                            ps.executeUpdate();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });


                return;
            }
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

            if (plugin.getConfig().getString(itemClickedPath + ".interact") == null) {
                ItemStack item = event.getClickedInventory().getItem(event.getRawSlot());
                for (ItemStack itemStack : event.getClickedInventory().getContents()) {
                    if (itemStack != null && itemStack.equals(item)) {
                        event.getClickedInventory().remove(itemStack);
                    }
                }

                ResultSet resultSet = null;
                Location location = plugin.playerBlockSelected.get(player.getUniqueId());
                String inventory = null;

                try {
                    PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT * FROM " + plugin.getServer().getName()
                            + " WHERE (X=" + location.getBlockX() + " AND Y=" + location.getBlockY() + " AND Z=" + location.getBlockZ() + ")");
                    resultSet = ps.executeQuery();
                    resultSet.next();
                    inventory = resultSet.getString(9);

                } catch (SQLException e) {
                    e.printStackTrace();
                }
                    System.out.println("*" + item.getType());
                    inventory = inventory.replace("*" + item.getType(), "");

                for (VacuumHopper vacuumHopper : plugin.vacuumHoppers) {
                    if (location.equals(vacuumHopper.location)) {
                        vacuumHopper.filters.remove(item.getType().toString());
                    }
                }

                    try {
                        PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("UPDATE " + plugin.getServer().getName()
                                + " SET INVENTORY=? WHERE (X=" + location.getBlockX() + " AND Y=" + location.getBlockY() + " AND Z=" + location.getBlockZ() + ")");
                        ps.setString(1, inventory);
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
                return;
            }

            switch (Objects.requireNonNull(plugin.getConfig().getString(itemClickedPath + ".interact"))) {
                case "toggle":
                    ItemMeta newMeta;

                    if (clickedItem.getType() == Material.valueOf(plugin.getConfig().getString(itemClickedPath + ".toggle.item"))) {
                        clickedItem.setType(Material.valueOf(plugin.getConfig().getString(itemClickedPath + ".item")));
                        newMeta = clickedItem.getItemMeta();
                        newMeta.setDisplayName(ChatColor.valueOf(plugin.getConfig().getString(itemClickedPath + ".color")) + plugin.getConfig().getString(itemClickedPath + ".name"));
                    } else {
                        clickedItem.setType(Material.valueOf(plugin.getConfig().getString(itemClickedPath + ".toggle.item")));
                        newMeta = clickedItem.getItemMeta();
                        newMeta.setDisplayName(ChatColor.valueOf(plugin.getConfig().getString(itemClickedPath + ".toggle.color")) + plugin.getConfig().getString(itemClickedPath + ".toggle.name"));
                    }

                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F);
                    clickedItem.setItemMeta(newMeta);

                    VacuumHopper vh = null;
                    Location location = plugin.playerBlockSelected.get(player.getUniqueId());

                    for (VacuumHopper vacuumHopper : plugin.vacuumHoppers) {
                        if (location.equals(vacuumHopper.location)) {
                            vh = vacuumHopper;
                        }
                    }
                    PreparedStatement ps = null;
                    switch (Objects.requireNonNull(plugin.getConfig().getString(itemClickedPath + ".toggle_property"))) {
                        case "enable":
                            assert vh != null;
                            if (vh.enabled) vh.enabled = false;
                            else vh.enabled = true;

                            try {
                                ps = plugin.SQL.getConnection().prepareStatement("UPDATE " + plugin.getServer().getName() + " SET ENABLED=" + vh.enabled
                                        + " WHERE (X=" + location.getBlockX() + " AND Y=" + location.getBlockY() + " AND Z=" + location.getBlockZ() + ")");
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            break;
                        case "filtering":
                            assert vh != null;
                            if (vh.filtering) vh.filtering = false;
                            else vh.filtering = true;
                            try {
                                ps = plugin.SQL.getConnection().prepareStatement("UPDATE " + plugin.getServer().getName() + " SET FILTERING=" + vh.filtering
                                        + " WHERE (X=" + location.getBlockX() + " AND Y=" + location.getBlockY() + " AND Z=" + location.getBlockZ() + ")");
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        break;
                    }

                    try {
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                break;
                case "upgrade":
                    location = plugin.playerBlockSelected.get(player.getUniqueId());
                    try {
                        ps = plugin.SQL.getConnection().prepareStatement("SELECT * FROM " + plugin.getServer().getName()
                                + " WHERE (X=" + location.getBlockX() + " AND Y=" + location.getBlockY() + " AND Z=" + location.getBlockZ() + ")");
                        ResultSet resultSet = ps.executeQuery();
                        resultSet.next();

                        int P1 = Integer.parseInt(resultSet.getString(7));
                        int P2 = Integer.parseInt(resultSet.getString(8));

                        int cost = Integer.parseInt(plugin.getConfig().getConfigurationSection(clickedType + ".slots." +
                                (event.getRawSlot() + 1) + ".info.Cost").get(String.valueOf(P1 + P2 - 1)).toString().split(" ")[1].substring(1));
                        if (cost > plugin.getEconomy().getBalance(player)) {
                            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1F, 1F);
                            return;
                        }


                        if (P1 + P2 - 1 < plugin.getConfig().getConfigurationSection(clickedType + ".slots." + (event.getRawSlot() + 1) + ".info.Cost").getKeys(false).size()) {
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
                        clickedItem.setItemMeta(GuiTools.updateInfo(plugin, plugin.getConfig(), player, clickedType, String.valueOf(event.getRawSlot() + 1), plugin.getEconomy(), clickedItem.getItemMeta()));

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
