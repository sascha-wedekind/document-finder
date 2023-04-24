package com.bytedompteur.documentfinder.settings.adapter.in;

import lombok.Builder;
import lombok.Value;
import lombok.With;

import java.io.Serializable;
import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.isNull;

@SuppressWarnings("ClassCanBeRecord")
@Value
@Builder
public class Settings implements Serializable {
  @With
  @SuppressWarnings("java:S1948")
  List<String> folders;
  @With
  @SuppressWarnings("java:S1948")
  List<String> fileTypes;

  @With
  boolean debugLoggingEnabled;

  @With
  transient boolean runOnStartup;

  @SuppressWarnings("unused")
  public List<String> getFolders() {
    return isNull(folders) ? List.of() : unmodifiableList(folders);
  }

  @SuppressWarnings("unused")
  public List<String> getFileTypes() {
    return isNull(fileTypes) ? List.of() : unmodifiableList(fileTypes);
  }
}
