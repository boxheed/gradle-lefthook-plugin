/* (C) 2024 */
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
            
            // Convert String IDs to OS objects if necessary?
            // OS.groovy probably has a lookup.
            // Let's check OS.groovy later. 
            // LefthookInstallation.install expects OS.Arch and OS.Family enums/objects.
            // osArches array has strings (IDs).
            
            // Re-checking LefthookDownloadAllTask original code:
            // context.extension.os = osArch[0]
            // context.extension.arch = osArch[1]
            // And LefthookDownloadTask used OS.getArch() which returns an object.
            
            // I need to convert IDs to Enum/Object.
            context.os = OS.getOs(osArch[0])
            context.arch = OS.getArch(osArch[1])
            
            Loggy.lifecycle("Installing {} : {}", context.os, context.arch)
            LefthookDownloadTask.run(context)
        }
    }
    
    // Keeping these overrides if they were doing something useful, 
    // but they seem to rely on super.methods which don't exist in DefaultTask? 
    // Wait, LefthookDownloadAllTask extends DefaultTask.
    // The previous code had:
    /*
    def getAsset(def context) {
        context.os = currentOs
        context.arch = currentArch
        return super.getAsset(context)
    }
    */
    // DefaultTask does not have `getAsset`.
    // It seems LefthookDownloadAllTask might have been copy-pasted or extended a different class before.
    // Given the imports `import org.gradle.api.DefaultTask`, it extends DefaultTask.
    // So `super.getAsset` would fail unless DefaultTask has it (it doesn't).
    // I will remove these methods as they seem dead/broken code.
}