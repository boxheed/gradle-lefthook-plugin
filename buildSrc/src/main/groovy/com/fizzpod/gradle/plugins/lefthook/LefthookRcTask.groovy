/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Not worth caching")
public abstract class LefthookRcTask extends DefaultTask {

    public static final String NAME = "lefthookRc"

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract RegularFileProperty getLefthookBinary()

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract DirectoryProperty getLefthookLocation()

    @Input
    abstract Property<String> getRcConfiguration()

    @OutputFile
    abstract RegularFileProperty getLefthookRcFile()

    @Inject
    public LefthookRcTask(Project project) {
        def extension = project.extensions.getByType(LefthookPluginExtension)
        def providers = project.getProviders()
        getLefthookLocation().convention(extension.getLocation())
        getRcConfiguration().convention(extension.getRc())
        getLefthookRcFile().fileProvider(providers.provider({
                File dirFile = getLefthookLocation().getAsFile().get()
                return new File(dirFile, ".lefthookrc")
            })
        )
    }

    static register(Project project) {
        project.getLogger().info("Registering task {}", NAME)
        def taskContainer = project.getTasks()

        return taskContainer.create([name: NAME,
            type: LefthookRcTask,
            dependsOn: [LefthookBinaryTask.NAME],
            group: LefthookPlugin.GROUP,
            description: 'Creates the lefthookrc file'])
    }

    @TaskAction
    def runTask() {
        def binary = getLefthookBinary().getAsFile().get()
        def rcFile = getLefthookRcFile().getAsFile().get()
        def rcConfig = getRcConfiguration().get()
        println("Left ${binary.getAbsolutePath()}")
        rcFile.withWriter { writer ->
            writer.writeLine "export LEFTHOOK_BIN=${binary.getAbsolutePath()}"
            writer.writeLine rcConfig
        }
    }
}
