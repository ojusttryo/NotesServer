package ru.justtry.database;

import static ru.justtry.shared.Constants.APPLICATION_CONTEXT;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.migmong.MongoMigration;
import com.github.migmong.exception.MigrationException;

import lombok.RequiredArgsConstructor;
import ru.justtry.database.sort.Sort;
import ru.justtry.mappers.LogMapper;

@Configuration
@RequiredArgsConstructor
public class DatabaseConfiguration
{
    public static final String MONGO_MIGRATION_BEAN = "mongoMigration";

    private final ApplicationContext context;
    private final LogMapper logMapper;
    private final Sort sort;

    @Value("${database.port:27017}")
    private Integer port;
    @Value("${database.host:localhost}")
    private String host;
    @Value("${database.name}")
    private String name;
    @Value("${database.user}")
    private String user;
    @Value("${database.password}")
    private String password;


    @Bean(name = "database")
    public Database getDatabase()
    {
        Database db = new Database(logMapper, sort);
        db.init(host, port, name, user, password);
        return db;
    }

    @Bean(name = MONGO_MIGRATION_BEAN)
    public MongoMigration mongoMigration() throws MigrationException
    {
        MongoMigration migration = new MongoMigration(host, port, name, user, password);
        migration.setMigrationScanPackage("ru.justtry.database.migrations");
        migration.setMigrationVariable(APPLICATION_CONTEXT, context);
        migration.setEnabled(true);
        migration.setMigrationNamePrefix("V");
        migration.setMigrationCollectionName("migrationLog");
        migration.execute();
        return migration;
    }

}
