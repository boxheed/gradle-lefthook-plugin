package com.fizzpod.gradle.plugins.lefthook

import org.gradle.api.Project
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

public class LefthookDownloadTask extends DefaultTask {

    public static final String NAME = "lefthookDownload"

    public static final String LEFTHOOK_INSTALL_DIR = ".lefthook"

    private Project project

    @Inject
    public LefthookDownloadTask(Project project) {
        this.project = project
    }

    static register(Project project) {
        project.getLogger().info("Registering task {}", NAME)
        def taskContainer = project.getTasks()

        return taskContainer.create([name: NAME,
            type: LefthookDownloadTask,
            dependsOn: [],
            group: LefthookPlugin.GROUP,
            description: 'Downloads and installs lefthook'])
    }

    @TaskAction
    def runTask() {
        def extension = project[LefthookPlugin.NAME]
        def context = [:]
        context.project = project
        context.extension = extension
        LefthookDownloadTask.run(context)
    }

    static def run = Loggy.wrap({ context ->
        return Optional.ofNullable(context)
            .map(x -> LefthookDownloadTask.location(x))
            .map(x -> LefthookDownloadTask.download(x))
            .orElseThrow(() -> new RuntimeException("Unable to install lefthook"))
    })

    static def download = Loggy.wrap({ x ->
        def repo = x.extension.repository
        def arch = x.extension.arch
        def os = x.extension.os
        def version = x.extension.version
        def location = x.location
        x.binary = LefthookInstallation.install(repo, arch, os, version, location)
        return x.binary? x: null
    })

    static def location = Loggy.wrap({ x ->
        def projectDir = x.project.rootDir
        def lefthookDir = x.extension.location
        x.location = new File(projectDir, lefthookDir)
        return x.location? x: null
    })
}