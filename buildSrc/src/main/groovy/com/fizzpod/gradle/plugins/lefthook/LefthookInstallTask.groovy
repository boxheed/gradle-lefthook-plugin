/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

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
        def context = LefthookPluginHelper.createContext(project)
        LefthookInstallTask.run(context)
    }

    static def run = Loggy.wrap({ context ->
        return Optional.ofNullable(context)
            .map(x -> LefthookDownloadTask.run(x))
            .map(x -> LefthookRcTask.run(x))
            .map(x -> LefthookLocalTask.run(x))
            .map(x -> LefthookInitTask.run(x))
            .orElseThrow(() -> new RuntimeException("Unable to install lefthook"))
    })
}
