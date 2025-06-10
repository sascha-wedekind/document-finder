package com.bytedompteur.documentfinder.ui.dagger;

import com.bytedompteur.documentfinder.CustomNamePrefixThreadFactoryBuilder;
import com.bytedompteur.documentfinder.PathUtil;
import com.bytedompteur.documentfinder.commands.dagger.CommandsModule;
import com.bytedompteur.documentfinder.searchhistory.dagger.SearchHistoryModule; // Added import
import com.bytedompteur.documentfinder.settings.dagger.SettingsModule;
import com.bytedompteur.documentfinder.ui.KeyboardShortcut;
import com.bytedompteur.documentfinder.ui.KeyboardShortcuts;
import com.bytedompteur.documentfinder.ui.WindowManager;
import com.bytedompteur.documentfinder.ui.adapter.out.JavaFxPlatformAdapter;
import com.bytedompteur.documentfinder.ui.mainwindow.dagger.MainWindowComponent;
import com.bytedompteur.documentfinder.ui.optionswindow.dagger.OptionsWindowComponent;
import com.bytedompteur.documentfinder.ui.systemtray.dagger.SystemTrayComponent;
import dagger.Module;
import dagger.Provides;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.apache.commons.lang3.SystemUtils;
import org.cryptomator.integrations.tray.TrayIntegrationProvider;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Module(
    subcomponents = {MainWindowComponent.class, OptionsWindowComponent.class, SystemTrayComponent.class},
    includes = {SettingsModule.class, CommandsModule.class, SearchHistoryModule.class} // Added SearchHistoryModule
)
public abstract class UIModule {

    @Provides
    static MainWindowComponent provideMainWindowComponent(MainWindowComponent.Builder builder) {
        return builder.build();
    }

    @Provides
    static OptionsWindowComponent provideOptionsWindowComponent(OptionsWindowComponent.Builder builder) {
        return builder.build();
    }

    @Provides
    @Singleton
    public static PathUtil providePathUtil() {
        return new PathUtil();
    }

    @Provides
    @Singleton
    static ExecutorService provideExecutorService(@Named("numberOfVirtualThreads") int numberOfThreads, ThreadFactory threadFactory) {
        return Executors.newFixedThreadPool(Math.max(1, numberOfThreads), threadFactory);
    }

    @Provides
    @Singleton
    static Optional<TrayIntegrationProvider> provideTrayIntegrationProvider() {
        return TrayIntegrationProvider.get();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Provides
    @Singleton
    static WindowManager provideWindowManager(
        MainWindowComponent.Builder mainWindowComponentBuilder,
        OptionsWindowComponent.Builder optionsWindowComponentBuilder,
        SystemTrayComponent.Builder systemTrayComponentBuilder,
        @Named("primaryStage") Stage stage,
        JavaFxPlatformAdapter platformAdapter,
        Optional<TrayIntegrationProvider> trayIntegrationProvider,
        KeyboardShortcuts keyboardShortcuts
    ) {
        return new WindowManager(
            stage,
            mainWindowComponentBuilder,
            optionsWindowComponentBuilder,
            systemTrayComponentBuilder,
            platformAdapter,
            keyboardShortcuts,
            WindowManager.DEFAULT_SCENE_FACTORY,
            trayIntegrationProvider
        );
    }

    @Provides
    @Singleton
    static ThreadFactory provideThreadFactory() {
        return new CustomNamePrefixThreadFactoryBuilder().build();
    }

    @Provides
    @Singleton
    static KeyboardShortcuts provideKeyboardShortcutList() {
        var keyCombinatonsMap = SystemUtils.IS_OS_MAC ? getMacOsKeyCombinations() : getWindowsAndLinuxKeyCombinations();
        var shortcuts = buildKeyboardShortcuts(keyCombinatonsMap);
        return new KeyboardShortcuts(shortcuts);
    }

    private static Map<KeyCombinationType, KeyCombination> getMacOsKeyCombinations() {
        return Map.of(
            KeyCombinationType.SHOW_OPTIONS, new KeyCodeCombination(KeyCode.COMMA, KeyCombination.META_DOWN),
            KeyCombinationType.FIND_LAST_UPDATED_FILES, new KeyCodeCombination(KeyCode.Y, KeyCombination.META_DOWN),
            KeyCombinationType.RESET_SEARCH_RESULTS, new KeyCodeCombination(KeyCode.R, KeyCombination.META_DOWN),
            KeyCombinationType.FOCUS_SEARCH_FIELD, (KeyCombination) new KeyCodeCombination(KeyCode.F, KeyCombination.META_DOWN)
        );
    }

    private static Map<KeyCombinationType, KeyCombination> getWindowsAndLinuxKeyCombinations() {
        return Map.of(
            KeyCombinationType.SHOW_OPTIONS, new KeyCodeCombination(KeyCode.COMMA, KeyCombination.CONTROL_DOWN),
            KeyCombinationType.FIND_LAST_UPDATED_FILES, new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN),
            KeyCombinationType.RESET_SEARCH_RESULTS, new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN),
            KeyCombinationType.FOCUS_SEARCH_FIELD, (KeyCombination) new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN)
        );
    }

    private static List<KeyboardShortcut> buildKeyboardShortcuts(Map<KeyCombinationType, KeyCombination> keyCombinatonsMap) {
        return List.of(
            KeyboardShortcut.builder()
                .keyCombination(keyCombinatonsMap.get(KeyCombinationType.SHOW_OPTIONS))
                .description("Show options")
                .action(WindowManager::showOptionsWindow)
                .build(),

            KeyboardShortcut.builder()
                .keyCombination(keyCombinatonsMap.get(KeyCombinationType.FIND_LAST_UPDATED_FILES))
                .description("Find last updated files")
                .action(it -> it.getMainWindowComponent().mainWindowController().showFilesLastUpdated())
                .build(),

            KeyboardShortcut.builder()
                .keyCombination(keyCombinatonsMap.get(KeyCombinationType.RESET_SEARCH_RESULTS))
                .description("Reset search results view")
                .action(it -> it.getMainWindowComponent().mainWindowController().clearView())
                .build(),

            KeyboardShortcut.builder()
                .keyCombination(keyCombinatonsMap.get(KeyCombinationType.FOCUS_SEARCH_FIELD))
                .description("Focus search field")
                .action(it -> it.getMainWindowComponent().mainWindowController().focusSearchTextField())
                .build()
        );
    }

    private enum KeyCombinationType {
        SHOW_OPTIONS,
        FIND_LAST_UPDATED_FILES,
        RESET_SEARCH_RESULTS,
        FOCUS_SEARCH_FIELD
    }
}
