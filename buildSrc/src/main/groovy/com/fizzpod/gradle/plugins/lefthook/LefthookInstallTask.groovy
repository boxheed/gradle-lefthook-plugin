/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.api.provider.Property
import org.gradle.api.provider.MapProperty
import org.gradle.api.file.DirectoryProperty

public abstract class LefthookInstallTask extends DefaultTask {

    public static final String NAME = "lefthookInstall"

    public static final String GITSEMVER_INSTALL_DIR = ".lefthook"

    private Project project

    @Inject
    public abstract WorkerExecutor getWorkerExecutor();

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

    interface LefthookInstallParameters extends WorkParameters {
        DirectoryProperty getProjectDir()
        MapProperty<String, Object> getLefthookOptions()
        MapProperty<String, Object> getLefthookConfig()
    }

    @TaskAction
    def runTask() {
        // Resolve options and config before submitting to worker
        // We use the existing helper but extract what we need
        def options = LefthookPluginHelper.getOptions(project)
        def config = LefthookPluginHelper.getConfig(project)
        def projectDir = project.layout.projectDirectory

        getWorkerExecutor().noIsolation().submit(LefthookInstallAction.class) { parameters ->
             parameters.getProjectDir().set(projectDir)
             parameters.getLefthookOptions().set(options)
             parameters.getLefthookConfig().set(config)
        }
    }

    static def run = Loggy.wrap({ context ->
        return Optional.ofNullable(context)
            .map(x -> LefthookDownloadTask.run(x))
            .map(x -> LefthookRcTask.run(x))
            .map(x -> LefthookLocalTask.run(x))
            .map(x -> LefthookInitTask.run(x))
            .orElseThrow(() -> new RuntimeException("Unable to install lefthook"))
    })

    abstract static class LefthookInstallAction implements WorkAction<LefthookInstallParameters> {
        @Override
        public void execute() {
            def parameters = getParameters()
            def projectDir = parameters.getProjectDir().get().asFile
            def options = parameters.getLefthookOptions().get()
            def config = parameters.getLefthookConfig().get()

            def context = LefthookPluginHelper.createContextForWorker(projectDir, options, config)
            LefthookInstallTask.run(context)
        }
    }
}
