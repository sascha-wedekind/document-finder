package com.bytedompteur.documentfinder.directorywatcher.core;

import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.Comparator;

public class AbsolutePathWatchEventByKindComparator implements Comparator<AbsolutePathWatchEvent> {

  @Override
  public int compare(AbsolutePathWatchEvent o1, AbsolutePathWatchEvent o2) {
    return toNumber(o1.kind()) - toNumber(o2.kind());
  }

  public static int toNumber(WatchEvent.Kind<Path> kind) {
    int result;
    if (StandardWatchEventKinds.ENTRY_CREATE.equals(kind)) {
      result = 0;
    } else if (StandardWatchEventKinds.ENTRY_MODIFY.equals(kind)) {
      result = 1;
    } else {
      result = 3;
    }
    return result;
  }
}
