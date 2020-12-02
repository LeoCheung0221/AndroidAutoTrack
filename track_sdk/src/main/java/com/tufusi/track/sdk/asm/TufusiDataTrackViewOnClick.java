package com.tufusi.track.sdk.asm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by LeoCheung on 2020/12/2.
 *
 * @description
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TufusiDataTrackViewOnClick {
} 