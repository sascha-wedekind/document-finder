package com.bytedompteur.documentfinder.interprocesscommunication.core;

import com.bytedompteur.documentfinder.interprocesscommunication.adapter.in.messages.IPCMessageType;
import com.bytedompteur.documentfinder.interprocesscommunication.adapter.out.UIAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class IPCMessageRequestListener implements RequestListener {

  private final IPCMessageMapper mapper;

  private final UIAdapter uiAdapter;

  @Override
  public void process(CharSequence requestPayload) {
    mapper
      .map(requestPayload)
      .filter(m -> m.getMessageType() == IPCMessageType.SHOW_MAIN_WINDOW)
      .ifPresent(ignore -> uiAdapter.showMainWindow());
  }
}
