package com.fizzpod.gradle.plugins.lefthook

import org.gradle.api.Project
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

public class LefthookInstallTask extends DefaultTask {

    public static final String NAME = "lefthookInstall"

    public static final String GITSEMVER_INSTALL_DIR = ".lefthook"

    private Project project

    @Inject
    public LefthookInstallTask(Project project) {
        this.project = project
    }

    static register(Project project) {
        project.getLogger().info("Registering task {}", NAME)
        def taskContainer = project.getTasks()

        return taskContainer.create([name: NAME,
            type: LefthookInstallTask,
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
        LefthookInstallTask.run(context)
    }

    static def run = Loggy.wrap({ context ->
        return Optional.ofNullable(context)
            .map(x -> LefthookInstallTask.location(x))
            .map(x -> LefthookInstallTask.install(x))
            .orElseThrow(() -> new RuntimeException("Unable to install lefthook"))
    })

    static def install = Loggy.wrap({ x ->
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