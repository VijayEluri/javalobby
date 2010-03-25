package com.javalobby.tnt.annotation;

import java.lang.annotation.*;


@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.PACKAGE, ElementType.FIELD})
public @interface Note {
    String value();
    Priority priority() default Priority.MEDIUM;
}
