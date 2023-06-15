module DocumentFinder.main {

  requires reactor.core;
  requires org.slf4j;
  requires dagger;
  requires dev.failsafe.core;
  requires transitive  javafx.graphics;
  requires com.google.common;
  requires org.apache.lucene.core;
  requires org.apache.lucene.queryparser;
  requires org.apache.commons.lang3;
  requires org.apache.lucene.analysis.common;
  requires org.apache.tika.core;
  requires transitive  com.google.gson;
  requires transitive  javafx.fxml;
  requires transitive  javafx.controls;
  requires transitive  javafx.swing;
  requires transitive  java.compiler;
  requires org.reactivestreams;
  requires transitive de.jensd.fx.glyphs.materialicons;
  requires transitive de.jensd.fx.glyphs.commons;
  requires ch.qos.logback.classic;
  requires jdk.management;
  requires com.jthemedetector;
  requires org.cryptomator.integrations.api;

  requires static javax.inject;
  requires static lombok;

  opens com.bytedompteur.documentfinder.ui.mainwindow to javafx.fxml, javafx.base;
  opens com.bytedompteur.documentfinder.ui.optionswindow to javafx.fxml, javafx.base;
  opens com.bytedompteur.documentfinder.settings.adapter.in to com.google.gson;
  opens com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.messages to com.google.gson;

  exports com.bytedompteur.documentfinder.ui.mainwindow to javafx.fxml;
  exports com.bytedompteur.documentfinder.ui.optionswindow to javafx.fxml;
  exports com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.messages to com.google.gson;
  exports com.bytedompteur.documentfinder.ui to javafx.fxml;
  opens com.bytedompteur.documentfinder.ui to javafx.base, javafx.fxml, javafx.graphics;
  exports com.bytedompteur.documentfinder.ui.adapter.out to javafx.fxml;
  opens com.bytedompteur.documentfinder.ui.adapter.out to javafx.base, javafx.fxml, javafx.graphics;
}
