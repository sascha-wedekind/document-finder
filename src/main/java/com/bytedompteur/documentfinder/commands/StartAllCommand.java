package com.bytedompteur.documentfinder.commands;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
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
