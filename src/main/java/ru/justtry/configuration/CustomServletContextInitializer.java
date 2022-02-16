package ru.justtry.configuration;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;


@Configuration
public class CustomServletContextInitializer implements ServletContextInitializer
{
    
    @Override
    public void onStartup(ServletContext container)
    {
        AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
        ctx.register(CustomWebMvcConfigurer.class);
        ctx.setServletContext(container);

        ServletRegistration.Dynamic servlet = container.addServlet(
            "dispatcherExample", new DispatcherServlet(ctx));
        servlet.setLoadOnStartup(1);
        servlet.addMapping("/");
        servlet.setMultipartConfig(new MultipartConfigElement("", 20848820, 418018841, 1048576));
    }

}
