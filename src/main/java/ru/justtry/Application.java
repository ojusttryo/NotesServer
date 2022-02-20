package ru.justtry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import ru.justtry.configuration.AppConfiguration;


@SpringBootApplication
@EnableScheduling
public class Application
{

    public static void main(String[] args)
    {
        SpringApplication.run(new Class[] { Application.class, AppConfiguration.class }, args);
    }

}
