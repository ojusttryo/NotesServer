package ru.justtry.configuration;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import ru.justtry.rest.controllers.AttributesController;
import ru.justtry.rest.controllers.EntitiesController;
import ru.justtry.rest.controllers.LogController;
import ru.justtry.rest.controllers.StatisticsController;
import ru.justtry.servlet.AttributesServlet;
import ru.justtry.servlet.EntitiesServlet;
import ru.justtry.servlet.EntityServlet;
import ru.justtry.servlet.ErrorServlet;
import ru.justtry.servlet.LogServlet;
import ru.justtry.servlet.ResourceServlet;
import ru.justtry.servlet.StatisticsServlet;

/**
 * Конфигурация сервлетов.
 *
 * Почему-то только с этим способом пока удалось заставить работать. Возможно, потом стоит переделать
 * https://www.baeldung.com/register-servlet
 */
@Configuration
@RequiredArgsConstructor
public class ServletConfiguration
{
    private final LogController logController;
    private final StatisticsController statisticsController;
    private final AttributesController attributesController;
    private final EntitiesController entitiesController;


    @Bean
    public ServletRegistrationBean<LogServlet> logServletBean() {
        LogServlet servlet = new LogServlet(logController);
        ServletRegistrationBean<LogServlet> registrationBean =
                new ServletRegistrationBean<>(servlet, "/log/*");
        registrationBean.setLoadOnStartup(1);
        return registrationBean;
    }

    @Bean
    public ServletRegistrationBean<ResourceServlet> resourceServletBean() {
        ResourceServlet servlet = new ResourceServlet();
        ServletRegistrationBean<ResourceServlet> registrationBean =
                new ServletRegistrationBean<>(servlet, "/resources/web/*");
        registrationBean.setLoadOnStartup(2);
        return registrationBean;
    }

    @Bean
    public ServletRegistrationBean<StatisticsServlet> statisticsServletBean() {
        StatisticsServlet servlet = new StatisticsServlet(statisticsController);
        ServletRegistrationBean<StatisticsServlet> registrationBean =
                new ServletRegistrationBean<>(servlet, "/statistics");
        registrationBean.setLoadOnStartup(3);
        return registrationBean;
    }

    @Bean
    public ServletRegistrationBean<AttributesServlet> attributesServletBean() {
        AttributesServlet servlet = new AttributesServlet(attributesController, entitiesController);
        ServletRegistrationBean<AttributesServlet> registrationBean =
            new ServletRegistrationBean<>(servlet, "/attributes");
        registrationBean.setLoadOnStartup(4);
        return registrationBean;
    }

    @Bean
    public ServletRegistrationBean<EntitiesServlet> entitiesServletBean() {
        EntitiesServlet servlet = new EntitiesServlet(entitiesController);
        ServletRegistrationBean<EntitiesServlet> registrationBean =
            new ServletRegistrationBean<>(servlet, "/entities");
        registrationBean.setLoadOnStartup(5);
        return registrationBean;
    }

    @Bean
    public ServletRegistrationBean<EntityServlet> entityServletBean() {
        EntityServlet servlet = new EntityServlet(entitiesController, attributesController);
        ServletRegistrationBean<EntityServlet> registrationBean =
            new ServletRegistrationBean<>(servlet, "/entities/entity/*");
        registrationBean.setLoadOnStartup(6);
        return registrationBean;
    }

    @Bean
    public ServletRegistrationBean<ErrorServlet> errorServletBean() {
        ErrorServlet servlet = new ErrorServlet();
        ServletRegistrationBean<ErrorServlet> registrationBean =
            new ServletRegistrationBean<>(servlet, "/error-not-used");
        registrationBean.setLoadOnStartup(7);
        return registrationBean;
    }

}
