package com.bytedompteur.documentfinder.ui.systemtray.dagger;

import com.bytedompteur.documentfinder.ui.systemtray.SystemTrayIconController;
import dagger.Lazy;
import dagger.Subcomponent;

@Subcomponent(modules = {SystemTrayModule.class})
@SystemTrayScope
public interface SystemTrayComponent {

  Lazy<SystemTrayIconController> systemTrayIconController();

  @Subcomponent.Builder
  interface Builder {
    SystemTrayComponent build();
  }

}
