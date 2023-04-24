package com.bytedompteur.documentfinder.ui.optionswindow;

import javafx.scene.layout.VBox;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;

import static java.util.Objects.requireNonNullElse;

public class MockedOptionsViewHelper extends OptionsViewHelper {

  private final OkCancelButtonHandler okCancelButtonHandler = new OkCancelButtonHandler();
  private OptionsModificationContext context;

  public MockedOptionsViewHelper(Name name) {
    super(new VBox(), name, Mockito.mock(OptionsController.class));
  }

  @Override
  Flux<Object> cancelButtonClicked() {
    return okCancelButtonHandler.cancelButtonClicked();
  }

  @Override
  Flux<Object> okButtonClicked() {
    return okCancelButtonHandler.okButtonClicked();
  }

  @Override
  void insertModificationContextInController(OptionsModificationContext context) {
    this.context = context;
  }

  @Override
  OptionsModificationContext extractModificationContextFromController(OptionsModificationContext context) {
    return requireNonNullElse(this.context, context);
  }

  public OkCancelButtonHandler getOkCancelButtonHandler() {
    return okCancelButtonHandler;
  }
}
