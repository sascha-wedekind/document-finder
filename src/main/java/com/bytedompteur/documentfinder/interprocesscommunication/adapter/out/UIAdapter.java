package com.bytedompteur.documentfinder.interprocesscommunication.adapter.out;

import com.bytedompteur.documentfinder.ui.WindowManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class UIAdapter {

  private final WindowManager windowManager;

  public void showMainWindow() {
    windowManager.showMainWindow();
  }
}
