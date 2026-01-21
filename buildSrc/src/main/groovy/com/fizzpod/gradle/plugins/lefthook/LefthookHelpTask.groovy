/* (C) 2024-2025 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.api.file.DirectoryProperty

public abstract class LefthookHelpTask extends DefaultTask {

    public static final String NAME = "lefthookHelp"

    @Internal
    abstract DirectoryProperty getLefthookLocation()

    @Inject
    abstract ExecOperations getExecOperations()

    @Inject
    public LefthookHelpTask(Project project) {
        def extension = project.extensions.getByType(LefthookPluginExtension)
        getLefthookLocation().convention(extension.getLocation())
    }

    static register(Project project) {
        project.getLogger().info("Registering task {}", NAME)
        def taskContainer = project.getTasks()

        return taskContainer.create([name: NAME,
            type: LefthookHelpTask,
            dependsOn: [],
            group: LefthookPlugin.GROUP,
            description: 'Outputs the current lefthook help'])
    }

    @TaskAction
    def runTask() {
        def binary = LefthookInstallation.findBinary(getLefthookLocation().getAsFile().get())
        
        getExecOperations().exec { spec ->
            spec.commandLine(binary.absolutePath, "help")
        }
    }

    static def run = { context ->
        return null
    }

    static def getOut = Loggy.wrap( { x -> 
            def out = x.sout? x.sout.trim(): ""
            return out
        })

    static def command = Loggy.wrap( { x ->
        def commandParts = []
        commandParts.add(x.binary.getAbsolutePath())
        commandParts.add("help")
        x.command = commandParts.join(" ")
        return x
    } )
        

}