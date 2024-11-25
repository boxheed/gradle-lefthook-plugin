package com.fizzpod.gradle.plugins.lefthook

import org.gradle.api.Project
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject


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
        for(def osArch: osArches) {
            def context = LefthookPluginHelper.createContext(project)
            context.extension.os = osArch[0]
            context.extension.arch = osArch[1]
            Loggy.lifecycle("Installing {} : {}", context.extension.os, context.extension.arch)
            LefthookDownloadTask.run(context)
        }
    }

    def getAsset(def context) {
        context.os = currentOs
        context.arch = currentArch
        return super.getAsset(context)
    }
    
    def install(def context) {
        context.os = currentOs
        context.arch = currentArch
        super.install(context)
    }
    
    def download(def context) {
        context.os = currentOs
        context.arch = currentArch
        super.download(context)
    }
    

}