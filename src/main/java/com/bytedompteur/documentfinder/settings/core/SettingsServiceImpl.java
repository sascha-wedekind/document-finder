package com.bytedompteur.documentfinder.settings.core;

import com.bytedompteur.documentfinder.settings.adapter.in.Settings;
import com.bytedompteur.documentfinder.settings.adapter.in.SettingsService;
import com.bytedompteur.documentfinder.settings.adapter.out.FilesReadWriteAdapter;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.nio.file.Files.notExists;
import static java.util.Objects.isNull;

@RequiredArgsConstructor
@Slf4j
public class SettingsServiceImpl implements SettingsService {

  private final Gson gson = new Gson();

  private final String applicationHomeDirectory;

  private final FilesReadWriteAdapter filesAdapter;

  @Override
  public void save(Settings value) {
    if (isNull(value)) {
      log.warn("Settings parameter is null. Won't save");
      return;
    }

    Path settingsFilePath = createSettingsFilePath();
    log.info("Saving settings to file '{}'", settingsFilePath);
    log.debug("Saving these settings: {}", value);
    try {
      filesAdapter.writeString(settingsFilePath, toJson(value));
      log.info("Saving settings to '{}' succeeded", settingsFilePath);
    } catch (IOException e) {
      log.error("Saving settings to '{}' failed", settingsFilePath, e);
    }
  }

  @Override
  public Optional<Settings> read() {
    var settingsFilePath = createSettingsFilePath();
    log.info("Reading settings from file '{}'", settingsFilePath);
    String settingsJson = null;
    try {
      settingsJson = filesAdapter.readString(settingsFilePath);
      log.info("Reading settings from '{}' succeeded", settingsFilePath);
    } catch (IOException e) {
      log.error("Reading settings from file '{}' failed", settingsFilePath, e);
    }
    var result = Optional.ofNullable(settingsJson).map(this::fromJson);
    result.ifPresent(it -> log.debug("Read these settings: {}", it));
    return result;
  }

  protected String toJson(Settings value) {
    return gson.toJson(value);
  }

  protected Settings fromJson(String value) {
    return gson.fromJson(value, Settings.class);
  }

  protected Path createSettingsFilePath() {
    return Paths.get(applicationHomeDirectory, "settings.json");
  }

  protected void ensureIndexDirectoryExists(Path indexDirectory) throws IOException {
    if (notExists(indexDirectory)) {
      log.info("Application home directory '{}' does not exist. Creating it.", indexDirectory);
      try {
        Files.createDirectories(indexDirectory);
        log.info("Application home directory '{}' created", indexDirectory);
      } catch (IOException e) {
        log.error("Could not create application home directory '{}'", indexDirectory, e);
        throw e;
      }
    } else {
      log.debug("Application home directory '{}' exists", indexDirectory);
    }
  }
}
