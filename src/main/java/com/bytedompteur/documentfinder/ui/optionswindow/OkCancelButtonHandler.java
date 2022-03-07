package com.bytedompteur.documentfinder.ui.optionswindow;

import lombok.NoArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@NoArgsConstructor
public class OkCancelButtonHandler {

  private Sinks.Many<Object> okButtonClickedSink = Sinks.many().unicast().onBackpressureError();
  private Sinks.Many<Object> cancelButtonClickedSink = Sinks.many().unicast().onBackpressureError();

  Flux<Object> okButtonClicked() {
    return okButtonClickedSink.asFlux();
  }

  Flux<Object> cancelButtonClicked() {
    return cancelButtonClickedSink.asFlux();
  }

  void completeSinksAndReset() {
    okButtonClickedSink.tryEmitComplete();
    cancelButtonClickedSink.tryEmitComplete();
    okButtonClickedSink = Sinks.many().unicast().onBackpressureError();
    cancelButtonClickedSink = Sinks.many().unicast().onBackpressureError();
  }

  void emitCancelButtonClicked() {
    cancelButtonClickedSink.tryEmitNext("");
  }

  void emitOkButtonClicked() {
    okButtonClickedSink.tryEmitNext("");
  }
}
