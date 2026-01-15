/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory

public abstract class LefthookDownloadTask extends DefaultTask {

    public static final String NAME = "lefthookDownload"

    private Project project

    @Internal
    abstract RegularFileProperty getLefthookBinary()

    @Input
    abstract Property<String> getLefthookVersion()

    @Input
    abstract Property<String> getLefthookRepository()

    @OutputDirectory
    abstract DirectoryProperty getLefthookLocation()

    @Inject
    public LefthookDownloadTask(Project project) {
        this.project = project
        getLefthookBinary().convention(null)
        def extension = project.extensions.getByType(LefthookPluginExtension)
        getLefthookVersion().convention(extension.getVersion())
        getLefthookRepository().convention(extension.getRepository())
        getLefthookLocation().convention(extension.getLocation())
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
        def context = [:]
        context.version = getLefthookVersion().get()
        context.repository = getLefthookRepository().get()
        context.location = getLefthookLocation().getAsFile().get()
        
        run(context)
        getLefthookBinary().set(context.binary)
    }

    private static Map run(Map context) {
        Loggy.debug("{} Entry : {}", "LefthookDownloadTask", context)
        def result = Optional.ofNullable(context)
            .map(x -> ttl(x))
            .map(x -> download(x))
            .orElseThrow(() -> new RuntimeException("Unable to install lefthook"))
        Loggy.debug("{} Exit : {}", "LefthookDownloadTask", result != null? result: "null")
        return result
    }

    /**
    * Find the most recent binary and see if it is within ttl
    */
    private static Map ttl(Map x) {
        return x
    }

    private static Map download(Map x) {
        Loggy.debug("{} Entry : {}", "LefthookDownloadTask", x)
        def repo = x.repository ?: x.extension?.getRepository()?.get()
        def arch = x.arch ?: OS.getArch(null)
        def os = x.os ?: OS.getOs(null)
        def version = x.version ?: x.extension?.getVersion()?.get()
        def location = x.location ?: x.extension?.getLocation()?.getAsFile()?.get()

        x.repository = repo
        x.version = version
        x.location = location

        x.binary = LefthookInstallation.install(repo, arch, os, version, location)

        def result = x.binary? x: null
        Loggy.debug("{} Exit : {}", "LefthookDownloadTask", result != null? result: "null")
        return result
    }

}
