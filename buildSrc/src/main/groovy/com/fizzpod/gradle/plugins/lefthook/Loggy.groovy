package com.fizzpod.gradle.plugins.lefthook

import org.gradle.api.logging.Logging
import org.gradle.api.logging.LogLevel

import org.codehaus.groovy.reflection.ReflectionUtils

public class Loggy {

    static LogLevel level = LogLevel.LIFECYCLE 

    public static log(LogLevel level, String msg, Object... params) {
        def callingClass = getCallingClass()
        def logger = Logging.getLogger(callingClass)
        if(Loggy.level.compareTo(level) <=0 || logger.isEnabled(level)) {
            params = params? params: []
            logger.lifecycle(msg, *params)
        }
    }

    public static log(LogLevel level, String msg) {
        def callingClass = getCallingClass()
        def logger = Logging.getLogger(callingClass)
        if(Loggy.level.compareTo(level) <=0 || logger.isEnabled(level) ) {
            logger.lifecycle(msg)
        }
    }

    private static Class<?> getCallingClass() {
        int stackDepth = 0
        Class<?> clazz = null
        do {
            clazz =  ReflectionUtils.getCallingClass(++stackDepth)
        } while (clazz.getName().contains("Loggy") && stackDepth < 10)
        return clazz
    }

    public static info(String msg) {
        log(LogLevel.INFO, msg)
    }
    
    public static info(String msg, Object... params) {
        log(LogLevel.INFO, msg, *params)
    }

    public static lifecycle(String msg) {
        log(LogLevel.LIFECYCLE, msg)
    }

    public static lifecycle(String msg, Object... params) {
        log(LogLevel.LIFECYCLE, msg, *params)
    }

    public static debug(String msg) {
        log(LogLevel.DEBUG, msg)
    }

    public static debug(String msg, Object... params) {
        log(LogLevel.DEBUG, msg, *params)
    }

    public static error(String msg) {
        log(LogLevel.ERROR, msg)
    }

    public static error(String msg, Object... params) {
        log(LogLevel.ERROR, msg, *params)
    }

    public static warn(String msg) {
        log(LogLevel.WARN, msg)
    }

    public static warn(String msg, Object... params) {
        log(LogLevel.WARN, msg, *params)
    }

    static def wrap(Closure closure) {
        def entryLog = { args ->
            def callingClass = getCallingClass()
            Loggy.debug("{} Entry : {}", callingClass, args)
            return args
        }
        def exitLog = { args ->
            def callingClass = getCallingClass()
            Loggy.debug("{} Exit : {}", callingClass, args != null? args: "null")
            return args
        }
        return entryLog >> closure >> exitLog
    }

    static def wrap(Closure closure, String id) {
        def entryLog = { args ->
            Loggy.debug("{} Entry : {}", id, args)
            return args
        }
        def exitLog = { args ->
            Loggy.debug("{} Exit : {}", id, args? args: "null")
            return args
        }
        return entryLog >> closure >> exitLog
    }

}
