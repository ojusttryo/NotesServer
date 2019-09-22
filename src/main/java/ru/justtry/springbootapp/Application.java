package ru.justtry.springbootapp;

import org.apache.log4j.Logger;
//import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import ru.justtry.database.DatabaseConfiguration;

import javax.inject.Inject;
import java.net.MalformedURLException;

@Configuration("application.properties")
@EnableAutoConfiguration
@PropertySource("classpath:application.properties")
@ComponentScan(basePackages = "ru.justtry")
//@ComponentScan(basePackages = "ru.justtry")
public class Application
{
    // To see logger debug info at start use Run configurations - VM option -Dlog4j.debug
    private static Logger logger = Logger.getLogger(Application.class);// LogManager.getLogger(Application.class.getName());
    //private static AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(Application.class);
    //private static AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();


    public static void main(String[] args) throws MalformedURLException
    {
        //AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
        //applicationContext.register(DatabaseConfiguration.class);
        //applicationContext.refresh();
        //PropertyConfigurator.configure(Application.class.getResource("log4j.properties"));
        //System.setProperty("log4j.configuration", Application.class.getResource("log4j.properties").toString());
        //System.setProperty("log4j.configuration", new File("resources", "ru/justtry/springbootapp/log4j.properties").toURI().toURL().toString());

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
