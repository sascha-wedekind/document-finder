package com.bytedompteur.documentfinder.filewalker.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Playground {

  // https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Files.html walkFileTree


  public static void main(String[] args) throws IOException {

    Path projectPath = Path.of("X:\\OneDrive\\Dokumente\\DokumentePrivate");

    FileEndingMatcher fileEndingMatcher = new FileEndingMatcher(Set.of("pdf", "doxc", "doc"));

    Set<Path> filesToWatch = new TreeSet<>();
    Files.walkFileTree(projectPath, new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (fileEndingMatcher.matches(file)) {
          log.debug("File '{}' matches filter options", file);
          filesToWatch.add(file);
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        log.info("Searching in '{}'", dir);
        return super.preVisitDirectory(dir, attrs);
      }
    });

    System.out.println();

  }

}
