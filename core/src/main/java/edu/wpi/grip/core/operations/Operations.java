package edu.wpi.grip.core.operations;

import edu.wpi.grip.core.Description;
import edu.wpi.grip.core.FileManager;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OperationDescription;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.events.OperationAddedEvent;
import edu.wpi.grip.core.operations.network.MapNetworkPublisherFactory;
import edu.wpi.grip.core.operations.network.PublishAnnotatedOperation;
import edu.wpi.grip.core.operations.network.Publishable;
import edu.wpi.grip.core.operations.network.PublishableProxy;
import edu.wpi.grip.core.operations.network.PublishableRosProxy;
import edu.wpi.grip.core.operations.network.http.HttpPublishOperation;
import edu.wpi.grip.core.operations.network.networktables.NTPublishAnnotatedOperation;
import edu.wpi.grip.core.operations.network.ros.JavaToMessageConverter;
import edu.wpi.grip.core.operations.network.ros.ROSNetworkPublisherFactory;
import edu.wpi.grip.core.operations.network.ros.ROSPublishOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.reflect.ClassPath;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class Operations {

  private static final Logger logger = Logger.getLogger(Operations.class.getName());

  private final EventBus eventBus;

  private final Injector injector;
  private final InputSocket.Factory isf;
  private final OutputSocket.Factory osf; //NOPMD
  private final MapNetworkPublisherFactory ntManager;
  private final MapNetworkPublisherFactory httpManager;
  private final ROSNetworkPublisherFactory rosManager; //NOPMD
  private final ImmutableList<OperationMetaData> operations;

  private List<Class<Publishable>> publishableTypes = null;

  /**
   * Creates a new Operations instance. This should only be used in tests.
   *
   * @param eventBus    the app-global event bus
   * @param ntManager   the NetworkTable manager
   * @param httpManager the HTTP manager
   * @param rosManager  the ROS manager
   * @param injector    the injector to use to create operations
   * @param fileManager the file manager
   * @param isf         the factory for creating input sockets
   * @param osf         the factory for creating output sockets
   */
  @Inject
  Operations(EventBus eventBus,
             @Named("ntManager") MapNetworkPublisherFactory ntManager,
             @Named("httpManager") MapNetworkPublisherFactory httpManager,
             @Named("rosManager") ROSNetworkPublisherFactory rosManager,
             Injector injector,
             FileManager fileManager,
             InputSocket.Factory isf,
             OutputSocket.Factory osf) {
    this.injector = checkNotNull(injector, "The injector cannot be null");
    this.eventBus = checkNotNull(eventBus, "EventBus cannot be null");
    this.ntManager = checkNotNull(ntManager, "ntManager cannot be null");
    this.httpManager = checkNotNull(httpManager, "httpManager cannot be null");
    this.rosManager = checkNotNull(rosManager, "rosManager cannot be null");
    this.isf = checkNotNull(isf, "InputSocket factory cannot be null");
    this.osf = checkNotNull(osf, "OutputSocket factory cannot be null");
    checkNotNull(fileManager, "fileManager cannot be null");

    List<OperationMetaData> all = new ArrayList<>();
    all.addAll(createBasicOperations());
    all.addAll(createNetworkTableOperations());
    all.addAll(createHttpOperations());
    all.addAll(createRosOperations());

    // Sort alphabetically in each category
    all.sort(Comparator.comparing((OperationMetaData md) -> md.getDescription().category())
        .thenComparing(md -> md.getDescription().name()));
    this.operations = ImmutableList.copyOf(all);
  }

  /**
   * Creates an operation description for the given operation subclass. The subclass <i>must</i>
   * be annotated with {@link Description @Description} or a {@code NullPointerException} will
   * be thrown.
   *
   * @throws NullPointerException if {@code clazz} is null, or if it is not annotated with
   *                              {@code @Description}
   */
  private OperationDescription descriptionFor(Class<? extends Operation> clazz) {
    return OperationDescription.from(clazz.getAnnotation(Description.class));
  }

  @SuppressWarnings("unchecked")
  private List<OperationMetaData> createBasicOperations() {
    try {
      ClassPath cp = ClassPath.from(getClass().getClassLoader());
      return cp.getAllClasses().stream()
          .filter(ci -> ci.getName().startsWith("edu.wpi.grip.core.operations"))
          .map(ClassPath.ClassInfo::load)
          .filter(Operation.class::isAssignableFrom)
          .map(c -> (Class<? extends Operation>) c)
          .filter(c -> c.isAnnotationPresent(Description.class))
          .map(c -> new OperationMetaData(descriptionFor(c), () -> injector.getInstance(c)))
          .collect(Collectors.toList());
    } catch (IOException e) {
      logger.log(Level.WARNING, "Could not discover operations", e);
      return ImmutableList.of();
    }
  }

  /**
   * Finds all subclasses of {@link Publishable} in {@code edu.wpi.grip.core.operation}.
   */
  @SuppressWarnings("unchecked")
  private List<Class<Publishable>> findPublishables() {
    if (publishableTypes == null) {
      // Only need to search once
      try {
        ClassPath cp = ClassPath.from(getClass().getClassLoader());
        publishableTypes = cp.getAllClasses().stream()
            // only look in our namespace (don't want to wade through tens of thousands of classes)
            .filter(ci -> ci.getName().startsWith("edu.wpi.grip.core.operation"))
            .map(ClassPath.ClassInfo::load)
            .filter(Publishable.class::isAssignableFrom)
            // only accept concrete top-level subclasses
            .filter(c -> !c.isAnonymousClass() && !c.isInterface() && !c.isLocalClass()
                && !c.isMemberClass())
            .filter(c -> Modifier.isPublic(c.getModifiers()))
            .map(c -> (Class<Publishable>) c)
            .collect(Collectors.toList());
      } catch (IOException e) {
        logger.log(Level.WARNING, "Could not find the publishable types.", e);
        publishableTypes = ImmutableList.of();
      }
    }
    return publishableTypes;
  }

  /**
   * Creates a list of operation metadata for every kind of publishable type for a specific subclass
   * of {@link PublishAnnotatedOperation}.
   *
   * @param descriptionMaker a function that takes a data class and creates an operation description
   *                         for it
   * @param simpleFactory    the factory to use when the data type is itself publishable
   * @param complexFactory   the factory to use when the publishable type is a proxy for the data
   *                         type
   * @param <D>              the data type (may also be {@code P} if the data type is publishable)
   * @param <P>              the publishable type
   */
  // This is a mess of a function definition
  @SuppressWarnings("unchecked")
  private <D, P extends Publishable> List<OperationMetaData> createPublishAnnotatedOperations(
      Function<Class<D>, OperationDescription> descriptionMaker,
      Function<Class<P>, PublishAnnotatedOperation<D, P>> simpleFactory,
      BiFunction<Class<D>, Class<P>, PublishAnnotatedOperation<D, P>> complexFactory) {
    return findPublishables().stream()
        .flatMap(p -> {
          // Declare the stream type early to help the compiler figure out what the heck is going on
          Stream<OperationMetaData> stream;
          if (p.isAnnotationPresent(PublishableProxy.class)) {
            stream = Stream.of(p.getAnnotation(PublishableProxy.class).value())
                .map(d -> new OperationMetaData(
                    descriptionMaker.apply(d),
                    () -> complexFactory.apply(d, (Class<P>) p)
                ));
          } else {
            // Class<D> == Class<P>
            stream = Stream.of(new OperationMetaData(descriptionMaker.apply((Class<D>) p),
                () -> simpleFactory.apply((Class<P>) p)));
          }
          return stream;
        })
        .collect(Collectors.toList());
  }

  private List<OperationMetaData> createHttpOperations() {
    return createPublishAnnotatedOperations(
        HttpPublishOperation::descriptionFor,
        d -> new HttpPublishOperation<>(isf, d, httpManager),
        (d, p) -> new HttpPublishOperation<>(isf, d, p,
            data -> createPublishableProxy(data, d, p), httpManager)
    );
  }

  private List<OperationMetaData> createNetworkTableOperations() {
    return createPublishAnnotatedOperations(
        NTPublishAnnotatedOperation::descriptionFor,
        d -> new NTPublishAnnotatedOperation<>(isf, d, ntManager),
        (d, p) -> new NTPublishAnnotatedOperation<>(isf, d, p,
            data -> createPublishableProxy(data, d, p), ntManager)
    );
  }

  private List<OperationMetaData> createRosOperations() {
    return Stream.of(JavaToMessageConverter.class.getFields())
        .filter(f -> Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers()))
        .filter(f -> f.isAnnotationPresent(PublishableRosProxy.class))
        .map(f -> {
          Class<?> dataType = f.getAnnotation(PublishableRosProxy.class).value();
          return new OperationMetaData(ROSPublishOperation.descriptionFor(dataType),
              () -> new ROSPublishOperation<>(isf, dataType, rosManager, getStaticField(f)));
        })
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  private <T> T getStaticField(Field f) {
    try {
      return (T) f.get(null);
    } catch (IllegalAccessException e) {
      // This shouldn't have happened
      logger.log(Level.WARNING, "", e);
      throw new AssertionError(e);
    }
  }

  /**
   * Creates a new publishable object to wrap the given data. The publishable proxy class
   * <i>must</i> have a public constructor that only takes a single parameter of type {@code D}.
   * An error will be thrown if this contract is broken.
   *
   * @param data            the data to create a publishable proxy for
   * @param publishableType the type of the publishable proxy class
   */
  private <D, P extends Publishable> P createPublishableProxy(D data,
                                                              Class<D> dataType,
                                                              Class<? extends P> publishableType) {
    checkNotNull(data, "Cannot publish null data");
    try {
      return publishableType.getConstructor(dataType).newInstance(data);
    } catch (ReflectiveOperationException | IllegalArgumentException e) {
      // Uh oh, somebody broke the contract
      throw new InternalError(
          String.format("Operation instantiation failed: new %s(%s)",
              publishableType.getSimpleName(), dataType.getSimpleName()),
          e
      );
    }
  }

  @VisibleForTesting
  ImmutableList<OperationMetaData> operations() {
    return operations;
  }

  /**
   * Submits all operations for addition on the {@link EventBus}.
   */
  public void addOperations() {
    operations.stream()
        .map(OperationAddedEvent::new)
        .forEach(eventBus::post);
  }
}
