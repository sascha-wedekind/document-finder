package com.bytedompteur.documentfinder.commands;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.bytedompteur.documentfinder.settings.adapter.in.SettingsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ApplyLogLevelFromSettingsCommand implements Runnable {

  public static final String DOCUMENT_FINDER_PACKAGE_NAME = "com.bytedompteur.documentfinder";

  private final SettingsService settingsService;

  @Override
  public void run() {
    if (settingsService.read().orElse(settingsService.getDefaultSettings()).isDebugLoggingEnabled()) {
      setLogLevel(Level.DEBUG.toString());
    } else {
      setLogLevel(Level.INFO.toString());
    }
  }


  private static void setLogLevel(String logLevel) {
    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    ch.qos.logback.classic.Logger logger = loggerContext.getLogger(DOCUMENT_FINDER_PACKAGE_NAME);
    var newLogLevel = Level.toLevel(logLevel);
    if (!newLogLevel.equals(logger.getLevel())) {
      logger.setLevel(newLogLevel);
    }
  }
}
