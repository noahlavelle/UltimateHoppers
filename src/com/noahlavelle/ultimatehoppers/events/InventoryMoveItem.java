package com.noahlavelle.ultimatehoppers.events;

import com.noahlavelle.ultimatehoppers.Main;
import com.noahlavelle.ultimatehoppers.hoppers.Crate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

public class InventoryMoveItem implements Listener {

    private Main plugin;

    public InventoryMoveItem (Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryMoveItem(InventoryMoveItemEvent event) throws IOException {
        Crate crate = null;
        Player player = null;
        plugin.reloadCratesConfig();

        for (Crate c : plugin.crates) {
            if (c.chest.getInventory().equals(event.getDestination())) crate = c;
        }

        if (crate != null) {
            if (plugin.cratesConfig.get(crate.location.toString()) == null) {
                plugin.cratesConfig.createSection(crate.location.toString());
            }

            Boolean addAmount = false;
            ItemStack finalItemStack = null;
            String finalKey = null;

            for (String key : plugin.cratesConfig.getConfigurationSection(crate.location.toString()).getKeys(false)) {
                ItemStack itemStack = plugin.cratesConfig.getItemStack(crate.location + "." + key);

                if (event.getItem().isSimilar(itemStack)) {
                    addAmount = true;
                    finalItemStack = itemStack;
                    finalKey = key;
                }
            }

            int cratesSlotSize = plugin.cratesConfig.getConfigurationSection(crate.location.toString()).getKeys(false).size();
            boolean isFull = cratesSlotSize == crate.inventorySize - plugin.getConfig().getConfigurationSection("crate.slots").getKeys(false).size();
            if ((isFull && !event.getItem().isSimilar(finalItemStack)) || (isFull && plugin.cratesConfig.getItemStack(crate.location.toString() + "." + (cratesSlotSize - 1)).getAmount() == crate.storage)) {
                event.setCancelled(true);
                return;
            }

            if (addAmount && finalItemStack.getAmount() < crate.storage) {
                finalItemStack.setAmount(finalItemStack.getAmount() + 1);
                plugin.cratesConfig.set(crate.location + "." +  finalKey, finalItemStack);

            } else {
                plugin.cratesConfig.set(crate.location + "." + plugin.cratesConfig.getConfigurationSection(crate.location.toString()).getKeys(false).size(), event.getItem());
            }

            plugin.cratesConfig.save(plugin.cratesConfigFile);
            Crate finalCrate = crate;
            Bukkit.getScheduler().runTask(plugin, () -> {
                finalCrate.chest.getInventory().remove(event.getItem());
            });

        }
    }
}

