package com.bytedompteur.documentfinder.ui.dagger;

import com.bytedompteur.documentfinder.ui.FxController;
import dagger.MapKey;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target(METHOD)
@Retention(RUNTIME)
@MapKey
public @interface FxControllerMapKey {

	Class<? extends FxController> value();
}
