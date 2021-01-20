package me.botsko.prism.database.sql;

import me.botsko.prism.Prism;
import me.botsko.prism.database.AbstractSettingsQuery;
import me.botsko.prism.database.SettingsQuery;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 6/04/2019.
 */
public class SqlSettingsQuery extends AbstractSettingsQuery implements SettingsQuery {

    private final SqlPrismDataSource dataSource;
    private static String prefix = "prism_";
    private final Logger logger;

    /**
     * Create a settings Query with Prism initialized.
     * @param dataSource SqlPrismDataSource
     * @param log Logger.
     */
    public SqlSettingsQuery(SqlPrismDataSource dataSource, Logger log) {
        this.dataSource = dataSource;
        prefix = dataSource.getPrefix();
        logger = log;
    }

    /**
     * Create a settings Query with Prism initialized.
     * @param dataSource SqlPrismDataSource
     */
    public SqlSettingsQuery(SqlPrismDataSource dataSource) {
        this.dataSource = dataSource;
        prefix = dataSource.getPrefix();
        logger = Prism.getInstance().getLogger();
    }

    @Override
    public void deleteSetting(String key, Player player) {
        try (
                Connection conn =  dataSource.getConnection();
                PreparedStatement s = conn.prepareStatement("DELETE FROM " + prefix + "meta WHERE k = ?")
        ) {
            String finalKey = key;
            if (player != null) {
                finalKey = getPlayerKey(player, key);
            }
            s.setString(1, finalKey);
            s.executeUpdate();

        } catch (final SQLException e) {
            log("[Prism] -Debug- Database Error:" + e.getMessage());
        }
    }

    @Override
    public void saveSetting(String key, String value, Player player) {

        String finalKey = key;
        if (player != null) {
            finalKey = getPlayerKey(player, key);
        }
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement s = conn.prepareStatement("DELETE FROM " + prefix + "meta WHERE k = ?");
                PreparedStatement s2 = conn.prepareStatement("INSERT INTO " + prefix + "meta (k,v) VALUES (?,?)")
                ) {
            s.setString(1, finalKey);
            s.executeUpdate();
            s2.setString(1, finalKey);
            s2.setString(2, value);
            s2.executeUpdate();
        } catch (final SQLException e) {
            log("Database Error:" + e.getMessage());
        }
    }

    @Override
    public String getSetting(String key, Player player) {
        String value = null;
        ResultSet rs;


        String finalKey = key;
        if (player != null) {
            finalKey = getPlayerKey(player, key);
        }
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement s = conn.prepareStatement("SELECT v FROM " + prefix + "meta WHERE k = ? LIMIT 0,1")
        ) {
            s.setString(1, finalKey);
            rs = s.executeQuery();

            while (rs.next()) {
                value = rs.getString("v");
            }

        } catch (final SQLException e) {
            log("Database Error:" + e.getMessage());
        }
        return value;
    }

    private void log(String message) {
        logger.info("[Prism] --Debug-- " + message);
    }
}
