package com.bytedompteur.documentfinder;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread factory creating thread with name prefix 'document-finder-'
 */
@SuppressWarnings("NullableProblems")
public class CustomNamePrefixThreadFactory implements java.util.concurrent.ThreadFactory {

  public static final String NAME_PREFIX = "document-finder-";
  private final AtomicInteger threadNumber = new AtomicInteger(1);

  @Override
  public Thread newThread(Runnable r) {
    var result = new Thread(r, NAME_PREFIX + threadNumber.getAndIncrement());

    if (result.isDaemon())
      result.setDaemon(false);
    if (result.getPriority() != Thread.NORM_PRIORITY)
      result.setPriority(Thread.NORM_PRIORITY);

    return result;
  }
}
