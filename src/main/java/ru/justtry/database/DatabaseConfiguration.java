package ru.justtry.database;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import ru.justtry.database.Database;

import javax.inject.Inject;
import javax.inject.Named;

@Configuration
@PropertySource("classpath:application.properties")
public class DatabaseConfiguration
{
    @Value("${server.port:8765}")
    private Integer port;

    @Value("${server.host:localhost}")
    private String host;

    @Value("${database.name}")
    private String name;

    @Value("${database.user}")
    private String user;

    @Value("${database.password}")
    private String password;



    @Bean
//    @Scope("singleton")
//    @Named("database")
    public Database getDatabase()
    {
        Database db = new Database();
        db.init(host, port, name, user, password);
        return db;
    }
}
