/*
 * Copyright (c) 2026 MirraNET, Niklas Linz. All rights reserved.
 *
 * This file is part of the MirraNET project and is licensed under the
 * GNU Lesser General Public License v3.0 (LGPLv3).
 *
 * You may use, distribute and modify this code under the terms
 * of the LGPLv3 license. You should have received a copy of the
 * license along with this file. If not, see <https://www.gnu.org/licenses/lgpl-3.0.html>
 * or contact: niklas.linz@mirranet.de
 */

package de.linzn.pelltech.dblogger;

import de.linzn.pelltech.PelltechPlugin;
import de.linzn.pelltech.objects.Inlet;
import de.linzn.pelltech.objects.Notify;
import de.linzn.pelltech.objects.Outlet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Database access for heater configuration and history. All statements are prepared and closed,
 * connections are always released.
 */
public class DBLogger {

    private static final String[] SCHEMA = {
            """
            CREATE TABLE IF NOT EXISTS `addon_heater_inlets` (
              `inlet_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
              `index` INT NOT NULL,
              `name` VARCHAR(128) NOT NULL,
              UNIQUE KEY `uk_addon_heater_inlets_index` (`index`)
            ) DEFAULT CHARSET=utf8mb4""",
            """
            CREATE TABLE IF NOT EXISTS `addon_heater_outlets` (
              `outlet_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
              `index` INT NOT NULL,
              `name` VARCHAR(128) NOT NULL,
              UNIQUE KEY `uk_addon_heater_outlets_index` (`index`)
            ) DEFAULT CHARSET=utf8mb4""",
            """
            CREATE TABLE IF NOT EXISTS `addon_heater_notifies` (
              `notify_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
              `index` INT NOT NULL,
              `name` VARCHAR(128) NOT NULL,
              UNIQUE KEY `uk_addon_heater_notifies_index` (`index`)
            ) DEFAULT CHARSET=utf8mb4""",
            """
            CREATE TABLE IF NOT EXISTS `addon_heater_inlets_data` (
              `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
              `inlet_id` INT NOT NULL,
              `health` TINYINT NOT NULL,
              `value` DOUBLE NOT NULL,
              `timestamp` BIGINT NOT NULL,
              INDEX `idx_addon_heater_inlets_data` (`inlet_id`, `timestamp`)
            ) DEFAULT CHARSET=utf8mb4""",
            """
            CREATE TABLE IF NOT EXISTS `addon_heater_outlets_data` (
              `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
              `outlet_id` INT NOT NULL,
              `active` TINYINT NOT NULL,
              `timestamp` BIGINT NOT NULL,
              INDEX `idx_addon_heater_outlets_data` (`outlet_id`, `timestamp`)
            ) DEFAULT CHARSET=utf8mb4""",
            """
            CREATE TABLE IF NOT EXISTS `addon_heater_notifies_data` (
              `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
              `notify_id` INT NOT NULL,
              `active` TINYINT NOT NULL,
              `timestamp` BIGINT NOT NULL,
              INDEX `idx_addon_heater_notifies_data` (`notify_id`, `timestamp`)
            ) DEFAULT CHARSET=utf8mb4"""
    };

    /**
     * Create missing tables. Existing tables are not changed.
     */
    public void createTables() {
        try {
            PelltechPlugin.pelltechPlugin.getStemApi().data().withConnection(connection -> {
                try (Statement statement = connection.createStatement()) {
                    for (String sql : SCHEMA) {
                        statement.execute(sql);
                    }
                }
                return null;
            });
        } catch (SQLException e) {
            PelltechPlugin.pelltechPlugin.getPluginLogger().error("Could not create database tables", e);
        }
    }

    private interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    private <T> List<T> loadAll(String table, RowMapper<T> mapper) {
        List<T> result = new ArrayList<>();
        try {
            PelltechPlugin.pelltechPlugin.getStemApi().data().withConnection(connection -> {
                try (PreparedStatement st = connection.prepareStatement("SELECT * FROM `" + table + "`");
                     ResultSet rs = st.executeQuery()) {
                    while (rs.next()) {
                        result.add(mapper.map(rs));
                    }
                }
                return null;
            });
        } catch (SQLException e) {
            PelltechPlugin.pelltechPlugin.getPluginLogger().error("Could not load " + table, e);
        }
        return result;
    }

    public List<Inlet> loadInlets() {
        return loadAll("addon_heater_inlets", rs -> new Inlet(rs.getInt("index"), rs.getString("name")));
    }

    public List<Outlet> loadOutlets() {
        return loadAll("addon_heater_outlets", rs -> new Outlet(rs.getInt("index"), rs.getString("name")));
    }

    public List<Notify> loadNotifies() {
        return loadAll("addon_heater_notifies", rs -> new Notify(rs.getInt("index"), rs.getString("name")));
    }

    /**
     * Find the id of an entry by index; the entry is created if it does not exist.
     */
    private static int resolveId(Connection connection, String table, String idColumn, int index, String name) throws SQLException {
        String select = "SELECT `" + idColumn + "` FROM `" + table + "` WHERE `index` = ?";
        try (PreparedStatement st = connection.prepareStatement(select)) {
            st.setInt(1, index);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(idColumn);
                }
            }
        }
        try (PreparedStatement insert = connection.prepareStatement("INSERT INTO `" + table + "` (`index`, `name`) VALUES (?, ?)")) {
            insert.setInt(1, index);
            insert.setString(2, name);
            insert.executeUpdate();
        }
        try (PreparedStatement st = connection.prepareStatement(select)) {
            st.setInt(1, index);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next() ? rs.getInt(idColumn) : -1;
            }
        }
    }

    public void registerInlet(Inlet inlet) {
        this.register("addon_heater_inlets", "inlet_id", inlet.getIndex(), inlet.getName());
    }

    public void registerOutlet(Outlet outlet) {
        this.register("addon_heater_outlets", "outlet_id", outlet.getIndex(), outlet.getName());
    }

    public void registerNotify(Notify notify) {
        this.register("addon_heater_notifies", "notify_id", notify.getIndex(), notify.getName());
    }

    /**
     * Store an entry the CAN bus sent but the database does not know yet.
     */
    private void register(String table, String idColumn, int index, String name) {
        try {
            PelltechPlugin.pelltechPlugin.getStemApi().data().withConnection(connection -> resolveId(connection, table, idColumn, index, name));
            PelltechPlugin.pelltechPlugin.getPluginLogger().info("New entry in " + table + ": index " + index + " as '" + name + "'");
        } catch (SQLException e) {
            PelltechPlugin.pelltechPlugin.getPluginLogger().error("Could not register index " + index + " in " + table, e);
        }
    }

    public void saveInlets(List<Inlet> inlets) {
        try {
            PelltechPlugin.pelltechPlugin.getStemApi().data().withConnection(connection -> {
                for (Inlet inlet : inlets) {
                    int inletID = resolveId(connection, "addon_heater_inlets", "inlet_id", inlet.getIndex(), inlet.getName());
                    try (PreparedStatement st = connection.prepareStatement("INSERT INTO `addon_heater_inlets_data` (`inlet_id`, `health`, `value`, `timestamp`) VALUES (?, ?, ?, ?)")) {
                        st.setInt(1, inletID);
                        st.setInt(2, inlet.isHealth() ? 1 : 0);
                        st.setDouble(3, inlet.getValue());
                        st.setLong(4, inlet.getDate());
                        st.executeUpdate();
                    }
                }
                return null;
            });
        } catch (SQLException e) {
            PelltechPlugin.pelltechPlugin.getPluginLogger().error("Could not save inlets", e);
        }
    }

    public void saveOutlets(List<Outlet> outlets) {
        try {
            PelltechPlugin.pelltechPlugin.getStemApi().data().withConnection(connection -> {
                for (Outlet outlet : outlets) {
                    int outletID = resolveId(connection, "addon_heater_outlets", "outlet_id", outlet.getIndex(), outlet.getName());
                    try (PreparedStatement st = connection.prepareStatement("INSERT INTO `addon_heater_outlets_data` (`outlet_id`, `active`, `timestamp`) VALUES (?, ?, ?)")) {
                        st.setInt(1, outletID);
                        st.setInt(2, outlet.isActive() ? 1 : 0);
                        st.setLong(3, outlet.getDate());
                        st.executeUpdate();
                    }
                }
                return null;
            });
        } catch (SQLException e) {
            PelltechPlugin.pelltechPlugin.getPluginLogger().error("Could not save outlets", e);
        }
    }

    public void saveNotifies(List<Notify> notifies) {
        try {
            PelltechPlugin.pelltechPlugin.getStemApi().data().withConnection(connection -> {
                for (Notify notify : notifies) {
                    int notifyID = resolveId(connection, "addon_heater_notifies", "notify_id", notify.getIndex(), notify.getName());
                    try (PreparedStatement st = connection.prepareStatement("INSERT INTO `addon_heater_notifies_data` (`notify_id`, `active`, `timestamp`) VALUES (?, ?, ?)")) {
                        st.setInt(1, notifyID);
                        st.setInt(2, notify.isActive() ? 1 : 0);
                        st.setLong(3, notify.getDate());
                        st.executeUpdate();
                    }
                }
                return null;
            });
        } catch (SQLException e) {
            PelltechPlugin.pelltechPlugin.getPluginLogger().error("Could not save notifies", e);
        }
    }
}
