package com.noahlavelle.ultimatehoppers.sql;

import com.noahlavelle.ultimatehoppers.Main;
import org.bukkit.configuration.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQL {
    private Main plugin;
    private Configuration config;

    private String host;
    private String port;
    private String database;
    private String username;
    private String password;

    public MySQL (Main plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();

        host = plugin.getConfig().getString("mysql.host");
        port = plugin.getConfig().getString("mysql.port");
        database = plugin.getConfig().getString("mysql.database");
        username = plugin.getConfig().getString("mysql.username");
        password = plugin.getConfig().getString("mysql.password");
    }

    private Connection connection;

    public boolean isConnected() {
        return (connection == null ? false : true);
    }

    public void connect() throws ClassNotFoundException, SQLException {
        if (!(isConnected())) {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false", username, password);
        }

    }

    public void disconnect () {
        if (isConnected()) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }

}
