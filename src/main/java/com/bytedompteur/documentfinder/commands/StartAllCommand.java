package com.bytedompteur.documentfinder.commands;

import lombok.RequiredArgsConstructor;

import javax.inject.Inject;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class StartAllCommand implements Runnable {

  private final StartFulltextSearchServiceCommand startFulltextSearchServiceCommand;
  private final StartDirectoryWatcherCommand startDirectoryWatcherCommand;
  private final StartFileWalkerCommand startFileWalkerCommand;

  @Override
  public void run() {
    startFulltextSearchServiceCommand.run();
    startDirectoryWatcherCommand.run();
    startFileWalkerCommand.run();
  }
}
