package com.bytedompteur.documentfinder.ui;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KeyboardShortcutsTest {


    @Test
    void findKeyBoardShortcutByKeyEvent_returnsShortcut_whenContainedInProvidedCollectionWhileConstructing() {
        // Arrange
        var keyModifierShiftDown = true;
        var keyCode = KeyCode.M;
        var keyEvent = new KeyEvent(KeyEvent.KEY_PRESSED, "", "any text", keyCode, keyModifierShiftDown, false, false, false);
        var keyCodeCombination = new KeyCodeCombination(KeyCode.M, KeyCombination.SHIFT_DOWN);
        var shortcut = new KeyboardShortcut(keyCodeCombination, it -> {}, "some description");
        var sut = new KeyboardShortcuts(List.of(shortcut));

        // Act
        var result = sut.findKeyBoardShortcutByKeyEvent(keyEvent);

        // Assert
        assertThat(result).contains(shortcut);
    }
}