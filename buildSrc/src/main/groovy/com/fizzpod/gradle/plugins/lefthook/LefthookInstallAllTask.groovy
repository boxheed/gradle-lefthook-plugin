package com.fizzpod.gradle.plugins.lefthook

import org.gradle.api.Project
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

public class LefthookInstallAllTask extends DefaultTask {

    public static final String NAME = "lefthookInstallAll"

    public static final String GITSEMVER_INSTALL_DIR = ".lefthook"

    private Project project

    @Inject
    public LefthookInstallAllTask(Project project) {
        this.project = project
    }

    static register(Project project) {
        project.getLogger().info("Registering task {}", NAME)
        def taskContainer = project.getTasks()

        return taskContainer.create([name: NAME,
            type: LefthookInstallAllTask,
            dependsOn: [],
            group: LefthookPlugin.GROUP,
            description: 'Downloads and installs lefthook for all architectures'])
    }

    @TaskAction
    def runTask() {
        def context = LefthookPluginHelper.createContext(project)
        context.project = project
        context.extension = extension
        LefthookInstallTask.run(context)
    }

    static def run = Loggy.wrap({ context ->
        return Optional.ofNullable(context)
            .map(x -> LefthookDownloadAllTask.run(x))
            .map(x -> LefthookInstallTask.run(x))
            .orElseThrow(() -> new RuntimeException("Unable to install lefthook"))
    })
}