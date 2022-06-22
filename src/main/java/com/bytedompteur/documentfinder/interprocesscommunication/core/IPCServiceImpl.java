package com.bytedompteur.documentfinder.interprocesscommunication.core;

import com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.IPCClient;
import com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.IPCServer;
import com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.IPCServerNotRunningException;
import com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.IPCService;
import com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.messages.IPCMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class IPCServiceImpl implements IPCService {

  private final IPCServer server;
  private final IPCClient client;
  private final SocketAddressService socketAddressService;
  private final IPCMessageMapper mapper;

  @Override
  public void startIPCServer() {
    if (!server.isRunning()) {
      server.start();
    }
  }

  @Override
  public boolean isIPCServerAlreadyRunningInDifferentProcess() {
    return socketAddressService.socketAddressFileExists();
  }

  @Override
  public void sendMessageToServer(IPCMessage message) throws IPCServerNotRunningException {
    client.senMessage(mapper.map(message));
  }
}
