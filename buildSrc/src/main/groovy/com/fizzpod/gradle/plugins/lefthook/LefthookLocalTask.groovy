/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

public abstract class LefthookLocalTask extends DefaultTask {

    public static final String NAME = "lefthookLocal"

    @InputFile
    abstract RegularFileProperty getLefthookRcFile()

    @OutputFile
    abstract RegularFileProperty getLefthookLocalFile()

    @Inject
    public LefthookLocalTask(Project project) {
        // Convention for output file is project root/lefthook-local.yml
        getLefthookLocalFile().convention(project.layout.projectDirectory.file("lefthook-local.yml"))
    }

    static register(Project project) {
        project.getLogger().info("Registering task {}", NAME)
        def taskContainer = project.getTasks()

        return taskContainer.create([name: NAME,
            type: LefthookLocalTask,
            dependsOn: [],
            group: LefthookPlugin.GROUP,
            description: 'Creates the lefthook-local.yml file'])
    }

    @TaskAction
    def runTask() {
        def rcFile = getLefthookRcFile().getAsFile().get()
        def localFile = getLefthookLocalFile().getAsFile().get()
        
        localFile.withWriter { writer ->
            writer.writeLine "rc: ${rcFile.absolutePath}"
        }
    }

    // Kept for backward compatibility if called statically, though likely broken now without context
    static def run = { context ->
        def status = Optional.ofNullable(context)
            .map(x -> LefthookRcTask.run(x))
            .map(x -> LefthookLocalTask.writeLocal(x))
            .orElseThrow(() -> new RuntimeException("Unable to run lefthook"))
        return status
    }

    static def writeLocal = Loggy.wrap( { x ->
        def lefthookLocal = x.project.file('lefthook-local.yml')
        def rcPath = x.rc.getAbsolutePath()
        lefthookLocal.withWriter { writer ->
            writer.writeLine "rc: ${rcPath}"
        }
        return x
    })
}