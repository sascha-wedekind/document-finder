module DocumentFinder.main {

  requires transitive  reactor.core;
  requires transitive  org.slf4j;
  requires transitive  dagger;
  requires transitive  dev.failsafe;
  requires transitive  javafx.graphics;
  requires transitive  com.google.common;
  requires transitive  org.apache.lucene.core;
  requires transitive  org.apache.lucene.queryparser;
  requires transitive  org.apache.commons.lang3;
  requires transitive  org.apache.lucene.analysis.common;
  requires transitive  org.apache.tika.core;
  requires transitive  org.apache.commons.logging;
  requires transitive  com.google.gson;
  requires transitive  javafx.fxml;
  requires transitive  javafx.controls;
  requires transitive  javafx.swing;
  requires transitive  java.compiler;
  requires transitive  org.reactivestreams;
  requires transitive de.jensd.fx.glyphs.materialicons;
  requires transitive de.jensd.fx.glyphs.commons;

  requires static javax.inject;
  requires static lombok;

  opens com.bytedompteur.documentfinder.ui to javafx.graphics;
  opens com.bytedompteur.documentfinder.ui.mainwindow to javafx.fxml, javafx.base;
  opens com.bytedompteur.documentfinder.ui.optionswindow to javafx.fxml, javafx.base;
  opens com.bytedompteur.documentfinder.settings.adapter.in to com.google.gson;

  exports com.bytedompteur.documentfinder.ui.mainwindow to javafx.fxml;
  exports com.bytedompteur.documentfinder.ui.optionswindow to javafx.fxml;
}
