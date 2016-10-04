package edu.wpi.grip.web;


import edu.wpi.grip.web.swagger.api.PersonsApi;
import edu.wpi.grip.web.swagger.api.StepsApi;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import org.gwizard.rest.JaxrsModule;
import org.gwizard.rest.ObjectMapperContextResolver;
import org.gwizard.rest.RestModule;
import org.gwizard.services.Run;
import org.gwizard.swagger.SwaggerConfig;
import org.gwizard.swagger.SwaggerModule;
import org.gwizard.web.EventListenerScanner;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;

import java.util.Collections;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.ws.rs.GET;
import javax.ws.rs.Path;


public class GripServletConfig extends GuiceServletContextListener {
  private EventListenerScanner eventListenerScanner;
  /** A standard JAX-RS resource class */
  @Path("/hello")
  public static class HelloResource {
    @GET
    public String hello() {
      return "hello, world";
    }
  }

  @Override
  protected Injector getInjector() {
    final Injector injector = Guice.createInjector(
        //createServletModule(),
        createBasicModule(),
        createServletModule(),
        new SwaggerModule());
    eventListenerScanner
        = injector.getInstance(EventListenerScanner.class);
    return injector;
  }

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    // Assign this first. getInjector is called in the super method.
    super.contextInitialized(servletContextEvent);
    // This will be initialized by now
    eventListenerScanner.accept(visit -> {
      if (visit instanceof ServletContextListener) {
        ((ServletContextListener) visit).contextInitialized(servletContextEvent);
      }
    });
  }

  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    eventListenerScanner.accept(visit -> {
      if (visit instanceof ServletContextListener) {
        ((ServletContextListener) visit).contextInitialized(servletContextEvent);
      }
    });
    super.contextDestroyed(servletContextEvent);
  }

  private static Module createBasicModule() {
    return new AbstractModule() {

      @Provides
      private SwaggerConfig swaggerConfig() {
        System.out.println("Initializing swagger config");
        final SwaggerConfig swaggerConfig = new SwaggerConfig();
        swaggerConfig.setResourcePackages(
            Collections.singletonList("edu.wpi.grip.web.swagger.api"));
        return swaggerConfig;
      }

      @Override
      protected void configure() {
        bind(HelloResource.class);
        bind(StepsApi.class);
        bind(PersonsApi.class);
      }
    };
  }

  private static Module createServletModule() {
    return new ServletModule() {

      @Override
      protected void configureServlets() {
        install(new JaxrsModule());

        // Binding this will cause it to be picked up by gwizard-web
        bind(GuiceResteasyBootstrapServletContextListener.class);

        // Make sure RESTEasy picks this up so we get our ObjectMapper from guice
        bind(ObjectMapperContextResolver.class);

        bind(HttpServletDispatcher.class)
            .in(javax.inject.Singleton.class);
        serve("/*")
            .with(HttpServletDispatcher.class);

        serve("/grip")
            .with(MJPG.class);
      }

    };
  }

  public static void main(String ... args) {
    Guice.createInjector(createBasicModule(),
        new RestModule(),
        new SwaggerModule())
        .getInstance(Run.class).start();
  }
}
