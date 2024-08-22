package com.bytedompteur.documentfinder.interprocesscommunication.core;

import com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.IPCConnectionException;
import com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.IPCServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;

import jakarta.inject.Inject;
import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.UnsupportedAddressTypeException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class IPCServerImpl implements IPCServer {

  private final SocketAddressService addressService;

  private final ExecutorService executorService;

  private final RequestListener requestListener;

  private ServerSocketChannel serverSocketChannel;

  private final AtomicBoolean starting = new AtomicBoolean(false);

  private final AtomicBoolean started = new AtomicBoolean(false);
  private final AtomicBoolean shouldStop = new AtomicBoolean(false);

  @Override
  public void start() throws IPCConnectionException {
    Validate.notNull(requestListener, "'requestListener' required");
    Validate.notNull(executorService, "'executorService' required");

    if (starting.compareAndSet(false, true)) {
      log.info("Starting IPC server");
      createServerSocketChannel();
      listenOnServerSocketChannel();
    } else {
      log.info("IPC server is already started");
    }
  }

  @Override
  public void stop() {
    if (started.get() && shouldStop.compareAndSet(false, true)) {
      try {
        serverSocketChannel.close();
      } catch (IOException e) {
        log.error("While closing server socket channel", e);
        throw new IPCConnectionException(e);
      }
    }
  }

  @Override
  public boolean isRunning() {
    return started.get();
  }

  protected void listenOnServerSocketChannel() {
    executorService.submit(() -> {
      starting.compareAndSet(true, false);
      started.compareAndSet(false, true);
      while (!shouldStop.get() && serverSocketChannel.isOpen()) {
        acceptRequestAndGetPayload(serverSocketChannel).ifPresent(requestListener::process);
      }
      closeSocketChannelAndResetFlags();
    });
  }

  protected Optional<CharSequence> acceptRequestAndGetPayload(ServerSocketChannel serverSocketChannel) {
    Optional<CharSequence> result = Optional.empty();
    try {
      var socketChannel = serverSocketChannel.accept();
      var byteBuffer = ByteBuffer.allocate(1024);
      var bytesRead = socketChannel.read(byteBuffer);
      if (bytesRead > 0) {
        byte[] rawPayload = new byte[bytesRead];
        byteBuffer.flip();
        byteBuffer.get(rawPayload);
        result = Optional.of(new String(rawPayload, StandardCharsets.UTF_8));
      }
    } catch (IOException e) {
      log.error("Failed to accept request", e);
    }
    return result;
  }

  @SuppressWarnings("java:S2095")
  protected void createServerSocketChannel() {
    try {
      addressService.deleteSocketAddressFileIfExists(); // Maybe the file was not delete because, the application was stopped with SIGKILL
      log.info("Spawning IPC server listening on socket {}", addressService.getSocketAddressFile());
      serverSocketChannel = ServerSocketChannel
        .open(StandardProtocolFamily.UNIX)
        .bind(addressService.createSocketAddress());
    } catch (IOException | AlreadyBoundException | UnsupportedAddressTypeException e) {
      closeSocketChannelAndResetFlags();
      throw new IPCConnectionException(e);
    }
  }

  protected void closeSocketChannelAndResetFlags() {
    log.info("Stopping IPC server");
    try {
      if (serverSocketChannel != null) {
        serverSocketChannel.close();
      }
      addressService.deleteSocketAddressFileIfExists();
    } catch (IOException e) {
      log.error("While closing IPC socket channel");
    } finally {
      shouldStop.set(false);
      started.set(false);
    }
    log.info("IPC server stopped");
  }
}
