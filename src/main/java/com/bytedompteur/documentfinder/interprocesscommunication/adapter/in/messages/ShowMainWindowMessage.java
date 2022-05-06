package com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.messages;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@EqualsAndHashCode
public class ShowMainWindowMessage implements IPCMessage {

  @Override
  public IPCMessageType getMessageType() {
    return IPCMessageType.SHOW_MAIN_WINDOW;
  }

}
