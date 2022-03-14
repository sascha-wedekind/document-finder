package com.bytedompteur.documentfinder.persistedqueue.core;

import com.bytedompteur.documentfinder.persistedqueue.adapter.in.FileEvent;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

import static java.util.Objects.nonNull;

@Value
public class PersistedQueueItem {

  long timestamp;
  QueueModificationType queueModificationType;
  FileEvent.Type fileEventType;
  String path;

  public Optional<String> toFileLine() {
    var result = Optional.<String>empty();
    if (nonNull(queueModificationType) && nonNull(fileEventType) && nonNull(path)) {
      result = Optional.of(
        String.format("%s,%s,%s,'%s'", timestamp, queueModificationType, fileEventType, path)
      );
    }
    return result;
  }

  public static Optional<PersistedQueueItem> fromFileLine(String line) {
    var result = Optional.<PersistedQueueItem>empty();
    if (StringUtils.isNotBlank(line)) {
      var items = line.split(",");
      if (items.length == 4 && isValidPathEntry(items[3])) {
        result = Optional.of(
          new PersistedQueueItem(
            Long.parseLong(items[0]),
            QueueModificationType.valueOf(items[1]),
            FileEvent.Type.valueOf(items[2]),
            items[3].substring(1, items[3].length() - 1)
          )
        );
      }
    }
    return result;
  }

  private static boolean isValidPathEntry(String pathEntry) {
    return pathEntry.length() >= 3 && pathEntry.startsWith("'") && pathEntry.endsWith("'");
  }
}
