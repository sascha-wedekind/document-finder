package com.bytedompteur.documentfinder.ui.optionswindow.dagger;

import com.bytedompteur.documentfinder.settings.adapter.in.SettingsService;
import com.bytedompteur.documentfinder.settings.dagger.SettingsModule;
import com.bytedompteur.documentfinder.ui.FxmlFile;
import com.bytedompteur.documentfinder.ui.dagger.FxmlParent;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Lazy;
import javafx.scene.Parent;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Component(modules = {OptionsWindowModule.class, SettingsModule.class})
@OptionsWindowScope
public interface OptionsWindowComponent {

  @FxmlParent(FxmlFile.OPTIONS_VIEW)
  Lazy<Parent> optionsViewNode();

  @FxmlParent(FxmlFile.FILE_TYPE_OPTIONS)
  Lazy<Parent> fileTypeOptionsView();

  @FxmlParent(FxmlFile.FOLDER_OPTIONS)
  Lazy<Parent> folderOptionsView();

  SettingsService settingsService();

  @Component.Builder
  interface Builder {
    @BindsInstance
    OptionsWindowComponent.Builder applicationHomeDirectory(@Named("applicationHomeDirectory") String value);

    OptionsWindowComponent build();
  }
}
