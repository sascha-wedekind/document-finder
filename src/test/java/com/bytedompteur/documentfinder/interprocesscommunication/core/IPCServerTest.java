package com.bytedompteur.documentfinder.interprocesscommunication.core;

import com.google.common.base.Verify;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class IPCServerTest {

  private IPCServerImpl sut;

  private static ThreadPoolExecutor EXECUTOR_SERVICE;

  TestRequestListener requestListener;
  private SocketAddressService socketAddressFactory;

  @BeforeAll
  static void beforeAll() {
    EXECUTOR_SERVICE = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
  }

  @AfterAll
  static void afterAll() {
    EXECUTOR_SERVICE.shutdown();
  }

  @BeforeEach
  void setUp() throws IOException {
    var applicationHomeDirectory = Files.createTempDirectory("document-finder-test");
    socketAddressFactory = new SocketAddressService(applicationHomeDirectory.toString());
    requestListener = new TestRequestListener();
    sut = new IPCServerImpl(
      socketAddressFactory,
      EXECUTOR_SERVICE,
      requestListener
    );
  }

  @AfterEach
  void tearDown() throws IOException {
    sut.closeSocketChannelAndResetFlags();
    Files.deleteIfExists(socketAddressFactory.getSocketAddressFile().getParent());
  }

  @Test
  void start_createsASocketChannelAndRegisterListenerLogicOnExecutorService() throws InterruptedException {
    // Act
    sut.start();
    while (!sut.isRunning()) {
      TimeUnit.MILLISECONDS.sleep(100);
    }

    // Assert
    var socketFile = socketAddressFactory.getSocketAddressFile();
    assertThat(socketFile).existsNoFollowLinks();
    assertThat(sut.isRunning()).isTrue();
    assertThat(EXECUTOR_SERVICE.getActiveCount()).isEqualTo(1);
  }

  @Test
  void stop_closesSocketAndUnregisterFromExecutorService() throws InterruptedException {
    // Arrange
    sut.start();
    TimeUnit.SECONDS.sleep(1);

    // Act
    sut.stop();
    while (sut.isRunning()) {
      TimeUnit.MILLISECONDS.sleep(100);
    }

    // Assert
    var socketFile = socketAddressFactory.getSocketAddressFile();
    assertThat(socketFile).doesNotExist();
    assertThat(sut.isRunning()).isFalse();
    assertThat(EXECUTOR_SERVICE.getActiveCount()).isZero();
  }

  /*
   * Parameterized could not be used, because the test shall not be teared down between multiple parameters
   */
  @Test
  void server_passesMultipleMessagesToTheListener_whenTheServerIsStarted() throws IOException, InterruptedException {
    // Arrange
    var countDownLatch = new CountDownLatch(2);
    requestListener.setCountDownLatch(countDownLatch);
    sut.start();

    var message = "IPC message number 1";
    var message2 = "IPC message number 2";

    // Act
    sendToServer(message);
    sendToServer(message2);

    // Assert
    countDownLatch.await();
    assertThat(requestListener.getMessagesReceived()).containsExactly(message, message2);
  }

  private void sendToServer(String message) throws IOException {
    try (var clientChannel = SocketChannel.open(StandardProtocolFamily.UNIX)) {
      clientChannel.connect(socketAddressFactory.createSocketAddress());

      ByteBuffer buffer = ByteBuffer.allocate(1024);
      buffer.clear();
      buffer.put(message.getBytes(StandardCharsets.UTF_8));
      buffer.flip();

      while (buffer.hasRemaining()) {
        clientChannel.write(buffer);
      }
    }
  }
  private static class TestRequestListener implements RequestListener {


    private CountDownLatch countDownLatch;
    private final CopyOnWriteArrayList<String> messagesReceived = new CopyOnWriteArrayList<>();

    @Override
    public void process(CharSequence requestPayload) {
      Verify.verifyNotNull(countDownLatch, "Count down latch not set! But it is required for the test.");
      messagesReceived.add(requestPayload.toString());
      countDownLatch.countDown();
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
      this.countDownLatch = countDownLatch;
    }

    public List<String> getMessagesReceived() {
      return messagesReceived;
    }
  }

}
