/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logging

public class Loggy {

    static LogLevel level = resolveLogLevel()

    private static LogLevel resolveLogLevel() {
        String prop = System.getProperty("lefthook.log.level")
        if (prop) {
            try {
                return LogLevel.valueOf(prop.toUpperCase())
            } catch (Exception e) {
                // ignore
            }
        }
        if (Boolean.getBoolean("lefthook.debug")) {
            return LogLevel.DEBUG
        }
        return LogLevel.LIFECYCLE
    }

    private static void log(Object caller, LogLevel msgLevel, String msg, Object... params) {
        def logger = Logging.getLogger(caller instanceof Class ? (Class) caller : (caller != null ? caller.getClass() : Loggy.class))
        if (logger.isEnabled(msgLevel)) {
            params = params ?: []
            logger.log(msgLevel, msg, *params)
        } else if (Loggy.level.compareTo(msgLevel) <= 0) {
            params = params ?: []
            String prefix = "[${msgLevel.name()}] "
            logger.lifecycle(prefix + msg, *params)
        }
    }

    // Overloaded with caller parameter for zero-overhead logging
    public static info(Object caller, String msg, Object... params) {
        log(caller, LogLevel.INFO, msg, params)
    }

    public static lifecycle(Object caller, String msg, Object... params) {
        log(caller, LogLevel.LIFECYCLE, msg, params)
    }

    public static debug(Object caller, String msg, Object... params) {
        log(caller, LogLevel.DEBUG, msg, params)
    }

    public static error(Object caller, String msg, Object... params) {
        log(caller, LogLevel.ERROR, msg, params)
    }

    public static warn(Object caller, String msg, Object... params) {
        log(caller, LogLevel.WARN, msg, params)
    }

    // Legacy signatures without caller parameter (fallback to Loggy.class, zero-reflection)
    public static info(String msg, Object... params) {
        log(null, LogLevel.INFO, msg, params)
    }

    public static lifecycle(String msg, Object... params) {
        log(null, LogLevel.LIFECYCLE, msg, params)
    }

    public static debug(String msg, Object... params) {
        log(null, LogLevel.DEBUG, msg, params)
    }

    public static error(String msg, Object... params) {
        log(null, LogLevel.ERROR, msg, params)
    }

    public static warn(String msg, Object... params) {
        log(null, LogLevel.WARN, msg, params)
    }

    // Legacy signatures without params
    public static info(String msg) {
        log(null, LogLevel.INFO, msg)
    }

    public static lifecycle(String msg) {
        log(null, LogLevel.LIFECYCLE, msg)
    }

    public static debug(String msg) {
        log(null, LogLevel.DEBUG, msg)
    }

    public static error(String msg) {
        log(null, LogLevel.ERROR, msg)
    }

    public static warn(String msg) {
        log(null, LogLevel.WARN, msg)
    }

    static def wrap(Closure closure) {
        def caller = closure.getThisObject()
        def entryLog = { args ->
            Loggy.debug(caller, "Entry : {}", args)
            return args
        }
        def exitLog = { args ->
            Loggy.debug(caller, "Exit : {}", args != null ? args : "null")
            return args
        }
        return entryLog >> closure >> exitLog
    }

    static def wrap(Closure closure, String id) {
        def caller = closure.getThisObject()
        def entryLog = { args ->
            Loggy.debug(caller, "{} Entry : {}", id, args)
            return args
        }
        def exitLog = { args ->
            Loggy.debug(caller, "{} Exit : {}", id, args ? args : "null")
            return args
        }
        return entryLog >> closure >> exitLog
    }

}
