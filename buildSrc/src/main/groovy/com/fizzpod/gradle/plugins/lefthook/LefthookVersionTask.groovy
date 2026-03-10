/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Not worth caching")
public abstract class LefthookVersionTask extends DefaultTask {

    public static final String NAME = "lefthookVersion"

    @Inject
    abstract ExecOperations getExecOperations()

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract RegularFileProperty getLefthookBinary()

    @Inject
    public LefthookVersionTask(Project project) {
    }

    static register(Project project) {
        project.getLogger().info("Registering task {}", NAME)
        def taskContainer = project.getTasks()

        return taskContainer.create([name: NAME,
            type: LefthookVersionTask,
            dependsOn: [LefthookBinaryTask.NAME],
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

}
