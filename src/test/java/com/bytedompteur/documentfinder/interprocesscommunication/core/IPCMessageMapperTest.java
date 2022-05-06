package com.bytedompteur.documentfinder.interprocesscommunication.core;

import com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.messages.IPCMessageType;
import com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.messages.ShowMainWindowMessage;
import com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.messages.UnknownMessage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IPCMessageMapperTest {

  private final IPCMessageMapper sut = new IPCMessageMapper();

  @Test
  void validatePayloadAndParse_returnsUnknownMessage_whenMessageHeaderPayloadDelimiterNotContained() {
    // Act
    var result = sut.validatePayloadAndParse("Lorem ipsum dolor sit amet");

    // Assert
    assertThat(result).contains(new UnknownMessage());
  }

  @Test
  void validatePayloadAndParse_returnsUnknownMessage_whenMessageHeaderIsUnknown() {
    // Act
    var result = sut.validatePayloadAndParse("SOME_HEADER|");

    // Assert
    assertThat(result).contains(new UnknownMessage());
  }

  @Test
  void validatePayloadAndParse_returnsUnknownMessage_whenMessageHeaderIsEmpty() {
    // Act
    var result = sut.validatePayloadAndParse("|PAYLOAD");

    // Assert
    assertThat(result).contains(new UnknownMessage());
  }

  @Test
  void validatePayloadAndParse_returnsUnknownMessage_whenPayloadHeaderIsEmpty() {
    // Act
    var result = sut.validatePayloadAndParse(IPCMessageType.SHOW_MAIN_WINDOW + "|");

    // Assert
    assertThat(result).contains(new UnknownMessage());
  }

  @Test
  void validatePayloadAndParse_returnsShowMainWindowMessage() {
    // Act - SHOW_MAIN_WINDOW|{}
    var result = sut.validatePayloadAndParse( IPCMessageType.SHOW_MAIN_WINDOW + "|{}");

    // Assert
    assertThat(result).contains(new ShowMainWindowMessage());
  }

  @Test
  void map_returnsStringRepresentationOfIPCMessage() {
    // Act
    var sequence = sut.map(new ShowMainWindowMessage());

    // Assert
    assertThat(sequence).isEqualTo(IPCMessageType.SHOW_MAIN_WINDOW + "|{}");
  }
}
