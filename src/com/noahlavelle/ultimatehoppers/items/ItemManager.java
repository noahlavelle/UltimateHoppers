package com.noahlavelle.ultimatehoppers.items;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemManager {

    public static ItemStack vacuumHopper;
    public static ItemStack crate;
    public static ItemStack mobHopper;
    public static ItemStack filterHopper;

    public static void init() {
        createVacuumHopper();
        createCrate();
        createMobHopper();
        createFilterHopper();
    }

    private static void createVacuumHopper() {
        ItemStack item = new ItemStack(Material.HOPPER, 1);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName("§3Vacuum Hopper");
        List<String> lore = new ArrayList<>();
        lore.add("§7Sucks up all items near it");
        meta.setLore(lore);
        meta.addEnchant(Enchantment.ARROW_INFINITE, 1, false);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        vacuumHopper = item;
    }

    private static void createCrate() {
        ItemStack item = new ItemStack(Material.CHEST, 1);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName("§3Crate");
        List<String> lore = new ArrayList<>();
        lore.add("§7Sucks up all items near it and stores them in bulk");
        lore.add("§7Then you can just click to withdraw them");
        meta.setLore(lore);
        meta.addEnchant(Enchantment.ARROW_INFINITE, 1, false);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        crate = item;
    }

    private static void createMobHopper() {
        ItemStack item = new ItemStack(Material.HOPPER, 1);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName("§3Mob Hopper");
        List<String> lore = new ArrayList<>();
        lore.add("§7Pulls all mobs near on top of it");
        meta.setLore(lore);
        meta.addEnchant(Enchantment.ARROW_INFINITE, 1, false);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        mobHopper = item;
    }

    private static void createFilterHopper() {
        ItemStack item = new ItemStack(Material.HOPPER, 1);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName("§3Filter Hopper");
        List<String> lore = new ArrayList<>();
        lore.add("§7Items set in the filter move down, others just pass on");
        meta.setLore(lore);
        meta.addEnchant(Enchantment.ARROW_INFINITE, 1, false);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        filterHopper = item;
    }
}
