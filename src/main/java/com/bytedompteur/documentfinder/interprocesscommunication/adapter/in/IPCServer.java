package com.bytedompteur.documentfinder.interprocesscommunication.adapter.in;

public interface IPCServer {
  void start() throws IPCConnectionException;

  void stop();

  boolean isRunning();
}
