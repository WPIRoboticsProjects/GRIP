package edu.wpi.grip.web;


import edu.wpi.grip.core.Palette;
import edu.wpi.grip.core.operations.BasicOperations;
import edu.wpi.grip.core.operations.CVOperations;

import com.google.common.eventbus.EventBus;
import com.google.inject.servlet.SessionScoped;

import javax.inject.Inject;
import javax.inject.Provider;

@SessionScoped
public class LoadedPalette extends Palette {

  @Inject
  LoadedPalette(Provider<EventBus> eventBus,
                BasicOperations basicOperations,
                CVOperations cvOperations) {
    super(eventBus);
    addOperations(basicOperations);
    addOperations(cvOperations);
  }
}
