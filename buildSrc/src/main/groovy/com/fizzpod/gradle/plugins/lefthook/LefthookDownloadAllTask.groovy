/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile



public abstract class LefthookDownloadAllTask extends DefaultTask {

    public static final String NAME = "lefthookDownloadAll"

    private def osArches = [
        [OS.Family.LINUX.id, OS.Arch.AMD64.id],
        [OS.Family.LINUX.id, OS.Arch.ARM64.id],
        [OS.Family.MAC.id, OS.Arch.AMD64.id],
        [OS.Family.MAC.id, OS.Arch.ARM64.id],
        [OS.Family.WINDOWS.id, OS.Arch.AMD64.id]
    ]

    private Project project

    @Input
    abstract Property<String> getLefthookVersion()

    @Input
    abstract Property<String> getLefthookRepository()

    @InputDirectory
    abstract DirectoryProperty getLefthookLocation()

    @Inject
    public LefthookDownloadAllTask(Project project) {
        this.project = project
        this.project = project
        def providers = project.getProviders()
        def extension = project.extensions.getByType(LefthookPluginExtension)
        getLefthookVersion().convention(extension.getVersion())
        getLefthookRepository().convention(extension.getRepository())
        getLefthookLocation().convention(extension.getLocation())
    }

    static register(Project project) {
        project.getLogger().info("Registering task {}", NAME)
        def taskContainer = project.getTasks()

        taskContainer.create([name: NAME,
            type: LefthookDownloadAllTask,
            dependsOn: [],
            group: LefthookPlugin.GROUP,
            description: 'Download and install all lefthook binaries'])
    }

    @TaskAction
    def runTask() {

        for(def osArch: osArches) {
            def context = [:]
            context.version = getLefthookVersion().get()
            context.repo = getLefthookRepository().get()
            context.location = getLefthookLocation().getAsFile().get()
            
            context.os = OS.getOs(osArch[0])
            context.arch = OS.getArch(osArch[1])
            
            Loggy.lifecycle("Installing {} : {}", context.os, context.arch)
            LefthookDownloadTask.run(context)
        }
    }
}
