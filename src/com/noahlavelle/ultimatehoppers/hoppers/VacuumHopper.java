package com.noahlavelle.ultimatehoppers.hoppers;

import com.noahlavelle.ultimatehoppers.Main;
import org.bukkit.*;
import org.bukkit.block.Hopper;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VacuumHopper implements Listener {

    @EventHandler
    public void onInventoryPickup(InventoryPickupItemEvent event) {
        if (enabled && event.getItem().getLocation().distance(location) <= radius) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getItem().getItemStack().equals(item)) event.setCancelled(true);
    }

    public Location location;
    private BukkitTask task = null;
    private Main plugin;
    private Hopper hopper;
    private ItemStack item;
    private Chunk chunk;
    private String type;

    public int delay = 8;
    public int radius = 10;
    public ArrayList<String> filters = new ArrayList<>();
    public boolean filtering = false;
    public boolean enabled = true;
    public ArrayList<LivingEntity> noAiMobs = new ArrayList<>();

    public VacuumHopper(Main plugin, Location location, String type) {
        this.type = type;
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.location = location;
        this.chunk = location.getChunk();

        if (location.getWorld().getBlockAt(location).getType() != Material.HOPPER) return;
        this.hopper = (Hopper) location.getWorld().getBlockAt(location).getState();

    }

    public void createHopper() {
        if (task != null) {
            task.cancel();
        }

        switch (type) {
            case "vacuum":
                task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    if (location.getWorld().getBlockAt(location).getType() != Material.HOPPER) return;

                    if (enabled && chunk.isLoaded()) {
                        location.getWorld().spawnParticle(Particle.PORTAL, new Location(location.getWorld(), location.getX() + 0.5, location.getY() + 1, location.getZ() + 0.5), 10, 0.1, 0.1, 0.1);
                        for (Entity entity : location.getWorld().getNearbyEntities(location, radius, radius, radius)) {
                            if (entity instanceof Item) {
                                item = ((Item) entity).getItemStack();
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
            break;
            case "mob":
                task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    if (location.getWorld().getBlockAt(location).getType() != Material.HOPPER) return;


                    if (enabled && chunk.isLoaded()) {
                        location.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, new Location(location.getWorld(), location.getX() + 0.5, location.getY() + 1, location.getZ() + 0.5), 10, 1, 1, 1);

                        Collection<Entity> nearbyEntities = location.getWorld().getNearbyEntities(location, radius, radius, radius);

                     for (Entity entity : noAiMobs) {
                         if (!nearbyEntities.contains(entity)) noAiMobs.remove(entity);
                     }

                        for (Entity entity : nearbyEntities) {
                            if (!(entity instanceof Player) && !(entity instanceof Item) && !(entity instanceof Arrow) && !(entity instanceof ExperienceOrb)) {
                                entity.teleport(location.clone().add(0.5, 0.9, 0.5));
                                if (!noAiMobs.contains(entity)) {
                                    noAiMobs.add((LivingEntity) entity);
                                }

                                ((LivingEntity) entity).setAI(false);
                            }
                        }
                    }

                }, 0, delay * 10L);
            break;
        }
    }
}