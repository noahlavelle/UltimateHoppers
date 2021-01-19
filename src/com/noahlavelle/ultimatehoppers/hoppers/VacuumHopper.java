package com.noahlavelle.ultimatehoppers.hoppers;

import com.noahlavelle.ultimatehoppers.Main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class VacuumHopper {

    private Location location;
    private Main plugin;
    private Hopper hopper;

    public VacuumHopper(Main plugin, Location location) {
        this.plugin = plugin;
        this.location = location;

        if (location.getWorld().getBlockAt(location).getType() == Material.HOPPER) {
            this.hopper = (Hopper) location.getWorld().getBlockAt(location).getState();
        }

        createHopper();

    }

    public void createHopper() {
        BukkitTask loop = new BukkitRunnable() {
            @Override
            public void run() {
                if (location.getWorld().getBlockAt(location).getType() != Material.HOPPER) return;

                for (Entity entity : location.getWorld().getNearbyEntities(location, 10, 10, 10)) {
                    if (entity instanceof Item) {
                        ItemStack item = ((Item) entity).getItemStack();

                        for (ItemStack itemStack : hopper.getInventory()) {
                            if (itemStack == null || (itemStack.getAmount() < itemStack.getMaxStackSize() && itemStack.getMaxStackSize() - itemStack.getAmount() >= item.getAmount() && itemStack.getType() == item.getType())) {
                                hopper.getInventory().addItem(item);
                                item.setAmount(0);
                            } else if (itemStack.getAmount() < itemStack.getMaxStackSize() && itemStack.getMaxStackSize() - itemStack.getAmount() < item.getAmount() && itemStack.getType() == item.getType()) {
                                hopper.getInventory().addItem(item);
                                int amount = item.getAmount() - (itemStack.getMaxStackSize() - itemStack.getAmount());
                                item.setAmount(amount);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }
}