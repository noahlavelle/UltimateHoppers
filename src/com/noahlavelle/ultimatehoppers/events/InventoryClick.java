package com.noahlavelle.ultimatehoppers.events;

import com.noahlavelle.ultimatehoppers.Main;
import com.noahlavelle.ultimatehoppers.hoppers.Crate;
import com.noahlavelle.ultimatehoppers.hoppers.VacuumHopper;
import com.noahlavelle.ultimatehoppers.utils.GuiTools;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class InventoryClick implements Listener {

    private Main plugin;
    Economy economy;

    public InventoryClick (Main plugin) {
        this.plugin = plugin;
        this.economy = plugin.getEconomy();
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

        if (event.getInventory() != plugin.playerInventories.get(player.getUniqueId())) return;

        Iterator<VacuumHopper> vacuumHopperIterator = plugin.vacuumHoppers.iterator();
        Iterator<Crate> crateIterator = plugin.crates.iterator();
        Location playerSelectedLocation = plugin.playerBlockSelected.get(player.getUniqueId());
        VacuumHopper vacuumHopper = null;
        Crate crate = null;

        while (vacuumHopperIterator.hasNext() || crateIterator.hasNext()) {
            if (vacuumHopperIterator.hasNext()) {
                VacuumHopper vacuumHopperNext = vacuumHopperIterator.next();
                if (vacuumHopperNext.location.equals(playerSelectedLocation)) vacuumHopper = vacuumHopperNext;
            }

            if (crateIterator.hasNext()) {
                Crate crateNext = crateIterator.next();
                if (crateNext.location.equals(playerSelectedLocation)) crate = crateNext;
            }
        }

        if (plugin.playerInventories.containsKey(player.getUniqueId())) {
            if (event.isShiftClick()) {
                event.setCancelled(true);
                return;
            }
        }

        String itemClickedPath = "";
        String clickedType = "";
        ItemStack clickedItem = event.getInventory().getItem(event.getRawSlot());

        if (plugin.playerInventories.get(player.getUniqueId()) == event.getClickedInventory()) {
            switch (event.getView().getTitle().toLowerCase()) {
                case "vacuum hopper":
                    if (event.getInventory().getItem(event.getRawSlot()) == null) {
                        VacuumHopper finalVacuumHopper = vacuumHopper;
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
                                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1F);
                                finalVacuumHopper.filters.add(itemStack.getType().toString());

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

                    itemClickedPath = "vacuum.slots." + (event.getRawSlot() + 1);
                    clickedType = "vacuum";
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
                            inventory = inventory.replace("*" + item.getType(), "");

                            vacuumHopper.filters.remove(item.getType().toString());

                            try {
                                PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("UPDATE " + plugin.getServer().getName()
                                        + " SET INVENTORY=? WHERE (X=" + location.getBlockX() + " AND Y=" + location.getBlockY() + " AND Z=" + location.getBlockZ() + ")");
                                ps.setString(1, inventory);
                                ps.executeUpdate();
                                ps.executeUpdate();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }

                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);
                            return;
                        }

                break;
                case "crate":
                    if (plugin.getConfig().getString("crate.slots." + (event.getRawSlot() + 1) + ".interact") == null) {
                        if (event.getInventory().getItem(event.getRawSlot()) == null) {
                            event.setCancelled(true);
                            return;
                        }
                        if (event.getClick().isLeftClick()) {
                            plugin.reloadCratesConfig();

                            int key = event.getRawSlot() - (plugin.getConfig().getConfigurationSection("crate.slots").getKeys(false).size());
                            ItemStack itemStack = plugin.cratesConfig.getItemStack(crate.key + "." + key);

                            plugin.cratesConfig.set(crate.key + "." + key, null);

                            ItemStack itemOverflow = player.getInventory().addItem(itemStack).get(0);
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1F, 1F);

                            if (itemOverflow == null) {
                                event.getInventory().getItem(event.getRawSlot()).setAmount(0);

                            } else {
                                ItemMeta itemMeta = event.getInventory().getItem(event.getRawSlot()).getItemMeta();
                                List<String> lore = itemMeta.getLore();
                                lore.remove(lore.size() - 1);
                                lore.add(ChatColor.GRAY + "Amount: " + itemOverflow.getAmount());
                                itemMeta.setLore(lore);
                                event.getInventory().getItem(event.getRawSlot()).setItemMeta(itemMeta);

                                plugin.cratesConfig.set(crate.location + "." + plugin.cratesConfig.getConfigurationSection(crate.key).getKeys(false).size(), itemOverflow);
                            }

                            try {
                                plugin.cratesConfig.save(plugin.cratesConfigFile);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            itemStack.setAmount(0);
                        } else if (event.getClick().isRightClick()) {
                        }
                    }

                    itemClickedPath = "crate.slots." + (event.getRawSlot() + 1);
                    clickedType = "crate";
                break;
            }

            event.setCancelled(true);

            if (plugin.getConfig().getString(itemClickedPath + ".interact") == null) return;
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

                    Location location = plugin.playerBlockSelected.get(player.getUniqueId());

                    PreparedStatement ps = null;
                    switch (clickedType) {
                        case "vacuum":
                            switch (Objects.requireNonNull(plugin.getConfig().getString(itemClickedPath + ".toggle_property"))) {
                                case "enable":
                                    assert vacuumHopper != null;
                                    if (vacuumHopper.enabled) vacuumHopper.enabled = false;
                                    else vacuumHopper.enabled = true;

                                    try {
                                        ps = plugin.SQL.getConnection().prepareStatement("UPDATE " + plugin.getServer().getName() + " SET ENABLED=" + vacuumHopper.enabled
                                                + " WHERE (X=" + location.getBlockX() + " AND Y=" + location.getBlockY() + " AND Z=" + location.getBlockZ() + ")");
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case "filtering":
                                    assert vacuumHopper != null;
                                    if (vacuumHopper.filtering) vacuumHopper.filtering = false;
                                    else vacuumHopper.filtering = true;
                                    try {
                                        ps = plugin.SQL.getConnection().prepareStatement("UPDATE " + plugin.getServer().getName() + " SET FILTERING=" + vacuumHopper.filtering
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
                        if (cost > economy.getBalance(player)) {
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
                        economy.withdrawPlayer(player, cost);
                        clickedItem.setItemMeta(GuiTools.updateInfo(plugin, plugin.getConfig(), player, clickedType, String.valueOf(event.getRawSlot() + 1), economy, clickedItem.getItemMeta()));

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