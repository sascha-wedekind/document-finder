package com.bytedompteur.documentfinder.interprocesscommunication.core;

import com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.messages.IPCMessage;
import com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.messages.IPCMessageType;
import com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.messages.ShowMainWindowMessage;
import com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.messages.UnknownMessage;
import com.google.gson.Gson;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@NoArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class IPCMessageMapper {

  @SuppressWarnings("RegExpEmptyAlternationBranch")
  public static final String MESSAGE_HEADER_PAYLOAD_DELIMITER = "|";

  private final Gson gson = new Gson();

  private final Map<IPCMessageType, Function<String, IPCMessage>> messageConverter = Map.of(
    IPCMessageType.SHOW_MAIN_WINDOW, payload -> gson.fromJson(payload, ShowMainWindowMessage.class),
    IPCMessageType.UNKNOWN, ignore -> new UnknownMessage()
  );

  public Optional<IPCMessage> map(CharSequence requestPayload) {
    log.debug("Trying to parse '{}'", requestPayload);
    return validatePayloadAndParse(requestPayload);
  }

  public CharSequence map(IPCMessage message) {
    Validate.notNull(message, "'message' must not be null");
    return String.format(
      "%s%s%s",
      message.getMessageType().toString(),
      MESSAGE_HEADER_PAYLOAD_DELIMITER,
      gson.toJson(message)
    );
  }

  protected Optional<IPCMessage> validatePayloadAndParse(CharSequence requestPayload) {
    Optional<IPCMessage> ipcMessage = Optional.of(new UnknownMessage());

    if (!StringUtils.isBlank(requestPayload)) {
      var messageParts = requestPayload.toString().split("\\" + MESSAGE_HEADER_PAYLOAD_DELIMITER);
      if (messageParts.length == 2) {
        ipcMessage = parseValidatedMessage(messageParts);
      }
    } else {
      log.warn("Request payload is empty. Ignoring message");
    }

    return ipcMessage;
  }

  private Optional<IPCMessage> parseValidatedMessage(String[] messageParts) {
    Optional<IPCMessage> ipcMessage = Optional.of(new UnknownMessage());
    var messageTypeStr = messageParts[0];
    var payload = messageParts[1];
    var type = mapToIpcMessageType(messageTypeStr).orElse(IPCMessageType.UNKNOWN);
    if (IPCMessageType.UNKNOWN != type) {
      try {
        ipcMessage = Optional.of(messageConverter.get(type).apply(payload));
        log.debug("Parsed message data '{}' for message type '{}' to {}", payload, messageTypeStr, ipcMessage.get());
      } catch (Exception e) {
        log.error("Could not parse message data '{}' for message type '{}'", payload, messageTypeStr,e);
      }
    } else {
      log.warn("Request payload does not have the form '[MESSAGE_TYPE]" + MESSAGE_HEADER_PAYLOAD_DELIMITER + "[MESSAGE_DATA]'. Ignoring message");
    }
    return ipcMessage;
  }

  private Optional<IPCMessageType> mapToIpcMessageType(String messageType) {
    Optional<IPCMessageType> result = Optional.empty();
    if (StringUtils.isNotBlank(messageType)) {
      try {
        result = Optional.of(IPCMessageType.valueOf(messageType));
      } catch (IllegalArgumentException e) {
        log.warn("Request message type '{}' not supported. Ignoring message", messageType);
      }
    } else {
      log.warn("Request message type prefix is empty. Ignoring message");
    }
    return result;
  }

}
