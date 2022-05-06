package com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.messages;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class UnknownMessage implements IPCMessage {
  @Override
  public IPCMessageType getMessageType() {
    return IPCMessageType.UNKNOWN;
  }
}
