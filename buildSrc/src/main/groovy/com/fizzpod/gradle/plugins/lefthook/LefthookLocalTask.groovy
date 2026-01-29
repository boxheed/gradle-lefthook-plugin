/* (C) 2024-2026 */
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
            dependsOn: [LefthookRcTask.NAME],
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

    
}
