package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.thoughtworks.xstream.XStream;
import edu.wpi.grip.core.events.UnexpectedThrowableEvent;
import edu.wpi.grip.core.operations.Operations;
import edu.wpi.grip.core.serialization.*;
import edu.wpi.grip.core.sources.CameraSource;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.generated.CVOperations;

import javax.inject.*;

/**
 * A Guice {@link com.google.inject.Module} for GRIP's core package.  This is where instances of {@link Pipeline},
 * {@link Palette}, {@link Project}, etc... are created.
 */
public class GRIPCoreModule extends AbstractModule {

    private final EventBus eventBus = new EventBus(this::onSubscriberException);
    private final Pipeline pipeline = new Pipeline(eventBus);
    private final Palette palette = new Palette(eventBus);

    public GRIPCoreModule() {
        Thread.setDefaultUncaughtExceptionHandler(this::onThreadException);
    }

    @Override
    protected void configure() {
        bind(Pipeline.class).toInstance(pipeline);
        bind(Palette.class).toInstance(palette);
        bind(Project.class).asEagerSingleton();

        // Register any injected object on the event bus
        bindListener(Matchers.any(), new TypeListener() {
            @Override
            public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
                encounter.register((InjectionListener<I>) eventBus::register);
            }
        });
    }

    @Provides
    @Singleton
    public EventBus provideEventBus() {
        Operations.addOperations(eventBus);
        CVOperations.addOperations(eventBus);
        return eventBus;
    }

    @Provides
    @Singleton
    public XStream provideXStream() {
        final XStream xstream = new XStream();
        xstream.setMode(XStream.NO_REFERENCES);
        xstream.registerConverter(new StepConverter(eventBus, palette));
        xstream.registerConverter(new SourceConverter(eventBus, xstream.getMapper()));
        xstream.registerConverter(new SocketConverter(xstream.getMapper(), pipeline));
        xstream.registerConverter(new ConnectionConverter(eventBus));
        xstream.processAnnotations(new Class[]{Pipeline.class, Step.class, Connection.class, InputSocket.class,
                OutputSocket.class, ImageFileSource.class, CameraSource.class});
        return xstream;
    }

    private void onSubscriberException(Throwable exception, SubscriberExceptionContext context) {
        eventBus.post(new UnexpectedThrowableEvent(exception, "An event subscriber threw an exception"));
    }

    private void onThreadException(Thread thread, Throwable exception) {
        eventBus.post(new UnexpectedThrowableEvent(exception, thread + " threw an exception"));
    }
}
