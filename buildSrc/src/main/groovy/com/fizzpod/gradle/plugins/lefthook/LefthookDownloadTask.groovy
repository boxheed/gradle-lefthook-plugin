/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CacheableTask
public abstract class LefthookDownloadTask extends DefaultTask {

    public static final String NAME = "lefthookDownload"

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    abstract RegularFileProperty getResolvedVersionFile()

    @Input
    abstract Property<String> getLefthookRepository()

    @OutputDirectory
    abstract DirectoryProperty getLefthookLocation()

    @OutputFile
    abstract RegularFileProperty getLefthookBinary()

    @Inject
    public LefthookDownloadTask(Project project) {
        def extension = project.extensions.getByType(LefthookPluginExtension)
        getLefthookRepository().convention(extension.getRepository())
        getLefthookLocation().convention(extension.getLocation())

        getLefthookBinary()
                .set(
                        getResolvedVersionFile()
                                .zip(
                                        getLefthookLocation(),
                                        { versionFile, location ->
                                            def file = versionFile.asFile
                                            def version =
                                                    file.exists()
                                                            ? file.text.trim()
                                                            : extension.getVersion().get()
                                            def os = OS.getOs(null)
                                            def arch = OS.getArch(null)
                                            def name =
                                                    LefthookInstallation.getBinaryName(
                                                            version, os, arch)
                                            return location.file(name)
                                        }))
    }

    static register(Project project) {
        project.getLogger().info("Registering task {}", NAME)
        def taskContainer = project.getTasks()

        return taskContainer.create([
            name: NAME,
            type: LefthookDownloadTask,
            dependsOn: [],
            group: LefthookPlugin.GROUP,
            description: 'Downloads and installs lefthook'
        ])
    }

    @TaskAction
    def runTask() {
        def context = [:]
        context.repo = getLefthookRepository().get()
        context.arch = OS.getArch(null)
        context.os = OS.getOs(null)
        context.version = getResolvedVersionFile().getAsFile().get().text.trim()
        context.location = getLefthookLocation().getAsFile().get()
        context.binary = getLefthookBinary().get().asFile

        if (!context.binary.exists()) {
            LefthookDownloadTask.run(context)
        }
    }

    static def run = { context ->
        return java.util.Optional.ofNullable(context)
                .map(x -> LefthookDownloadTask.download(x))
                .orElseThrow(() -> new RuntimeException("Unable to install lefthook"))
    }

    static def download = { context ->
        // The version passed here is now always a concrete version
        def artifact =
                LefthookInstallation.resolveArtifact(
                        context.repo, context.arch, context.os, context.version)
        LefthookInstallation.downloadAndInstall(artifact.url, context.binary, context.os)
        return context
    }
}
