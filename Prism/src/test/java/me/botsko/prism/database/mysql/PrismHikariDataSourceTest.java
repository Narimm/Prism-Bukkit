package me.botsko.prism.database.mysql;

import com.zaxxer.hikari.HikariConfig;
import me.botsko.prism.actionlibs.ActionRegistry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 17/01/2021.
 */
public class PrismHikariDataSourceTest {

    @Test
    public void testConstruction() {
        ConfigurationSection section = new MemoryConfiguration();
        HikariConfig dbConfig = new HikariConfig();
        dbConfig.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
        dbConfig.setJdbcUrl("jdbc:derby:target/test;create=true");
        PrismHikariDataSource dataSource = new PrismHikariDataSource(section,dbConfig){
            private String prefix = "prism_";
            public void setupDatabase(ActionRegistry actionRegistry) {
                try (
                        Connection conn = getConnection();
                        Statement st = conn.createStatement()
                ) {
                    String query = "CREATE TABLE " + prefix + "actions ("
                            + "action_id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,"
                            + "action varchar(25) NOT NULL,"
                            + "UNIQUE(action_id, action)"
                            + ")";
                    st.executeUpdate(query);

                    // data
                    query = "CREATE TABLE " + prefix + "data ("
                            + "id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,"
                            + "epoch int NOT NULL,"
                            + "action_id int NOT NULL,"
                            + "player_id int NOT NULL,"
                            + "world_id int NOT NULL,"
                            + "x int NOT NULL,"
                            + "y int NOT NULL,"
                            + "z int NOT NULL,"
                            + "block_id int DEFAULT NULL,"
                            + "block_subid int DEFAULT NULL,"
                            + "old_block_id int DEFAULT NULL,"
                            + "old_block_subid int DEFAULT NULL"
                            + ")";
                    st.executeUpdate(query);

                    // extra prism data table (check if it exists first, so we can avoid
                    // re-adding foreign key stuff)
                    final DatabaseMetaData metadata = conn.getMetaData();
                    ResultSet resultSet;
                    resultSet = metadata.getTables(null, null, "" + prefix + "data_extra", null);
                    if (!resultSet.next()) {

                        // extra data
                        query = "CREATE TABLE " + prefix + "data_extra ("
                                + "extra_id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY,"
                                + "data_id int NOT NULL,"
                                + "data varchar(2000),"
                                + "te_data varchar(2000)"
                                + ")";
                        st.executeUpdate(query);

                        // add extra data delete cascade
                        query = "ALTER TABLE " + prefix + "data_extra ADD CONSTRAINT " + prefix
                                + "data_extra_ibfk_1 FOREIGN KEY (data_id) REFERENCES " + prefix
                                + "data (id) ON DELETE CASCADE ON UPDATE NO ACTION";
                        st.executeUpdate(query);
                    }

                    // meta
                    query = "CREATE TABLE " + prefix + "meta ("
                            + "id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                            + "k varchar(25) NOT NULL,"
                            + "v varchar(255) NOT NULL,"
                            + "PRIMARY KEY (id)" + ")";
                    st.executeUpdate(query);

                    // players
                    query = "CREATE TABLE IF NOT EXISTS " + prefix + "players ("
                            + "player_id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
                            + "player varchar(255) NOT NULL,"
                            + "player_uuid binary(16) NOT NULL,"
                            + "PRIMARY KEY (player_id),"
                            + "UNIQUE KEY player_k (player),"
                            + "UNIQUE KEY player_uuid_k (player_uuid)"
                            + ")";
                    st.executeUpdate(query);

                    // worlds
                    query = "CREATE TABLE IF NOT EXISTS `" + prefix + "worlds` ("
                            + "`world_id` int(10) unsigned NOT NULL AUTO_INCREMENT," + "`world` varchar(255) NOT NULL,"
                            + "PRIMARY KEY (`world_id`)," + "UNIQUE KEY `world` (`world`)"
                            + ") ENGINE=InnoDB  DEFAULT CHARSET=utf8;";
                    st.executeUpdate(query);

                    // actions
                    cacheActionPrimaryKeys(); // Pre-cache, so we know if we need to
                    // populate db
                    final String[] actions = actionRegistry.listAll();
                    for (final String a : actions) {
                        addActionName(a);
                    }

                    // id map
                    query = "CREATE TABLE IF NOT EXISTS `" + prefix + "id_map` ("
                            + "`material` varchar(63) NOT NULL,"
                            + "`state` varchar(255) NOT NULL,"
                            + "`block_id` mediumint(5) NOT NULL AUTO_INCREMENT,"
                            + "`block_subid` mediumint(5) NOT NULL DEFAULT 0,"
                            + "PRIMARY KEY (`material`, `state`),"
                            + "UNIQUE KEY (`block_id`, `block_subid`)"
                            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
                    st.executeUpdate(query);
                } catch (final SQLException e) {
                    handleDataSourceException(e);
                    info("Database connection error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };

        dataSource.createDataSource();
        try {
            Assert.assertTrue(dataSource.getDataSource().getConnection().isValid(1000));
            ActionRegistry registry = new ActionRegistry();
            dataSource.setupDatabase(registry);
        }catch (SQLException e){
            Assert.fail();
        }
    }

}