package com.noahlavelle.ultimatehoppers.sql;

import com.noahlavelle.ultimatehoppers.Main;
import com.noahlavelle.ultimatehoppers.hoppers.VacuumHopper;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class SQLGetter {

    private Main plugin;
    public ArrayList<Location> hopperLocations = new ArrayList<>();

    public SQLGetter (Main plugin) {
        this.plugin = plugin;
    }

    public void createTable() {
        PreparedStatement ps;
        try {
            ps = plugin.SQL.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS " + plugin.getServer().getName() + " (X INT, Y INT, Z INT, WORLD VARCHAR(100), TYPE VARCHAR(100), UUID VARCHAR(100))");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createBlock (Location location, Player player, String type) {
        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("INSERT IGNORE INTO " + plugin.getServer().getName() + " (X, Y, Z, WORLD, TYPE, UUID) VALUES (?, ?, ?, ?, ?, ?)");
            ps.setString(1, String.valueOf(location.getBlockX()));
            ps.setString(2, String.valueOf(location.getBlockY()));
            ps.setString(3, String.valueOf(location.getBlockZ()));
            ps.setString(4, location.getWorld().getName());

            ps.setString(5, type);
            ps.setString(6, player.getUniqueId().toString());
            ps.executeUpdate();

            return;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeBlock (Location location, Player player, String type) {
        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("DELETE FROM " + plugin.getServer().getName()
                    + " WHERE (X=" + location.getBlockX() + " AND Y=" + location.getBlockY() + " AND Z=" + location.getBlockZ() + ")");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createAllBlocks() {
        try {
            PreparedStatement ps = plugin.SQL.getConnection().prepareStatement("SELECT * FROM " + plugin.getServer().getName());
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                Location location = new Location(plugin.getServer().getWorld(resultSet.getString(4)),
                        Double.parseDouble(resultSet.getString(1)),Double.parseDouble(resultSet.getString(2)), Double.parseDouble(resultSet.getString(3)));
                new VacuumHopper(plugin, location);
                hopperLocations.add(location);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
