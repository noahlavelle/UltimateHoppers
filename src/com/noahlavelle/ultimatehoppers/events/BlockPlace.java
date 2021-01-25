package com.noahlavelle.ultimatehoppers.events;

import com.noahlavelle.ultimatehoppers.hoppers.Crate;
import com.noahlavelle.ultimatehoppers.items.ItemManager;
import com.noahlavelle.ultimatehoppers.Main;
import com.noahlavelle.ultimatehoppers.hoppers.VacuumHopper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;

public class BlockPlace implements Listener {

    private Main plugin;
    private Random random = new Random(System.currentTimeMillis());

    public BlockPlace (Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlaced(BlockPlaceEvent event) throws IOException {
        Player player = event.getPlayer();
        ItemMeta vacuumMeta = ItemManager.vacuumHopper.getItemMeta();
        ItemMeta crateMeta = ItemManager.crate.getItemMeta();
        ItemMeta mobMeta = ItemManager.mobHopper.getItemMeta();

        plugin.hopperLocations.add(event.getBlock().getLocation());

        if (Objects.equals(player.getInventory().getItemInMainHand().getItemMeta(), vacuumMeta) || Objects.equals(player.getInventory().getItemInOffHand().getItemMeta(), vacuumMeta)) {
            VacuumHopper vh = new VacuumHopper(plugin, event.getBlock().getLocation(), "vacuum");
            vh.createHopper();
            plugin.vacuumHoppers.add(vh);
            plugin.data.createBlock(event.getBlock().getLocation(), player, "vacuum", "");
            plugin.data.hopperLocations.add(event.getBlock().getLocation());
        }

        if (Objects.equals(player.getInventory().getItemInMainHand().getItemMeta(), mobMeta) || Objects.equals(player.getInventory().getItemInOffHand().getItemMeta(), mobMeta)) {
            VacuumHopper vh = new VacuumHopper(plugin, event.getBlock().getLocation(), "mob");
            vh.createHopper();
            plugin.vacuumHoppers.add(vh);
            plugin.data.createBlock(event.getBlock().getLocation(), player, "mob", "");
            plugin.data.hopperLocations.add(event.getBlock().getLocation());
        }

        Block block = event.getBlock();

        if (Objects.equals(player.getInventory().getItemInMainHand().getItemMeta(), crateMeta) && block.getType() == Material.CHEST) {
            for (BlockFace blockFace : BlockFace.values()) {
                Block adjacentBlock = block.getRelative(blockFace);
                if (blockFace != BlockFace.SELF && adjacentBlock.getType() == Material.CHEST) {
                    player.sendMessage(ChatColor.RED + "You cannot place two crates together");
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1F, 1F);
                    event.setCancelled(true);
                }
            }
        }

        if (Objects.equals(player.getInventory().getItemInMainHand().getItemMeta(), crateMeta) || Objects.equals(player.getInventory().getItemInOffHand().getItemMeta(), crateMeta)) {
            Crate crate = new Crate(plugin, event.getBlock().getLocation());
            String key = generateRandomKey();
            crate.key = key;
            plugin.crates.add(crate);
            plugin.data.createBlock(event.getBlock().getLocation(), player, "crate", key);
            plugin.data.hopperLocations.add(event.getBlock().getLocation());

            plugin.reloadCratesConfig();
            plugin.cratesConfig.createSection(key);
            plugin.cratesConfig.save(plugin.cratesConfigFile);
        }
    }

    public String generateRandomKey() {
        String key = String.valueOf(random.nextInt(99999));

        try {
            if (plugin.cratesConfig.getConfigurationSection("crates").getKeys(false).contains(key)) {
                generateRandomKey();
            } else {
                return key;
            }
        } catch (Exception e) {
            return key;
        }

        return null;
    }
}

