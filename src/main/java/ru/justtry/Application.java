package ru.justtry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import ru.justtry.configuration.AppConfiguration;


@Configuration("application.properties")
@EnableAutoConfiguration
@PropertySource("classpath:application.properties")
@ComponentScan("ru.justtry")
@EnableScheduling
public class Application
{

    public static void main(String[] args)
    {
        SpringApplication.run(new Class[] { Application.class, AppConfiguration.class }, args);
    }

}
