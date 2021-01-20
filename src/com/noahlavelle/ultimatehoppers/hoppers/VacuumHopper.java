package com.noahlavelle.ultimatehoppers.hoppers;

import com.noahlavelle.ultimatehoppers.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;

public class VacuumHopper implements Listener {

    @EventHandler
    public void onInventoryPickup(InventoryPickupItemEvent event) {
        if (event.getItem().getLocation().distance(location) <= radius) {
            event.setCancelled(true);
        }
    }

    public Location location;
    private BukkitTask task = null;
    private Main plugin;
    private Hopper hopper;

    public int delay = 8;
    public int radius = 10;
    public ArrayList<String> filters = new ArrayList<>();
    public boolean filtering = false;
    public boolean enabled = true;

    public VacuumHopper(Main plugin, Location location) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.location = location;

        if (location.getWorld().getBlockAt(location).getType() == Material.HOPPER) {
            this.hopper = (Hopper) location.getWorld().getBlockAt(location).getState();
        }

        createHopper();

    }

    public void createHopper() {
        if (task != null) {
            task.cancel();
        }
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (location.getWorld().getBlockAt(location).getType() != Material.HOPPER) return;

            if (enabled) {
                for (Entity entity : location.getWorld().getNearbyEntities(location, radius, radius, radius)) {
                    if (entity instanceof Item) {
                        ItemStack item = ((Item) entity).getItemStack();
                        ItemStack itemAdd = new ItemStack(item.getType(), 1);
                        itemAdd.setItemMeta(item.getItemMeta());

                        if (!filtering || filters.contains(item.getType().toString())) {
                            for (ItemStack itemStack : hopper.getInventory()) {
                                if (itemStack == null || (itemStack.getAmount() < itemStack.getMaxStackSize() && itemStack.getMaxStackSize() - itemStack.getAmount() >= item.getAmount() && itemStack.getType() == item.getType())) {
                                    item.setAmount(item.getAmount() - 1);
                                    hopper.getInventory().addItem(itemAdd);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }, 0, delay);
    }
}