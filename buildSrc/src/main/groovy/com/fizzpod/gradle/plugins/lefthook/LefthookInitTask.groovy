/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.yaml.snakeyaml.Yaml

public abstract class LefthookInitTask extends DefaultTask {

    public static final String NAME = "lefthookInit"

    @Internal
    abstract MapProperty<String, Object> getConfig()

    @Input
    Provider<String> getResolvedConfigContent() {
        return getConfig().map { config -> 
             // Resolve the configuration (handle closures etc.)
             def resolved = LefthookPluginHelper.resolve(project, [], config)
             Yaml yaml = new Yaml()
             return yaml.dump(resolved)
        }
    }

    @OutputFile
    abstract RegularFileProperty getLefthookConfigFile()

    @Inject
    public LefthookInitTask(Project project) {
        getConfig().convention([:])
        getLefthookConfigFile().convention(project.layout.projectDirectory.file("lefthook.yml"))
    }

    static register(Project project) {
        project.getLogger().info("Registering task {}", NAME)
        def taskContainer = project.getTasks()

        return taskContainer.create([name: NAME,
            type: LefthookInitTask,
            dependsOn: [],
            group: LefthookPlugin.GROUP,
            description: 'Creates the lefthook.yml file'])
    }

    @TaskAction
    def runTask() {
        def content = getResolvedConfigContent().get()
        def configFile = getLefthookConfigFile().getAsFile().get()
        
        if (content != null && !content.isEmpty()) {
            configFile.withWriter { writer ->
                writer.write(content)
            }
        }
    }

    // Kept for backward compatibility
    static def run = { context ->
        def status = Optional.ofNullable(context)
            .map(x -> LefthookDownloadTask.run(x))
            .map(x -> LefthookRcTask.run(x))
            .map(x -> LefthookInitTask.writeLocal(x))
            .map(x -> LefthookInitTask.resolveConfig(x))
            .map(x -> LefthookInitTask.writeConfig(x))
            .map(x -> LefthookInitTask.command(x))
            .map(x -> Command.execute(x))
            .orElseThrow(() -> new RuntimeException("Unable to run lefthook"))
        return status
    }
    
    // Legacy static methods kept for compatibility
    static def writeRc = Loggy.wrap( { x ->
        def binary = x.binary.getAbsolutePath()
        def rc = new File(x.location, ".lefthookrc")
        rc.withWriter { writer ->
            writer.writeLine "export LEFTHOOK_BIN=${binary}"
        }
        x.rc = rc
        return x
    })

    static def writeLocal = Loggy.wrap( { x ->
        def binary = x.binary.getAbsolutePath()
        def lefthookLocal = x.project.file('lefthook-local.yml')
        def rcPath = x.rc.getAbsolutePath()
        lefthookLocal.withWriter { writer ->
            writer.writeLine "rc: ${rcPath}"
        }
        return x
    })

    static def runConfigInstallers = Loggy.wrap( { x ->
        def binary = x.binary.getAbsolutePath()
        def config = x.config
        def lefthookLocal = x.project.file('lefthook.yml')
        def rcPath = x.rc.getAbsolutePath()
        lefthookLocal.withWriter { writer ->
            Yaml yaml = new Yaml()
            yaml.dump(x.config, writer)
        }
        return x
    })

    static def resolveConfig = Loggy.wrap( { x ->
        if(x.config != null) {
            x.config = LefthookPluginHelper.resolve(x.project, [], x.config)
        }
        return x
    })

    static def writeConfig = Loggy.wrap( { x ->
        if(x.config != null) {
            def binary = x.binary.getAbsolutePath()
            def lefthook = x.project.file('lefthook.yml')
            lefthook.withWriter { writer ->
                Yaml yaml = new Yaml()
                yaml.dump(x.config, writer)
            }
        }
        return x
    })

    static def getOut = Loggy.wrap( { x -> 
        def out = x.sout? x.sout.trim(): ""
        return out
    })

    static def command = Loggy.wrap( { x ->
        def commandParts = []
        commandParts.add(x.binary.getAbsolutePath())
        commandParts.add("install")
        commandParts.add("-f")
        x.command = commandParts.join(" ")
        return x
    } )

}
