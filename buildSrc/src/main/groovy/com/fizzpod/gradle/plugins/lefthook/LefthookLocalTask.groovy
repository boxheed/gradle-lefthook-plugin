/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@CacheableTask
public abstract class LefthookLocalTask extends DefaultTask {

    public static final String NAME = "lefthookLocal"

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract RegularFileProperty getLefthookRcFile()

    @OutputFile
    abstract RegularFileProperty getLefthookLocalFile()

    @Inject
    public LefthookLocalTask(ProjectLayout layout) {
        // Convention for output file is project root/lefthook-local.yml
        getLefthookLocalFile().convention(layout.projectDirectory.file("lefthook-local.yml"))
    }

    static register(Project project) {
        project.getLogger().info("Registering task {}", NAME)
        return project.getTasks().register(NAME, LefthookLocalTask) { task ->
            task.dependsOn(LefthookRcTask.NAME)
            task.group = LefthookPlugin.GROUP
            task.description = 'Creates the lefthook-local.yml file'
        }
    }

    @TaskAction
    def runTask() {
        def rcFile = getLefthookRcFile().getAsFile().get()
        def localFile = getLefthookLocalFile().getAsFile().get()
        
        logger.info("Creating Lefthook local configuration (lefthook-local.yml) at {}", localFile.absolutePath)
        localFile.withWriter { writer ->
            writer.writeLine "rc: ${rcFile.absolutePath}"
        }
    }

    
}
