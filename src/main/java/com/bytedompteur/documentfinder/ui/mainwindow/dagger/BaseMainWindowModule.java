package com.bytedompteur.documentfinder.ui.mainwindow.dagger;

import com.bytedompteur.documentfinder.ui.FxController;
import com.bytedompteur.documentfinder.ui.FxmlFile;
import com.bytedompteur.documentfinder.ui.dagger.FxmlLoaderFactory;
import com.bytedompteur.documentfinder.ui.dagger.FxmlParent;
import com.bytedompteur.documentfinder.ui.mainwindow.AnalyzeFilesProgressBarController;
import com.bytedompteur.documentfinder.ui.mainwindow.MainWindowController;
import com.bytedompteur.documentfinder.ui.mainwindow.SearchResultTableController;
import dagger.Module;
import dagger.Provides;
import jakarta.inject.Provider;
import javafx.scene.Parent;

import java.util.Map;

@Module
public abstract class BaseMainWindowModule {

  // Moved from @Binds to @Provides because of a bug in Dagger 2.52
  @Provides
  static Map<Class<? extends FxController>, Provider<FxController>> controllerFactoriesByClassMap(
    SearchResultTableController value1,
    MainWindowController value2,
    AnalyzeFilesProgressBarController value3
  ) {
    return Map.of(
      SearchResultTableController.class, () -> value1,
      MainWindowController.class, () -> value2,
      AnalyzeFilesProgressBarController.class, () -> value3
    );
  }

  @Provides
  static FxmlLoaderFactory provideFxmlLoaderFactory(Map<Class<? extends FxController>, Provider<FxController>> controllerFactoriesByClassMap) {
    return new FxmlLoaderFactory(controllerFactoriesByClassMap);
  }

  @Provides
  @FxmlParent(FxmlFile.SEARCH_RESULT_TABLE)
  static Parent provideSearchResultTable(FxmlLoaderFactory factory) {
    return factory.createParentNode(FxmlFile.SEARCH_RESULT_TABLE);
  }

  @Provides
  @FxmlParent(FxmlFile.MAIN_VIEW)
  static Parent provideMainView(FxmlLoaderFactory factory) {
    return factory.createParentNode(FxmlFile.MAIN_VIEW);
  }

  @Provides
  @FxmlParent(FxmlFile.PROGRESS_BAR)
  static Parent provideAnalyzeFilesProgressBar(FxmlLoaderFactory factory) {
    return factory.createParentNode(FxmlFile.PROGRESS_BAR);
  }

}
