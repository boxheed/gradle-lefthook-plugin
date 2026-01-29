/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

public abstract class LefthookBinaryTask extends DefaultTask {

    public static final String NAME = "lefthookBinary"

    @InputDirectory
    abstract DirectoryProperty getLefthookLocation()

    @Internal
    abstract RegularFileProperty getLefthookBinary()

    @Inject
    public LefthookBinaryTask(Project project) {
        def providers = project.getProviders()
        def extension = project.extensions.getByType(LefthookPluginExtension)
        getLefthookLocation().convention(extension.getLocation())
    }

    static register(Project project) {
        project.getLogger().info("Registering task {}", NAME)
        def taskContainer = project.getTasks()

        return taskContainer.create([name: NAME,
            type: LefthookBinaryTask,
            dependsOn: [],
            group: LefthookPlugin.GROUP])
    }

    @TaskAction
    def runTask() {
        File dirFile = getLefthookLocation().getAsFile().get()
        def binary = LefthookInstallation.findBinary(dirFile)
        getLefthookBinary().set(binary)
        if (binary == null || !binary.exists()) {
            throw new IllegalStateException("Lefthook binary not found at expected location: " + getLefthookLocation().getAsFile().get())
        }
    }

}
