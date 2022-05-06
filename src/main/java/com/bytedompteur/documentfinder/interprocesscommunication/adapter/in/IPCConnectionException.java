package com.bytedompteur.documentfinder.interprocesscommunication.adapter.in;

public class IPCConnectionException extends RuntimeException {
  public IPCConnectionException(Exception e) {
    super(e);
  }
}
