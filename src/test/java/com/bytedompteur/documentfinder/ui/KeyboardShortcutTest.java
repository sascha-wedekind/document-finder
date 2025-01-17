package com.bytedompteur.documentfinder.ui;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KeyboardShortcutTest {

    @Mock
    Consumer mockedConsumer;

    @Mock
    WindowManager mockedWindowManager;

    private KeyCodeCombination keyCodeCombination;
    private KeyboardShortcut sut;

    @BeforeEach
    void setUp() {
        keyCodeCombination = new KeyCodeCombination(KeyCode.M, KeyCombination.SHIFT_DOWN);
        sut = new KeyboardShortcut(keyCodeCombination, mockedConsumer, "some description");
    }

    @Test
    void getDisplayText_returnsKeyCodeDisplayText_whenNotNull() {
        // Assert
        assertThat(sut.getDisplayText()).isEqualTo(keyCodeCombination.getDisplayText());
    }

    @Test
    void match_returnsTrue_whenKeyModifierAndKeyCodeEquals() {
        // Arrange
        var keyModifierShiftDown = true;
        var keyCode = KeyCode.M;
        var keyEvent = new KeyEvent(KeyEvent.KEY_PRESSED, "", "any text", keyCode, keyModifierShiftDown, false, false, false);

        // Act
        var result = sut.match(keyEvent);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void match_returnsFalse_whenKeyModifierAndKeyCodeEquals() {
        // Arrange
        var keyModifierShiftDown = true;
        var keyCode = KeyCode.X;
        var keyEvent = new KeyEvent(KeyEvent.KEY_PRESSED, "", "any text", keyCode, keyModifierShiftDown, false, false, false);

        // Act
        var result = sut.match(keyEvent);

        // Assert
        assertThat(result).isFalse();
    }

    @SuppressWarnings("unchecked")
    @Test
    void name() {
        // Act
        sut.executeAction(mockedWindowManager);

        // Assert
        verify(mockedConsumer).accept(mockedWindowManager);
    }

    @Test
    void createKeyboardShortcut_throwsNPE_whenKeyCombinationIsNull() {
        // Act
        assertThrows(NullPointerException.class, () -> new KeyboardShortcut(null, mockedConsumer, "some description"));
    }

    @Test
    void createKeyboardShortcut_throwsNPE_whenConsumerIsNull() {
        // Act
        assertThrows(NullPointerException.class, () -> new KeyboardShortcut(keyCodeCombination, null, "some description"));
    }


}