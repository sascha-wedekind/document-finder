package com.bytedompteur.documentfinder.ui.optionswindow;

import com.bytedompteur.documentfinder.settings.adapter.in.Settings;
import lombok.Value;
import lombok.With;


@Value
public class OptionsModificationContext {

  @With
  Settings settings;

  @With
  boolean forceIndexRebuild;
}
