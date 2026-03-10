/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import org.yaml.snakeyaml.Yaml

@DisableCachingByDefault(because = "Not worth caching")
public abstract class LefthookYmlTask extends DefaultTask {

    public static final String NAME = "lefthookYml"

    @Internal
    abstract MapProperty<String, Object> getConfig()

    @Input
    Provider<String> getResolvedConfigContent() {
        return getConfig().map { config -> 
             // Resolve the configuration (handle closures etc.)
             def resolved = LefthookPluginHelper.resolve(project, [], config)
             Yaml yaml = new Yaml()
             return yaml.dump(resolved)
        }
    }

    @OutputFile
    abstract RegularFileProperty getLefthookConfigFile()

    @Inject
    public LefthookYmlTask(Project project) {
        getConfig().convention([:])
        getLefthookConfigFile().convention(project.layout.projectDirectory.file("lefthook.yml"))
    }

    static register(Project project) {
        project.getLogger().info("Registering task {}", NAME)
        def taskContainer = project.getTasks()

        return taskContainer.create([name: NAME,
            type: LefthookYmlTask,
            dependsOn: [],
            group: LefthookPlugin.GROUP,
            description: 'Creates the lefthook.yml file'])
    }

    @TaskAction
    def runTask() {
        def content = getResolvedConfigContent().get()
        def configFile = getLefthookConfigFile().getAsFile().get()
        
        if (content != null && !content.isEmpty()) {
            configFile.withWriter { writer ->
                writer.write(content)
            }
        }
    }

}
