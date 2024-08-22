package com.bytedompteur.documentfinder.interprocesscommunication.core;

import com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.IPCClient;
import com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.IPCServerNotRunningException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.inject.Inject;
import java.net.ConnectException;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class IPCClientImpl implements IPCClient {

  private final SocketAddressService addressService;

  @Override
  public void senMessage(CharSequence message) {
    try (var clientChannel = SocketChannel.open(StandardProtocolFamily.UNIX)) {
      if (clientChannel.connect(addressService.createSocketAddress())) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.clear();
        buffer.put(message.toString().getBytes(StandardCharsets.UTF_8));
        buffer.flip();

        while (buffer.hasRemaining()) {
          clientChannel.write(buffer);
        }
      }
    } catch (ConnectException e) {
      log.warn("Could not connect to IPC server, assuming server is not running. Suggesting caller to create a new Document Finder instance", e);
      throw new IPCServerNotRunningException();

    } catch (Exception e) {
      log.error("Could not send IPC message", e);
    }
  }

}
