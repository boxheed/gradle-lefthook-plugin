/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

public abstract class LefthookVersionTask extends DefaultTask {

    public static final String NAME = "lefthookVersion"

    @InputFile
    abstract RegularFileProperty getLefthookBinary()

    @Inject
    abstract ExecOperations getExecOperations()

    @Inject
    public LefthookVersionTask(Project project) {
    }

    static register(Project project) {
        project.getLogger().info("Registering task {}", NAME)
        def taskContainer = project.getTasks()

        return taskContainer.create([name: NAME,
            type: LefthookVersionTask,
            dependsOn: [],
            group: LefthookPlugin.GROUP,
            description: 'Outputs the current lefthook version'])
    }

    @TaskAction
    def runTask() {
        def binary = getLefthookBinary().getAsFile().get()
        
        getExecOperations().exec { spec ->
            spec.commandLine(binary.absolutePath, "version")
        }
    }

    // Kept for backward compatibility
    static def run = { context ->
        // This logic is likely broken due to changes in InstallTask, 
        // but kept as placeholder or for pure groovy script usage if any.
        // Given we are refactoring the plugin, we assume Gradle usage.
        return null
    }

    static def getOut = Loggy.wrap( { x -> 
            def out = x.sout? x.sout.trim(): ""
            return out
        })

    static def command = Loggy.wrap( { x ->
        def commandParts = []
        commandParts.add(x.binary.getAbsolutePath())
        commandParts.add("version")
        x.command = commandParts.join(" ")
        return x
    } )
        

}