package me.botsko.prism.database.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import me.botsko.prism.Prism;
import me.botsko.prism.database.PrismDataSource;
import me.botsko.prism.database.sql.SqlPrismDataSource;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.logging.Logger;


/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 1/01/2021.
 */
public class PrismHikariDataSource extends SqlPrismDataSource {

    private final HikariConfig dbConfig;

    public PrismHikariDataSource(ConfigurationSection section,HikariConfig dbConfig) {
        super(section,Logger.getAnonymousLogger());
        this.dbConfig = dbConfig;
    }

    /**
     * Create a dataSource.
     *
     * @param section Config
     */
    public PrismHikariDataSource(ConfigurationSection section) {
        super(section,Prism.getInstance().getLogger());
        name = "hikari";
        File propFile = new File(Prism.getInstance().getDataFolder(),"hikari.properties");
        if (propFile.exists()) {
            Prism.log("Configuring Hikari from " + propFile.getName());
            dbConfig = new HikariConfig(propFile.getPath());
        } else {
            Prism.log("You may need to adjust these settings for your setup.");
            Prism.log("To set a table prefix you will need to create a config entry under");
            Prism.log("prism:");
            Prism.log("  datasource:");
            Prism.log("    prefix: your-prefix");
            String jdbcUrl = "jdbc:mysql://localhost:3306/prism?useUnicode=true&characterEncoding=UTF-8&useSSL=false";
            Prism.log("Default jdbcUrl: " + jdbcUrl);
            Prism.log("Default Username: username");
            Prism.log("Default Password: password");
            Prism.log("You will need to provide the required jar libraries that support your database.");
            dbConfig = new HikariConfig();
            dbConfig.setJdbcUrl(jdbcUrl);
            dbConfig.setUsername("username");
            dbConfig.setPassword("password");
            HikariHelper.createPropertiesFile(propFile, dbConfig, false);
        }
    }

    @Override
    public PrismDataSource createDataSource() {
        try {
            database = new HikariDataSource(dbConfig);
            getSettingsQuery();
            return this;
        } catch (HikariPool.PoolInitializationException e) {
            info("Hikari Pool did not Initialize: " + e.getMessage());
            database = null;
        }
        return this;

    }

    @Override
    public void setFile() {

    }
}
