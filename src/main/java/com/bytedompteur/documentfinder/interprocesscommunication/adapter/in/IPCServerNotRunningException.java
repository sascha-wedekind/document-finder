package com.bytedompteur.documentfinder.interprocesscommunication.adapter.in;

public class IPCServerNotRunningException extends RuntimeException {
  public IPCServerNotRunningException() {
    super("IPC server is not running");
  }
}
