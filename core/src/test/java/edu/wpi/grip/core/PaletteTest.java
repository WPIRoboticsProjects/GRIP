package edu.wpi.grip.core;

import com.google.common.eventbus.EventBus;

import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class PaletteTest {
  private Palette palette;
  private OperationMetaData operation;

  @Before
  public void setUp() {
    final EventBus eventBus = new EventBus();
    palette = new Palette(() -> eventBus);
    operation = new OperationMetaData(OperationDescription.builder()
        .name("Find Target")
        .summary("")
        .build(),
        () -> null);
  }

  @Test
  public void testGetOperation() {
    palette.addOperation(operation);
    assertEquals(Optional.of(operation), palette.getOperationByName("Find Target"));
  }

  @Test
  public void testGetAllOperations() {
    palette.addOperation(operation);
    assertEquals(Collections.singleton(operation), new HashSet<>(palette.getOperations()));
  }

  @Test
  public void testGetNonexistantOperation() {
    palette.addOperation(operation);
    assertEquals(Optional.empty(), palette.getOperationByName("Test"));
  }
}
