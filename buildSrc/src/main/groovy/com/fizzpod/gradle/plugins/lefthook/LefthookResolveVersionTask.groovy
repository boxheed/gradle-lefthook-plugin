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
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

@CacheableTask
public abstract class LefthookResolveVersionTask extends DefaultTask {

    public static final String NAME = "lefthookResolveVersion"

    @Input
    abstract Property<String> getLefthookVersion()

    @Input
    abstract Property<String> getLefthookRepository()

    @Input
    abstract Property<Long> getTtl()

    @OutputDirectory
    abstract DirectoryProperty getVersionLocation()

    @OutputFile
    abstract RegularFileProperty getResolvedVersionFile()

    @Inject
    public LefthookResolveVersionTask(Project project) {
        def extension = project.extensions.getByType(LefthookPluginExtension)
        getLefthookVersion().convention(extension.getVersion())
        getLefthookRepository().convention(extension.getRepository())
        getTtl().convention(86400000) // 1 day
        getVersionLocation().convention(extension.getLocation())
        getResolvedVersionFile().convention(getVersionLocation().file("version.txt"))
    }

    static register(Project project) {
        project.getLogger().info("Registering task {}", NAME)
        def taskContainer = project.getTasks()

        return taskContainer.create([
            name: NAME,
            type: LefthookResolveVersionTask,
            group: LefthookPlugin.GROUP,
            description: "Resolves the 'latest' lefthook version from GitHub with TTL caching"
        ])
    }

    @TaskAction
    def runTask() {
        File versionFile = getResolvedVersionFile().get().asFile
        long ttl = getTtl().get()

        if (versionFile.exists() && (System.currentTimeMillis() - versionFile.lastModified() < ttl)) {
            logger.info(
                    "Resolved version file is within TTL, skipping GitHub query. Using version {}",
                    versionFile.text)
            return
        }

        String version = getLefthookVersion().get()
        if (version.equalsIgnoreCase("latest")) {
            logger.lifecycle("Querying GitHub for the latest lefthook version.")
            def artifact =
                    LefthookInstallation.resolveArtifact(
                            getLefthookRepository().get(),
                            OS.getArch(null),
                            OS.getOs(null),
                            "latest")
            version = artifact.version
        } else {
            logger.info("Using configured lefthook version: {}", version)
        }

        versionFile.parentFile.mkdirs()
        versionFile.text = version
    }
}
