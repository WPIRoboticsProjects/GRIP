package edu.wpi.grip.core;

import com.google.inject.AbstractModule;

public class GripFileModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(FileManager.class).to(GripFileManager.class);
  }

}
