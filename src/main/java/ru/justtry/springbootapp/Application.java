package ru.justtry.springbootapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import ru.justtry.configuration.WebAppInitializer;

@Configuration("application.properties")
@EnableAutoConfiguration
@PropertySource("classpath:application.properties")
@ComponentScan(basePackages = "ru.justtry")
public class Application
{
    public static void main(String[] args)
    {
        SpringApplication.run(new Class[] { Application.class, WebAppInitializer.class }, args);
    }
}
