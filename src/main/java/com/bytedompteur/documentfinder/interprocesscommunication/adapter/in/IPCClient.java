package com.bytedompteur.documentfinder.interprocesscommunication.adapter.in;

public interface IPCClient {
  void senMessage(CharSequence message) throws IPCServerNotRunningException;
}
