package com.cd.springbootdemo.mybatisplusdemo.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ParamCheck {

    /**
     *  默认非空
     * @return
     */
    boolean notNull() default true;
}

