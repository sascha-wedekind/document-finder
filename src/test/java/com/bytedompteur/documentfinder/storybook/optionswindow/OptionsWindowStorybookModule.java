package com.bytedompteur.documentfinder.storybook.optionswindow;

import com.bytedompteur.documentfinder.PathUtil;
import com.bytedompteur.documentfinder.ui.optionswindow.dagger.BaseOptionsWindowModule;
import dagger.Module;
import dagger.Provides;
import org.mockito.Mockito;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashSet;

@Module(includes = BaseOptionsWindowModule.class)
public abstract class OptionsWindowStorybookModule {

  @Provides
  @Singleton
  public static PathUtil providePathUtil() {
    var mockedPathUtil = Mockito.mock(PathUtil.class);
    Mockito
      .when(mockedPathUtil.isDirectory(Mockito.anyString()))
      .thenReturn(true);

    Mockito
      .when(mockedPathUtil.removeChildPaths(Mockito.anyList()))
      .then(invocation -> new HashSet<>((Collection<?>) invocation.getArguments()[0]));

    return mockedPathUtil;
  }

}
