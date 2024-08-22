package com.bytedompteur.documentfinder.ui.optionswindow.dagger;

import com.bytedompteur.documentfinder.ui.FxController;
import com.bytedompteur.documentfinder.ui.FxmlFile;
import com.bytedompteur.documentfinder.ui.dagger.FxmlLoaderFactory;
import com.bytedompteur.documentfinder.ui.dagger.FxmlParent;
import com.bytedompteur.documentfinder.ui.optionswindow.*;
import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import jakarta.inject.Provider;
import javafx.scene.Parent;

import java.util.Map;

@Module
public abstract class OptionsWindowModule {

  // Moved from @Binds to @Provides because of a bug in Dagger 2.52
  @Provides
  static Map<Class<? extends FxController>, Provider<FxController>> controllerFactoriesByClassMap(
    OptionsWindowController value1,
    FileTypeOptionsController value2,
    FolderOptionsController value3,
    AboutController value4,
    GeneralOptionsController value5
  ) {

    return Map.of(
      OptionsWindowController.class, () -> value1,
      FileTypeOptionsController.class, () -> value2,
      FolderOptionsController.class, () -> value3,
      AboutController.class, () -> value4,
      GeneralOptionsController.class, () -> value5
    );
  }

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
  @FxmlParent(FxmlFile.ABOUT)
  static Parent provideAbout(FxmlLoaderFactory factory) {
    return factory.createParentNode(FxmlFile.ABOUT);
  }

  @Provides
  @FxmlParent(FxmlFile.GENERAL_OPTIONS)
  static Parent provideGeneralOptions(FxmlLoaderFactory factory) {
    return factory.createParentNode(FxmlFile.GENERAL_OPTIONS);
  }

  @Provides
  static Map<OptionsViewHelper.Name, OptionsViewHelper> provideOptionViewsByNameMap(
    @FxmlParent(FxmlFile.FILE_TYPE_OPTIONS) Lazy<Parent> fileTypeOptionsView,
    @FxmlParent(FxmlFile.FOLDER_OPTIONS) Lazy<Parent> folderOptionsView,
    @FxmlParent(FxmlFile.ABOUT) Lazy<Parent> aboutView,
    @FxmlParent(FxmlFile.GENERAL_OPTIONS) Lazy<Parent> generalOptionsView,
    Map<Class<? extends FxController>, Provider<FxController>> controllerFactoriesByClassMap
  ) {
    return Map.of(
      OptionsViewHelper.Name.FILE_TYPES_VIEW, new FileTypeOptionsViewHelper(
        fileTypeOptionsView.get(),
        (FileTypeOptionsController) controllerFactoriesByClassMap.get(FileTypeOptionsController.class).get()
      ),
      OptionsViewHelper.Name.FOLDER_VIEW, new FolderOptionsViewHelper(
        folderOptionsView.get(),
        (FolderOptionsController) controllerFactoriesByClassMap.get(FolderOptionsController.class).get()
      ),
      OptionsViewHelper.Name.ABOUT_VIEW, new AboutViewHelper(
        aboutView.get(),
        (AboutController) controllerFactoriesByClassMap.get(AboutController.class).get()
      ),
      OptionsViewHelper.Name.GENERAL_OPTIONS_VIEW, new GeneralOptionsViewHelper(
        generalOptionsView.get(),
        (GeneralOptionsController) controllerFactoriesByClassMap.get(GeneralOptionsController.class).get()
      )
    );
  }

  @Provides
  static OkCancelButtonHandler provideOkCancelButtonHandler() {
    return new OkCancelButtonHandler();
  }
}
