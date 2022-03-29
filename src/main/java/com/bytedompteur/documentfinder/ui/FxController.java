package com.bytedompteur.documentfinder.ui;

/**
 * Marker interface
 */
public interface FxController {

  default void beforeViewHide(){
    // IGNORE
  };

  default void afterViewShown() {
    // IGNORE
  }
}
