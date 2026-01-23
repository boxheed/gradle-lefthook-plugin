/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction


public class LefthookDownloadAllTask extends DefaultTask {

    public static final String NAME = "lefthookDownloadAll"

    private Project project
    private def osArches = [
        [OS.Family.LINUX.id, OS.Arch.AMD64.id],
        [OS.Family.LINUX.id, OS.Arch.ARM64.id],
        [OS.Family.MAC.id, OS.Arch.AMD64.id],
        [OS.Family.MAC.id, OS.Arch.ARM64.id],
        [OS.Family.WINDOWS.id, OS.Arch.AMD64.id]
    ]

    @Inject
    public LefthookDownloadAllTask(Project project) {
        this.project = project
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
        def extension = project.extensions.getByType(LefthookPluginExtension)
        for(def osArch: osArches) {
            def context = [:]
            context.version = extension.getVersion().get()
            context.repository = extension.getRepository().get()
            context.location = extension.getLocation().getAsFile().get()
            
            context.os = OS.getOs(osArch[0])
            context.arch = OS.getArch(osArch[1])
            
            Loggy.lifecycle("Installing {} : {}", context.os, context.arch)
            LefthookDownloadTask.run(context)
        }
    }
}
