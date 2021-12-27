package com.bytedompteur.documentfinder.filewalker.dagger;

import com.bytedompteur.documentfinder.filewalker.adapter.in.FileWalker;
import com.bytedompteur.documentfinder.filewalker.core.FileWalkerImpl;
import com.bytedompteur.documentfinder.filewalker.core.WalkFileTreeAdapterImpl;
import com.bytedompteur.documentfinder.filewalker.core.WalkerFactory;
import dagger.Module;
import dagger.Provides;
import java.util.concurrent.ExecutorService;
import javax.inject.Singleton;

@Module
public class FileWalkerModule {

  @Provides
  @Singleton
  public WalkerFactory provideWalkFileTreeAdapter() {
    return new WalkerFactory(new WalkFileTreeAdapterImpl());
  }

  @Provides
  @Singleton
  public FileWalker provideFileWalker(WalkerFactory factory, ExecutorService executorService) {
    return new FileWalkerImpl(factory, executorService);
  }

}
