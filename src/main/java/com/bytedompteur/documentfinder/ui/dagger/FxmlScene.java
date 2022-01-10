package com.bytedompteur.documentfinder.ui.dagger;

import com.bytedompteur.documentfinder.ui.FxmlFile;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Qualifier
@Documented
@Retention(RUNTIME)
public @interface FxmlScene {

	FxmlFile value();

}
