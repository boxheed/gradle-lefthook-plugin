/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault

@CacheableTask
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
    abstract ProjectLayout getLayout()

    @Inject
    public LefthookInstallTask(ProjectLayout layout) {
        getGitHooksDir().convention(layout.projectDirectory.dir(".git/hooks"))
    }

    static register(Project project) {
        project.getLogger().info("Registering task {}", NAME)
        return project.getTasks().register(NAME, LefthookInstallTask) { task ->
            task.dependsOn(LefthookRcTask.NAME, LefthookLocalTask.NAME, LefthookYmlTask.NAME)
            task.group = LefthookPlugin.GROUP
            task.description = 'Installs the lefthook hooks into the git repository'
        }
    }

    @TaskAction
    def runTask() {
        def binary = getLefthookBinary().getAsFile().get()
        
        logger.lifecycle("Installing Lefthook Git hooks...")
        // Execute lefthook install
        getExecOperations().exec { spec ->
            spec.commandLine(binary.absolutePath, "install", "-f")
            spec.workingDir = getLayout().getProjectDirectory().getAsFile()
        }
        logger.lifecycle("Lefthook Git hooks successfully installed!")
    }
}
