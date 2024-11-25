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

    static getExtension(Project project, String name) {
        def extension = project[LefthookPlugin.NAME]
/*
        def extension = null
         def extensions = project
            .extensions
            .findByName(LefthookPlugin.NAME)
            .matching( entry -> {
                Loggy.debug("Lefthook extension: {}", entry.name)
                def match =  entry.name.equals(name) || name ==~ entry.name
                return match
            })
        Loggy.debug("Extensions {}", extensions)
        if(extensions != null && extensions.size() == 1) {
            extension = extensions[0]
        } 
*/
        return extension
    }

    static merge(Map lhs, Map rhs) {
        return rhs.inject(lhs.clone()) { map, entry ->
            if (map[entry.key] instanceof Map && entry.value instanceof Map) {
                map[entry.key] = merge(map[entry.key], entry.value)
            } else if (map[entry.key] instanceof Collection && entry.value instanceof Collection) {
                map[entry.key] += entry.value
            } else if(entry.value != null) {
                map[entry.key] = entry.value
            }
            return map
        }
    }
    
}