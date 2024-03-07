package com.bytedompteur.documentfinder;

import java.util.concurrent.ThreadFactory;

/**
 * Thread factory creating thread with name prefix 'document-finder-'
 */
public class CustomNamePrefixThreadFactoryBuilder {

  public static final String NAME_PREFIX = "document-finder-";

  public ThreadFactory build() {
    return Thread.ofVirtual().name(NAME_PREFIX, 1).factory();
  }
}
