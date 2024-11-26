package com.fizzpod.gradle.plugins.lefthook

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.Copy

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class LefthookPluginHelper {

    static def createContext = Loggy.wrap( {Project project ->
        def context = [:]
        context.project = project
        context.extension = LefthookPluginHelper.getOptions(project)
        context.options = context.extension
        context.config = LefthookPluginHelper.getConfig(project)
        return context
    })

    static def getOptions = Loggy.wrap( {Project project ->
        Loggy.debug("Finding options")
        def extension = getExtension(project, "options")
        def config = extension != null? extension.config: [:]
        def options = config["options"] != null? config["options"]: [:] 
        if(options instanceof Closure) {
            def optionsMap = [:]
            options.delegate = optionsMap
            options.call()
            options = optionsMap
        }
        options = merge(options, LefthookPluginExtension.DEFAULT_OPTIONS)
        Loggy.debug("Lefthook Plugin Options {}", options)
        return options
    })

    static def getConfig = Loggy.wrap( {Project project ->
        Loggy.debug("Finding config")
        def extension = getExtension(project, "options")
        def config = extension != null? extension.config: [:]
        config = config["config"] != null? config["config"]: [:] 
        if(config instanceof Closure) {
            config = config.call()
        }
        Loggy.debug("Lefthook Config {}", config)
        return config
    })

    static def install = Loggy.wrap( {Project project ->
        Loggy.debug("Finding config")
        def extension = getExtension(project, "options")
        def config = extension != null? extension.config: [:]
        config = config["config"] != null? config["config"]: [:] 
        if(config instanceof Closure) {
            config = config.call()
        }
        Loggy.debug("Lefthook Config {}", config)
        return config
    })

    static getExtension(Project project, String name) {
        def extension = project[LefthookPlugin.NAME]
        return extension
    }

    static merge(Map lhs, Map rhs) {
        return rhs.inject(lhs.clone()) { map, entry ->
            if (map[entry.key] instanceof Map && entry.value instanceof Map) {
                map[entry.key] = merge(map[entry.key], entry.value)
            } else if (map[entry.key] instanceof Collection && entry.value instanceof Collection) {
                map[entry.key] += entry.value
            } else if (entry.value != null) {
                map[entry.key] = entry.value
            }
            return map
        }
    }

    static def resolve = {Project project, List stack, Map source ->
        Loggy.debug("resolve {}, {}", stack, source)
        def result = source.inject([:]) { map, key, value ->
            Loggy.debug("inject {}, {}, {}", map, key, value)
            if(key instanceof Closure) {
                def installer = new LefthookScriptInstaller(project, stack)
                key.delegate = installer
                key.resolveStrategy = Closure.DELEGATE_FIRST
                key = key.call()
                Loggy.debug("key closure resolved to {}", key)
            }
            if(value instanceof Closure) {
                def installer = new LefthookScriptInstaller(project, stack)
                value.delegate = installer
                value.resolveStrategy = Closure.DELEGATE_FIRST
                value = value.call()
                Loggy.debug("value closure resolved to {}", value)
            }
            def localStack = stack + key
            Loggy.debug("Stack {}", localStack)
            if (value instanceof Map) {
                Loggy.debug("value {} is map, resolving", value)
                def resolvedValue = LefthookPluginHelper.resolve(project, localStack, value)
                Loggy.debug("value {} resolved to {}, assigning to {}", value, resolvedValue, key)
                map[key] = resolvedValue
                Loggy.debug("value {} is map, resolving", value)
            } else if (value instanceof Collection) {
                //TODO check the contents of the collection for closures
                map[key] = value
            } else if(value != null) {
                map[key] = value
            }
            return map
        }
        Loggy.debug("config resolved to {}", result)
        return result
    }

/*

        return rhs.inject(lhs.clone()) { map, entry ->
            stack = stack + entry.key
            if (map[entry.key] instanceof Map && entry.value instanceof Map) {
                map[entry.key] = resolve(stack, map[entry.key], entry.value)
            } else if (map[entry.key] instanceof Collection && entry.value instanceof Collection) {
                map[entry.key] += entry.value
            } else if(entry.value != null) {
                map[entry.key] = entry.value
            }
            return map
        }
    }
    */
}