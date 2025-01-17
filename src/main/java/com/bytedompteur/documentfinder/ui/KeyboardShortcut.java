package com.bytedompteur.documentfinder.ui;

import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

@RequiredArgsConstructor
@Builder
public class KeyboardShortcut {

    @NonNull
    final KeyCombination keyCombination;

    @NonNull
    final Consumer<WindowManager> action;

    @Getter
    final String description;

    public void executeAction(WindowManager windowManager) {
        action.accept(windowManager);
    }

    public boolean match(KeyEvent ke) {
        return keyCombination.match(ke);
    }

    public String getDisplayText() {
        return keyCombination.getDisplayText();
    }
}
