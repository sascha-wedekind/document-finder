package com.bytedompteur.documentfinder.ui.dagger;

import com.bytedompteur.documentfinder.commands.ExitApplicationCommand;
import com.bytedompteur.documentfinder.commands.StartDirectoryWatcherCommand;
import com.bytedompteur.documentfinder.commands.StartFulltextSearchServiceCommand;
import com.bytedompteur.documentfinder.commands.StopAllGracefulCommand;
import com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.IPCService;
import com.bytedompteur.documentfinder.interprocesscommunication.dagger.IPCModule;
import com.bytedompteur.documentfinder.ui.WindowManager;
import com.bytedompteur.documentfinder.ui.adapter.out.JavaFxPlatformAdapter;
import dagger.BindsInstance;
import dagger.Component;
import javafx.application.HostServices;
import javafx.stage.Stage;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Component(modules = {UIModule.class, IPCModule.class})
@Singleton
public interface UIComponent {

  WindowManager windowManager();

  ExitApplicationCommand exitApplicationCommand();

  StartDirectoryWatcherCommand startDirectoryWatcherCommand();

  StartFulltextSearchServiceCommand startFulltextSearchServiceCommand();

  StopAllGracefulCommand stopAllGracefulCommand();

  JavaFxPlatformAdapter platformAdapter();

  IPCService ipcService();

  @Component.Builder
  interface Builder {
    @BindsInstance
    UIComponent.Builder applicationHomeDirectory(@Named("applicationHomeDirectory") String value);

    @BindsInstance
    UIComponent.Builder numberOfVirtualThreads(@Named("numberOfVirtualThreads") int value);

    @BindsInstance
    UIComponent.Builder primaryStage(@Named("primaryStage") Stage value);

    @BindsInstance
    UIComponent.Builder hostServices(HostServices hostServices);

    UIComponent build();
  }
}
