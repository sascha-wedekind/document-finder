package com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in;

import lombok.Builder;
import lombok.Value;

import java.nio.file.Path;
import java.time.LocalDateTime;

@Value
@Builder
public class SearchResult {

  Path path;
  LocalDateTime fileCreated;
  LocalDateTime fileLastUpdated;

}
