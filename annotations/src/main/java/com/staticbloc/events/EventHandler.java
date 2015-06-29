package com.staticbloc.events;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface EventHandler {
  RunType runType() default RunType.BACKGROUND;
}
