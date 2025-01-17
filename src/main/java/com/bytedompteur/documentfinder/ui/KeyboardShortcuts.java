package com.bytedompteur.documentfinder.ui;

import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class KeyboardShortcuts {

    private final List<KeyboardShortcut> keyboardShortcuts = new ArrayList<>(5);

    public KeyboardShortcuts(Collection<KeyboardShortcut> keyboardShortcuts) {
        this.keyboardShortcuts.addAll(keyboardShortcuts);
    }

    public Optional<KeyboardShortcut> findKeyBoardShortcutByKeyEvent(KeyEvent keyEvent) {
        return keyboardShortcuts.stream()
                .filter(keyboardShortcut -> keyboardShortcut.match(keyEvent))
                .findFirst();
    }

}
