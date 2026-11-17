/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LefthookPluginHelper {

    static def resolveClosure = { File lefthookLocation, List stack, Closure closure ->
        def installer = new LefthookScriptInstaller(lefthookLocation, stack)
        closure.delegate = installer
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        return closure.call()
    }

    static def resolveCollection = { File lefthookLocation, List stack, Collection source ->
        return source.collect { item ->
            if (item instanceof Closure) {
                item = resolveClosure(lefthookLocation, stack, item)
            }
            if (item instanceof Map) {
                return LefthookPluginHelper.resolve(lefthookLocation, stack, item)
            } else if (item instanceof Collection) {
                return resolveCollection(lefthookLocation, stack, item)
            }
            return item
        }
    }

    static def resolve = {File lefthookLocation, List stack, Map source ->
        
        Loggy.debug(LefthookPluginHelper, "resolve {}, {}", stack, source)
        def result = source.inject([:]) { map, key, value ->
            Loggy.debug(LefthookPluginHelper, "inject {}, {}, {}", map, key, value)
            if(key instanceof Closure) {
                key = resolveClosure(lefthookLocation, stack, key)
                Loggy.debug(LefthookPluginHelper, "key closure resolved to {}", key)
            }
            if(value instanceof Closure) {
                value = resolveClosure(lefthookLocation, stack, value)
                Loggy.debug(LefthookPluginHelper, "value closure resolved to {}", value)
            }
            def localStack = stack + key
            Loggy.debug(LefthookPluginHelper, "Stack {}", localStack)
            if (value instanceof Map) {
                Loggy.debug(LefthookPluginHelper, "value {} is map, resolving", value)
                def resolvedValue = LefthookPluginHelper.resolve(lefthookLocation, localStack, value)
                Loggy.debug(LefthookPluginHelper, "value {} resolved to {}, assigning to {}", value, resolvedValue, key)
                map[key] = resolvedValue
            } else if (value instanceof Collection) {
                map[key] = resolveCollection(lefthookLocation, localStack, value)
            } else if(value != null) {
                map[key] = value
            }
            return map
        }
        Loggy.debug(LefthookPluginHelper, "config resolved to {}", result)
        return result
    }
}
