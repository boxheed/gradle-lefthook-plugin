/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.yaml.snakeyaml.Yaml

@DisableCachingByDefault(because = "Resolves dynamic script closures during execution")
public abstract class LefthookYmlTask extends DefaultTask {

    public static final String NAME = "lefthookYml"

    @Internal
    abstract MapProperty<String, Object> getConfig()

    @Internal
    abstract DirectoryProperty getLefthookLocation()

    @OutputFile
    abstract RegularFileProperty getLefthookConfigFile()

    @Inject
    public LefthookYmlTask(ProjectLayout layout) {
        getConfig().convention([:])
        getLefthookConfigFile().convention(layout.projectDirectory.file("lefthook.yml"))
    }

    static register(Project project) {
        project.getLogger().info("Registering task {}", NAME)
        def extension = project.extensions.getByType(LefthookPluginExtension)
        return project.getTasks().register(NAME, LefthookYmlTask) { task ->
            task.dependsOn(LefthookRcTask.NAME)
            task.group = LefthookPlugin.GROUP
            task.description = 'Creates the lefthook.yml file'
            task.getLefthookLocation().convention(extension.getLocation())
        }
    }

    @TaskAction
    def runTask() {
        File location = getLefthookLocation().getAsFile().get()
        if(location == null) {
            throw new RuntimeException("Lefthook location is not set")
        }
        def resolved = LefthookPluginHelper.resolve(location, [], getConfig().get())
        String content = new Yaml().dump(resolved)
        def configFile = getLefthookConfigFile().getAsFile().get()
        
        logger.info("Creating Lefthook configuration (lefthook.yml) at {}", configFile.absolutePath)
        if (content != null && !content.isEmpty()) {
            configFile.withWriter { writer ->
                writer.write(content)
            }
        }
    }

}
