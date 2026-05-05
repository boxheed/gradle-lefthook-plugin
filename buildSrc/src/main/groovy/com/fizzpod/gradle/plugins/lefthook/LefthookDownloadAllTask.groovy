/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@CacheableTask
public abstract class LefthookDownloadAllTask extends DefaultTask {

    public static final String NAME = "lefthookDownloadAll"

    private def osArches = [
        [OS.Family.LINUX.id, OS.Arch.AMD64.id],
        [OS.Family.LINUX.id, OS.Arch.ARM64.id],
        [OS.Family.MAC.id, OS.Arch.AMD64.id],
        [OS.Family.MAC.id, OS.Arch.ARM64.id],
        [OS.Family.WINDOWS.id, OS.Arch.AMD64.id]
    ]

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    abstract RegularFileProperty getResolvedVersionFile()

    @Input
    abstract Property<String> getLefthookRepository()

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract DirectoryProperty getLefthookLocation()

    @Inject
    public LefthookDownloadAllTask(Project project) {
        def providers = project.getProviders()
        def extension = project.extensions.getByType(LefthookPluginExtension)
        getLefthookRepository().convention(extension.getRepository())
        getLefthookLocation().convention(extension.getLocation())
    }

    static register(Project project) {
        project.getLogger().info("Registering task {}", NAME)
        def taskContainer = project.getTasks()

        return taskContainer.create([
            name: NAME,
            type: LefthookDownloadAllTask,
            dependsOn: [],
            group: LefthookPlugin.GROUP,
            description: 'Download and install all lefthook binaries'
        ])
    }

    @TaskAction
    def runTask() {
        def version = getResolvedVersionFile().getAsFile().get().text.trim()
        for (def osArch : osArches) {
            def context = [:]
            context.version = version
            context.repo = getLefthookRepository().get()
            context.location = getLefthookLocation().getAsFile().get()

            context.os = OS.getOs(osArch[0])
            context.arch = OS.getArch(osArch[1])
            context.binary =
                    LefthookInstallation.binary(context.location, context.version, context.os, context.arch)

            Loggy.lifecycle("Installing {} : {} at {}", context.os, context.arch, context.binary)
            LefthookDownloadTask.run(context)
        }
    }
}
