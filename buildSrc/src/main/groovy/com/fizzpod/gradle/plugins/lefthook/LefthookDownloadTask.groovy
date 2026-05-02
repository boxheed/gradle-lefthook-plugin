/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import javax.inject.Inject
import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Not worth caching")
public abstract class LefthookDownloadTask extends DefaultTask {

    public static final String NAME = "lefthookDownload"

    @Optional
    @OutputFile
    abstract RegularFileProperty getLefthookBinary()

    @Input
    abstract Property<Long> getLastModified()

    @Input
    abstract Property<Long> getTtl()

    @Input
    abstract Property<String> getLefthookVersion()

    @Input
    abstract Property<String> getLefthookRepository()

    @OutputDirectory
    abstract DirectoryProperty getLefthookLocation()

    @Inject
    public LefthookDownloadTask(Project project) {
        def providers = project.getProviders()
        def extension = project.extensions.getByType(LefthookPluginExtension)
        getLefthookVersion().convention(extension.getVersion())
        getLefthookRepository().convention(extension.getRepository())
        getLefthookLocation().convention(extension.getLocation())
        getTtl().convention(86400000) // 1 day
        getLastModified().value(providers.provider({
                //used to force the gradle up to date check to fail and force a download check
                File dirFile = extension.getLocation().getAsFile().get()
                def binary = LefthookInstallation.findBinary(dirFile)
                def insideTtl =  binary && binary.exists() && (System.currentTimeMillis() - FileUtils.lastModified(binary) < getTtl().get()) // 1 day
                def lastModified = 0L
                if(binary && binary.exists()) {
                    lastModified = FileUtils.lastModified(binary)
                }
                //calculate a new modified date for the binary
                if(!insideTtl) {
                    lastModified = System.currentTimeMillis()
                    // round to the nearest second as filesystem times are not as granular as system times
                    lastModified = Math.round(lastModified / 1000.0) * 1000L
                }
                return lastModified
            })
        )

        getLefthookBinary().fileProvider(providers.provider( {
				
                File dirFile = extension.getLocation().getAsFile().get()
                def binary = LefthookInstallation.findBinary(dirFile)
                if(binary == null) {
                    return null
                }
                return dirFile.toPath().resolve(binary.name).toFile()
            })
        )
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
        context.repo = getLefthookRepository().get()
        context.arch = OS.getArch(null)
        context.os = OS.getOs(null)
        context.version = getLefthookVersion().get()
        context.location = getLefthookLocation().getAsFile().get()
        context.lastModified = getLastModified().get()
        LefthookDownloadTask.run(context)
    }

    static def run = { context ->
        return java.util.Optional.ofNullable(context)
            .map(x -> LefthookDownloadTask.download(x))
            .map(x -> LefthookDownloadTask.touch(x))
            .orElseThrow(() -> new RuntimeException("Unable to install lefthook"))
    }

    static def download = { context ->
        context.binary = LefthookInstallation.install(context.repo, context.arch, context.os, context.version, context.location)
        return context
    }

    static def touch = { context ->
        if(context.binary && context.binary.exists() && context.lastModified > 0L) {
            context.binary.setLastModified(context.lastModified)
        }
        return context
    }

}
