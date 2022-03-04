package com.bytedompteur.documentfinder.settings.adapter.in;

import java.util.List;
import java.util.Optional;

public interface SettingsService {

  void save(Settings value);

  Optional<Settings> read();

  default Settings getDefaultSettings() {
    var userHomeDir = System.getProperty("user.home");
    return Settings
      .builder()
      .fileTypes(List.of(
        "pdf",
        "txt",
        "rtf",
        "xls",
        "xlsx",
        "ppt",
        "pptx",
        "doc",
        "docx",
        "odf",
        "ods",
        "ott",
        "pub",
        "pages",
        "numbers",
        "keynote",
        "wpd"
      ))
      .folders(List.of(userHomeDir))
      .build();
  }
}
