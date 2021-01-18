package com.noahlavelle.items;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemManager {

    public static ItemStack vacuumHopper;

    public static void init() {
        createVacuumHopper();
    }

    private static void createVacuumHopper() {
        ItemStack item = new ItemStack(Material.HOPPER, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§3Vacuum Hopper");
        List<String> lore = new ArrayList<>();
        lore.add("§7Sucks up all items near it");
        meta.setLore(lore);
        meta.addEnchant(Enchantment.ARROW_INFINITE, 1, false);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        vacuumHopper = item;
    }
}
