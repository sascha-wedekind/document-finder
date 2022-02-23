package com.bytedompteur.documentfinder.storybook.optionswindow;


import com.bytedompteur.documentfinder.ui.optionswindow.dagger.OptionsWindowComponent;
import com.bytedompteur.documentfinder.ui.optionswindow.dagger.OptionsWindowScope;
import dagger.Component;

import javax.inject.Singleton;

@Component(modules = OptionsWindowStorybookModule.class)
@Singleton
@OptionsWindowScope
public interface OptionsWindowStorybookComponent extends OptionsWindowComponent {


}
