/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Not worth caching")
public abstract class LefthookInstallTask extends DefaultTask {

    public static final String NAME = "lefthookInstall"

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract RegularFileProperty getLefthookBinary()

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract RegularFileProperty getLefthookConfigFile()

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract RegularFileProperty getLefthookLocalFile()
    
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract RegularFileProperty getLefthookRcFile()

    @Internal
    abstract DirectoryProperty getGitHooksDir()
    
    @Inject
    abstract ExecOperations getExecOperations()

    @Inject
    public LefthookInstallTask(Project project) {
        getGitHooksDir().convention(project.layout.projectDirectory.dir(".git/hooks"))
    }

    static register(Project project) {
        project.getLogger().info("Registering task {}", NAME)
        def taskContainer = project.getTasks()

        return taskContainer.create([name: NAME,
            type: LefthookInstallTask,
            dependsOn: [LefthookBinaryTask.NAME, LefthookRcTask.NAME, LefthookLocalTask.NAME, LefthookYmlTask.NAME],
            group: LefthookPlugin.GROUP,
            description: 'Installs lefthook hooks'])
    }

    @TaskAction
    def runTask() {
        def binary = getLefthookBinary().getAsFile().get()
        
        // Execute lefthook install
        getExecOperations().exec { spec ->
            spec.commandLine(binary.absolutePath, "install", "-f")
            spec.workingDir = project.projectDir
        }
    }
}
