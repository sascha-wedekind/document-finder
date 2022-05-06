package com.bytedompteur.documentfinder.interprocesscommunication.core;

import com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.IPCClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
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
    } catch (Exception e) {
      log.error("Could not send IPC message", e);
    }
  }

}
