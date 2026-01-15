/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Internal

public abstract class LefthookRcTask extends DefaultTask {

    public static final String NAME = "lefthookRc"

    @InputFile
    abstract RegularFileProperty getLefthookBinary()

    @Input
    abstract Property<String> getRcConfiguration()

    @OutputFile
    abstract RegularFileProperty getLefthookRcFile()

    @Inject
    public LefthookRcTask(Project project) {
        def extension = project.extensions.getByType(LefthookPluginExtension)
        getRcConfiguration().convention(extension.getRc())
        getLefthookRcFile().convention(extension.getLocation().file(".lefthookrc"))
    }

    static register(Project project) {
        project.getLogger().info("Registering task {}", NAME)
        def taskContainer = project.getTasks()

        return taskContainer.create([name: NAME,
            type: LefthookRcTask,
            dependsOn: [LefthookDownloadTask.NAME],
            group: LefthookPlugin.GROUP,
            description: 'Creates the lefthookrc file'])
    }

    @TaskAction
    def runTask() {
        def binary = getLefthookBinary().getAsFile().get()
        def rcFile = getLefthookRcFile().getAsFile().get()
        def rcConfig = getRcConfiguration().get()
        
        rcFile.withWriter { writer ->
            writer.writeLine "export LEFTHOOK_BIN=${binary.getAbsolutePath()}"
            writer.writeLine rcConfig
        }
    }
}
