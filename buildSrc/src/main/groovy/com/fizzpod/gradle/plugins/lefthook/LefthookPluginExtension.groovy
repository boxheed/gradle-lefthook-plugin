/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

public class LefthookPluginExtension implements GroovyInterceptable {
    static def DEFAULT_OPTIONS = [
        "version": "latest",
        "location": ".lefthook",
        "repository": "evilmartians/lefthook",
        "prefix": "v",
        "os": null,
        "arch": null,
        "project": null,
        "autoInstall": true,
        "rc": {""}
    ]

    def config = [:]
    def options = [:]

    void setProperty(String key, value) {
        Loggy.debug("setProperty: {}, {}", key, value)
        config[key] = value
    }
   
    def getProperty(String key) {
        Loggy.debug("getProperty: {}", key)
        if(key.equalsIgnoreCase("config")) {
            return this.config
        } else {
            return this.config[key]
        }
    } 

    def invokeMethod(String key, args) {
        Loggy.debug("invokeMethod: {}, {}", key, args)
        throw new RuntimeException(key)
        config[key] = args
    }
}
