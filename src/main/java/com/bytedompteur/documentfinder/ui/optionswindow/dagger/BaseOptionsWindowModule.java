package com.bytedompteur.documentfinder.ui.optionswindow.dagger;

import com.bytedompteur.documentfinder.ui.FxController;
import com.bytedompteur.documentfinder.ui.FxmlFile;
import com.bytedompteur.documentfinder.ui.dagger.FxControllerMapKey;
import com.bytedompteur.documentfinder.ui.dagger.FxmlLoaderFactory;
import com.bytedompteur.documentfinder.ui.dagger.FxmlParent;
import com.bytedompteur.documentfinder.ui.optionswindow.*;
import dagger.Binds;
import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import javafx.scene.Parent;

import javax.inject.Provider;
import java.util.Map;

@Module
public abstract class BaseOptionsWindowModule {

  @Binds
  @IntoMap
  @FxControllerMapKey(OptionsWindowController.class)
  abstract FxController bindOptionsWindowController(OptionsWindowController value);

  @Binds
  @IntoMap
  @FxControllerMapKey(FileTypeOptionsController.class)
  abstract FxController bindFileTypeOptionsController(FileTypeOptionsController value);

  @Binds
  @IntoMap
  @FxControllerMapKey(FolderOptionsController.class)
  abstract FxController bindFolderOptionsController(FolderOptionsController value);

  @Provides
  static FxmlLoaderFactory provideFxmlLoaderFactory(Map<Class<? extends FxController>, Provider<FxController>> controllerFactoriesByClassMap) {
    return new FxmlLoaderFactory(controllerFactoriesByClassMap);
  }

  @Provides
  @FxmlParent(FxmlFile.OPTIONS_VIEW)
  static Parent provideSearchResultTable(FxmlLoaderFactory factory) {
    return factory.createParentNode(FxmlFile.OPTIONS_VIEW);
  }

  @Provides
  @FxmlParent(FxmlFile.FILE_TYPE_OPTIONS)
  static Parent provideFileTypeOptions(FxmlLoaderFactory factory) {
    return factory.createParentNode(FxmlFile.FILE_TYPE_OPTIONS);
  }

  @Provides
  @FxmlParent(FxmlFile.FOLDER_OPTIONS)
  static Parent provideFolderOptions(FxmlLoaderFactory factory) {
    return factory.createParentNode(FxmlFile.FOLDER_OPTIONS);
  }

  @Provides
  static Map<OptionsView.Name, OptionsView> provideOptionViewsByNameMap(
    @FxmlParent(FxmlFile.FILE_TYPE_OPTIONS) Lazy<Parent> fileTypeOptionsView,
    @FxmlParent(FxmlFile.FOLDER_OPTIONS) Lazy<Parent> folderOptionsView,
    Map<Class<? extends FxController>, Provider<FxController>> controllerFactoriesByClassMap
  ) {
    return Map.of(
      OptionsView.Name.FILE_TYPES_VIEW, new OptionsView(
        fileTypeOptionsView.get(),
        (OptionsController) controllerFactoriesByClassMap.get(FileTypeOptionsController.class).get(),
        OptionsView.Name.FILE_TYPES_VIEW
      ),
      OptionsView.Name.FOLDER_VIEW, new OptionsView(
        folderOptionsView.get(),
        (OptionsController) controllerFactoriesByClassMap.get(FolderOptionsController.class).get(),
        OptionsView.Name.FOLDER_VIEW
      )
    );
  }

  @Provides
  static OkCancelButtonHandler provideOkCancelButtonHandler() {
    return new OkCancelButtonHandler();
  }
}
