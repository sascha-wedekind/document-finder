package com.bytedompteur.documentfinder.settings.adapter.in;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.nonNull;

public class SettingsChangedCalculator {

  public enum ChangeType {
    FOLDERS,
    FILE_TYPES,
    DEBUG_LOGGING_ENABLED,
    RUN_ON_STARTUP
  }

  public Set<ChangeType> calculateChanges(Settings oldSettings, Settings newSettings) {
    var result = new HashSet<ChangeType>();
    if (nonNull(oldSettings) && nonNull(newSettings)) {
      appendFolderChangedToResult(oldSettings, newSettings, result);
      appendFileTypesChangedToResult(oldSettings, newSettings, result);
      appendDebugLoggingEnabledChangedToResult(oldSettings, newSettings, result);
      appendRunOnStartupChangedToResult(oldSettings, newSettings, result);
    }
    return Collections.unmodifiableSet(result);
  }

  private static void appendRunOnStartupChangedToResult(Settings oldSettings, Settings newSettings, HashSet<ChangeType> result) {
    if (oldSettings.isRunOnStartup() != newSettings.isRunOnStartup()) {
      result.add(ChangeType.RUN_ON_STARTUP);
    }
  }

  private static void appendDebugLoggingEnabledChangedToResult(Settings oldSettings, Settings newSettings, HashSet<ChangeType> result) {
    if (oldSettings.isDebugLoggingEnabled() != newSettings.isDebugLoggingEnabled()) {
      result.add(ChangeType.DEBUG_LOGGING_ENABLED);
    }
  }

  private void appendFileTypesChangedToResult(Settings oldSettings, Settings newSettings, HashSet<ChangeType> result) {
    if(hasFileTypesChanged(oldSettings, newSettings)) {
      result.add(ChangeType.FILE_TYPES);
    }
  }

  private void appendFolderChangedToResult(Settings oldSettings, Settings newSettings, HashSet<ChangeType> result) {
    if (hasFoldersChanged(oldSettings, newSettings)) {
      result.add(ChangeType.FOLDERS);
    }
  }

  boolean hasFoldersChanged(Settings oldSettings, Settings newSettings) {
    return oldSettings.getFolders().size() != newSettings.getFolders().size() ||
      oldSettings.getFolders().stream().anyMatch(folder -> !newSettings.getFolders().contains(folder));
  }

  boolean hasFileTypesChanged(Settings oldSettings, Settings newSettings) {
    return oldSettings.getFileTypes().size() != newSettings.getFileTypes().size() ||
      oldSettings.getFileTypes().stream().anyMatch(fileType -> !newSettings.getFileTypes().contains(fileType));
  }
}
