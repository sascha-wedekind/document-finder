package com.bytedompteur.documentfinder.settings.adapter.in;

import lombok.Builder;
import lombok.Value;

import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.isNull;

@SuppressWarnings("ClassCanBeRecord")
@Value
@Builder(toBuilder = true)
public class Settings {
  List<String> folders;
  List<String> fileTypes;

  boolean debugLoggingEnabled;

  boolean runOnStartup;

  @SuppressWarnings("unused")
  public List<String> getFolders() {
    return isNull(folders) ? List.of() : unmodifiableList(folders);
  }

  @SuppressWarnings("unused")
  public List<String> getFileTypes() {
    return isNull(fileTypes) ? List.of() : unmodifiableList(fileTypes);
  }
}
