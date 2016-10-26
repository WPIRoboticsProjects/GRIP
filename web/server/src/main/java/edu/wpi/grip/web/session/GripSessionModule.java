package edu.wpi.grip.web.session;


import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class GripSessionModule extends AbstractModule {
  private final SessionEventBus sessionEventBus;

  public GripSessionModule(SessionEventBus sessionEventBus) {
    this.sessionEventBus = sessionEventBus;
  }

  @Override
  protected void configure() {
    bind(EventBus.class)
        .annotatedWith(Names.named("Session Event Bus"))
        .toInstance(sessionEventBus);
    bindListener(Matchers.any(), new TypeListener() {
      @Override
      public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
        if (type.getRawType().isAnnotationPresent(SessionEventRegistered.class)) {
          encounter.register((InjectionListener<I>) sessionEventBus::register);
        }
      }
    });
  }
}
