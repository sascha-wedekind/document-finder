package com.bytedompteur.documentfinder.filewalker.dagger;

import com.bytedompteur.documentfinder.PathUtil;
import com.bytedompteur.documentfinder.filewalker.adapter.in.FileWalker;
import com.bytedompteur.documentfinder.filewalker.core.FileWalkerImpl;
import com.bytedompteur.documentfinder.filewalker.core.WalkFileTreeAdapterImpl;
import com.bytedompteur.documentfinder.filewalker.core.WalkerFactory;
import dagger.Module;
import dagger.Provides;

import jakarta.inject.Singleton;
import java.util.concurrent.ExecutorService;

@Module
public class FileWalkerModule {

  @Provides
  @Singleton
  public WalkerFactory provideWalkFileTreeAdapter(PathUtil pathUtil) {
    return new WalkerFactory(new WalkFileTreeAdapterImpl(), pathUtil);
  }

  @Provides
  @Singleton
  public FileWalker provideFileWalker(WalkerFactory factory, ExecutorService executorService, PathUtil pathUtil) {
    return new FileWalkerImpl(factory, executorService, pathUtil);
  }

}
