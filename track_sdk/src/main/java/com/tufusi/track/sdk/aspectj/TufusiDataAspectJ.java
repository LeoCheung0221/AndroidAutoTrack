package com.tufusi.track.sdk.aspectj;

import android.util.Log;

import androidx.annotation.Keep;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Created by LeoCheung on 2020/11/27.
 *
 * @description
 */
@Aspect
@Keep
@SuppressWarnings("all")
public class TufusiDataAspectJ {

    private static final String TAG = "TufusiDataAspectJ";

    @Around("execution(* *(..))")
    public Object weaveAllMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        //纳秒，1毫秒=1纳秒*1000*1000
        long startMethodTime = System.currentTimeMillis();
        Object returnObj = joinPoint.proceed();
        long endMethodTime = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        long diffTime = endMethodTime - startMethodTime;
        if (diffTime > 500) {
            Log.i(TAG, String.format(
                    Locale.CHINA, "Method:<%s> cost=%s ns",
                    method.toGenericString(),
                    String.valueOf(diffTime)));
        }

        return returnObj;
    }

} 