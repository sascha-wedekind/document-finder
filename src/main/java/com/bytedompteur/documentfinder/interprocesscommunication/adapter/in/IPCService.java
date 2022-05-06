package com.bytedompteur.documentfinder.interprocesscommunication.adapter.in;

import com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.messages.IPCMessage;

public interface IPCService {

  void startIPCServer();

  boolean isIPCServerAlreadyRunningInDifferentProcess();

  void sendMessageToServer(IPCMessage message);

}
