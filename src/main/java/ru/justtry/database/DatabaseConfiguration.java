package ru.justtry.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
public class DatabaseConfiguration
{
    @Autowired
    private ApplicationContext context;

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
//    @Scope("singleton")
//    @Named("database")
    public Database getDatabase()
    {
        Database db = new Database();
        db.init(host, port, name, user, password);
        return db;
    }
}
