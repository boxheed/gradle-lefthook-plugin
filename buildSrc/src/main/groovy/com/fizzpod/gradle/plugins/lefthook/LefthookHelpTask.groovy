/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

public abstract class LefthookHelpTask extends DefaultTask {

    public static final String NAME = "lefthookHelp"

    @InputFile
    abstract RegularFileProperty getLefthookBinary()

    @Inject
    abstract ExecOperations getExecOperations()

    @Inject
    public LefthookHelpTask(Project project) {
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
        def binary = getLefthookBinary().getAsFile().get()
        getExecOperations().exec { spec ->
            spec.commandLine(binary.absolutePath, "help")
        }
    }
}
