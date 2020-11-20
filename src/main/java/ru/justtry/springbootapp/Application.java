package ru.justtry.springbootapp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration("application.properties")
@EnableAutoConfiguration
@PropertySource("classpath:application.properties")
@ComponentScan(basePackages = "ru.justtry")
public class Application
{
    // To see logger debug info at start use Run configurations - VM option -Dlog4j.debug
    private static Logger logger = LogManager.getLogger(Application.class);


    public static void main(String[] args)
    {
        //PropertyConfigurator.configure("log4j.properties");
        //showBeans(applicationContext);
        SpringApplication.run(Application.class, args);
    }


    public static void showBeans(ApplicationContext applicationContext)
    {
        int beansCount = applicationContext.getBeanDefinitionCount();
        String beanNames[] = applicationContext.getBeanDefinitionNames();

        logger.info(String.format("Found beans (%d):", beansCount));
        for (int i = 0; i < beansCount; i++)
            logger.info(String.format("Bean %d: %s", i, beanNames[i]));
    }
}
