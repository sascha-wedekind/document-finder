package com.bytedompteur.documentfinder.commands;

import lombok.RequiredArgsConstructor;

import javax.inject.Inject;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class StartAllCommand implements Runnable {

  private final StartFulltextSearchServiceCommand startFulltextSearchServiceCommand;
  private final StartDirectoryWatcherCommand startDirectoryWatcherCommand;
  private final StartFileWalkerCommand startFileWalkerCommand;
  private final ApplyLogLevelFromSettingsCommand applyLogLevelFromSettingsCommand;

  @Override
  public void run() {
    applyLogLevelFromSettingsCommand.run();
    startFulltextSearchServiceCommand.run();
    startDirectoryWatcherCommand.run();
    startFileWalkerCommand.run();
  }
}
