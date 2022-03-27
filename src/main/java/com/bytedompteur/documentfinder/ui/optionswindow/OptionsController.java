package com.bytedompteur.documentfinder.ui.optionswindow;

import com.bytedompteur.documentfinder.ui.FxController;
import reactor.core.publisher.Flux;

/**
 * Marker interface
 */
public interface OptionsController extends FxController {

  Flux<Object> okButtonClicked();

  Flux<Object> cancelButtonClicked();

}
