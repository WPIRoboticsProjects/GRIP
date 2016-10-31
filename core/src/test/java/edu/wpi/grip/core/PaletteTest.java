package edu.wpi.grip.core;

import edu.wpi.grip.core.events.OperationAddedEvent;

import com.google.common.eventbus.EventBus;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PaletteTest {
  private Palette palette;
  private EventBus eventBus;
  private OperationMetaData operation;

  @Before
  public void setUp() {
    eventBus = new EventBus();
    palette = new Palette(eventBus);
    eventBus.register(palette);
    operation = new OperationMetaData(OperationDescription.builder()
        .name("Find Target")
        .summary("")
        .build(),
        MockOperation::new);
  }

  @Test
  public void testGetOperation() {
    eventBus.post(new OperationAddedEvent(operation));
    assertEquals(Optional.of(operation), palette.getOperationByName("Find Target"));
  }

  @Test
  public void testGetAllOperations() {
    eventBus.post(new OperationAddedEvent(operation));
    assertEquals(Collections.singleton(operation), new HashSet<>(palette.getOperations()));
  }

  @Test
  public void testGetNonexistantOperation() {
    eventBus.post(new OperationAddedEvent(operation));
    assertEquals(Optional.empty(), palette.getOperationByName("Test"));
  }

  @Test
  public void testReplacedOperation() {
    // Only a custom operation may be replaced, and only by another custom operation
    // with the same name
    final String name = "Custom Operation";
    OperationMetaData first = new OperationMetaData(OperationDescription.builder()
        .name(name)
        .summary("A summary")
        .category(OperationDescription.Category.CUSTOM)
        .build(),
        MockOperation::new);
    OperationMetaData second = new OperationMetaData(OperationDescription.builder()
        .name(name)
        .summary("Another summary")
        .category(OperationDescription.Category.CUSTOM)
        .build(),
        MockOperation::new);
    eventBus.post(new OperationAddedEvent(first));
    assertTrue(palette.getOperations().contains(first));
    assertFalse(palette.getOperations().contains(second));
    eventBus.post(new OperationAddedEvent(second));
    assertFalse(palette.getOperations().contains(first));
    assertTrue(palette.getOperations().contains(second));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddOperationWithSameName() {
    OperationMetaData custom = new OperationMetaData(OperationDescription.builder()
        .name(operation.getDescription().name())
        .summary("Custom operation with the name of another operation in the palette")
        .category(OperationDescription.Category.CUSTOM)
        .build(),
        MockOperation::new);
    palette.onOperationAdded(new OperationAddedEvent(operation)); // EventBus messes with exceptions
    palette.onOperationAdded(new OperationAddedEvent(custom));
  }

}
