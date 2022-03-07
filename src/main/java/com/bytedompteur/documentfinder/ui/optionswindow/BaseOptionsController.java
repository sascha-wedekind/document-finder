package com.bytedompteur.documentfinder.ui.optionswindow;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public abstract class BaseOptionsController implements OptionsController {

  private final OkCancelButtonHandler okCancelButtonHandler;

  @Override
  public Flux<Object> okButtonClicked() {
    return okCancelButtonHandler.okButtonClicked();
  }

  @Override
  public Flux<Object> cancelButtonClicked() {
    return okCancelButtonHandler.cancelButtonClicked();
  }

  @Override
  public void beforeViewHide() {
    okCancelButtonHandler.completeSinksAndReset();
  }

  protected void emitCancelButtonClicked() {
    okCancelButtonHandler.emitCancelButtonClicked();
  }

  protected void emitOkButtonClicked() {
    okCancelButtonHandler.emitOkButtonClicked();
  }
}
