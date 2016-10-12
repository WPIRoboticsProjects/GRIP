package edu.wpi.grip.web;


import edu.wpi.grip.core.FileManager;
import edu.wpi.grip.core.GripBasicModule;
import edu.wpi.grip.core.Palette;
import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.web.api.OperationsApiServiceHandler;
import edu.wpi.grip.web.api.PersonsApiServiceHandler;
import edu.wpi.grip.web.api.SocketsApiServiceHandler;
import edu.wpi.grip.web.api.SourcesApiServiceHandler;
import edu.wpi.grip.web.api.StepApiServiceHandler;
import edu.wpi.grip.web.io.StreamingOutputEventListener;
import edu.wpi.grip.web.session.GripSessionModule;
import edu.wpi.grip.web.session.SessionCreatedEvent;
import edu.wpi.grip.web.session.SessionDestroyedEvent;
import edu.wpi.grip.web.session.SessionEventBus;
import edu.wpi.grip.web.swagger.api.OperationsApi;
import edu.wpi.grip.web.swagger.api.OperationsApiService;
import edu.wpi.grip.web.swagger.api.PersonsApi;
import edu.wpi.grip.web.swagger.api.PersonsApiService;
import edu.wpi.grip.web.swagger.api.SocketsApi;
import edu.wpi.grip.web.swagger.api.SocketsApiService;
import edu.wpi.grip.web.swagger.api.SourcesApi;
import edu.wpi.grip.web.swagger.api.SourcesApiService;
import edu.wpi.grip.web.swagger.api.StepsApi;
import edu.wpi.grip.web.swagger.api.StepsApiService;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.google.inject.servlet.SessionScoped;

import org.gwizard.logging.LoggingModule;
import org.gwizard.rest.JaxrsModule;
import org.gwizard.rest.ObjectMapperContextResolver;
import org.gwizard.rest.RestModule;
import org.gwizard.services.Run;
import org.gwizard.swagger.SwaggerConfig;
import org.gwizard.swagger.SwaggerModule;
import org.gwizard.web.EventListenerScanner;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

import javax.inject.Singleton;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.ws.rs.GET;
import javax.ws.rs.Path;


public class GripServletConfig extends GuiceServletContextListener implements HttpSessionListener {
  private static Logger LOGGER = LoggerFactory.getLogger(GripServletConfig.class);
  private final SessionEventBus sessionEventBus = new SessionEventBus("Session Event Bus");
  private EventListenerScanner eventListenerScanner;

  /**
   * A standard JAX-RS resource class.
   */
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
        new GripSessionModule(sessionEventBus),
        createBasicModule(),
        new GripBasicModule(),
        createServletModule(),
        new SwaggerModule(),
        new LoggingModule());
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
    if (eventListenerScanner != null) {
      eventListenerScanner.accept(visit -> {
        if (visit instanceof ServletContextListener) {
          ((ServletContextListener) visit).contextInitialized(servletContextEvent);
        }
      });
    } else {
      LOGGER.error("eventListenerScanner was null. Injector must have failed to be created");
    }
    super.contextDestroyed(servletContextEvent);
  }

  private static Module createBasicModule() {
    return new AbstractModule() {

      @Provides
      private SwaggerConfig swaggerConfig() {
        final SwaggerConfig swaggerConfig = new SwaggerConfig();
        swaggerConfig.setResourcePackages(
            Collections.singletonList("edu.wpi.grip.web.swagger.api"));
        return swaggerConfig;
      }

      @Override
      protected void configure() {
        bind(FileManager.class)
            .toInstance((image, filename) -> {
            });

        bind(HelloResource.class);

        bind(EventBus.class)
            .in(SessionScoped.class);
        bind(Pipeline.class)
            .in(SessionScoped.class);
        bind(Palette.class)
            .to(LoadedPalette.class);

        bind(StreamingOutputEventListener
            .Factory.class);

        bind(StepsApiService.class)
            .to(StepApiServiceHandler.class);

        bind(SourcesApiService.class)
            .to(SourcesApiServiceHandler.class);

        bind(PersonsApiService.class)
            .to(PersonsApiServiceHandler.class);

        bind(OperationsApiService.class)
            .to(OperationsApiServiceHandler.class);

        bind(SocketsApiService.class)
            .to(SocketsApiServiceHandler.class);

        bind(StepsApi.class);
        bind(SourcesApi.class);
        bind(PersonsApi.class);
        bind(OperationsApi.class);
        bind(SocketsApi.class);
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
            .in(Singleton.class);
        serve("/*")
            .with(HttpServletDispatcher.class);

        serve("/grip")
            .with(MJPG.class);
      }

    };
  }

  @Override
  public void sessionCreated(HttpSessionEvent httpSessionEvent) {
    LOGGER.info("Config Session created {}", httpSessionEvent);
    sessionEventBus.register(new SessionCreatedEvent(httpSessionEvent.getSession()));
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
    LOGGER.info("Config Session destroyed {}", httpSessionEvent);
    sessionEventBus.register(new SessionDestroyedEvent(httpSessionEvent.getSession()));
  }

  /**
   * Main entry point for testing.
   *
   * @param args The input to the process
   */
  public static void main(String... args) {
    Guice.createInjector(
        new GripSessionModule(new SessionEventBus("")),
        createBasicModule(),
        new RestModule(),
        new SwaggerModule())
        .getInstance(Run.class).start();
  }
}
