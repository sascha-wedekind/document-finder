package com.bytedompteur.documentfinder.ui.optionswindow.dagger;

import com.bytedompteur.documentfinder.PathUtil;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module(includes = BaseOptionsWindowModule.class)
public abstract class OptionsWindowModule {

  @Provides
  @Singleton
  public static PathUtil providePathUtil() {
    return new PathUtil();
  }

}
