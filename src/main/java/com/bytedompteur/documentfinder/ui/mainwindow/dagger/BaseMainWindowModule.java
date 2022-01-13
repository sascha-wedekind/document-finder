package com.bytedompteur.documentfinder.ui.mainwindow.dagger;

import com.bytedompteur.documentfinder.fulltextsearchengine.dagger.FulltextSearchEngineModule;
import com.bytedompteur.documentfinder.ui.FxController;
import com.bytedompteur.documentfinder.ui.FxmlFile;
import com.bytedompteur.documentfinder.ui.dagger.FxControllerMapKey;
import com.bytedompteur.documentfinder.ui.dagger.FxmlLoaderFactory;
import com.bytedompteur.documentfinder.ui.dagger.FxmlParent;
import com.bytedompteur.documentfinder.ui.mainwindow.AnalyzeFilesProgressBarController;
import com.bytedompteur.documentfinder.ui.mainwindow.MainWindowController;
import com.bytedompteur.documentfinder.ui.mainwindow.SearchResultTableController;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import javafx.scene.Parent;

import javax.inject.Provider;
import java.util.Map;

@Module(includes = {FulltextSearchEngineModule.class})
public abstract class BaseMainWindowModule {

  @Binds
  @IntoMap
  @FxControllerMapKey(SearchResultTableController.class)
  abstract FxController bindSearchResultTableController(SearchResultTableController value);

  @Binds
  @IntoMap
  @FxControllerMapKey(MainWindowController.class)
  abstract FxController bindMainViewController(MainWindowController value);

  @Binds
  @IntoMap
  @FxControllerMapKey(AnalyzeFilesProgressBarController.class)
  abstract FxController bindAnalyzeFilesProgressBarController(AnalyzeFilesProgressBarController value);

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
