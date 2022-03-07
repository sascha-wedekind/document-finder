package com.bytedompteur.documentfinder.ui.optionswindow;

import javafx.scene.Parent;
import lombok.Value;

@Value
public class OptionsView {

  public enum Name {FILE_TYPES_VIEW,FOLDER_VIEW}

  Parent viewInstance;
  OptionsController controller;
  Name name;



}
