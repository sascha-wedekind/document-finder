package com.bytedompteur.documentfinder.commands;

import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in.FulltextSearchService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClearFulltextSearchServiceIndexCommand implements Runnable {

  private final FulltextSearchService service;


  @Override
  public void run() {
    service.clearIndex();
  }
}
