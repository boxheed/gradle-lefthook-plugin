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
        
        LefthookDownloadTask.run(context)
        getLefthookBinary().set(context.binary)
    }

    static def run = Loggy.wrap({ context ->
        return Optional.ofNullable(context)
            .map(x -> LefthookDownloadTask.ttl(x))
            .map(x -> LefthookDownloadTask.download(x))
            .orElseThrow(() -> new RuntimeException("Unable to install lefthook"))
    })

    /**
    * Find the most recent binary and see if it is within ttl
    */
    static def ttl = { x ->
        return x
    }

    static def download = Loggy.wrap({ x ->
        def repo = x.repository ?: x.extension?.getRepository()?.get()
        def arch = x.arch ?: OS.getArch(null)
        def os = x.os ?: OS.getOs(null)
        def version = x.version ?: x.extension?.getVersion()?.get()
        def location = x.location ?: x.extension?.getLocation()?.getAsFile()?.get()

        x.repository = repo
        x.version = version
        x.location = location

        x.binary = LefthookInstallation.install(repo, arch, os, version, location)

        return x.binary? x: null
    })

    static def location = Loggy.wrap({ x ->
        x.location = x.extension.getLocation().get().getAsFile()
        return x.location? x: null
    })

}
