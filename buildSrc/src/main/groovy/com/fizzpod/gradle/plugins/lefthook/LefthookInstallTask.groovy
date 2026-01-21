/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

public abstract class LefthookInstallTask extends DefaultTask {

    public static final String NAME = "lefthookInstall"

    @InputFile
    abstract RegularFileProperty getLefthookBinary()

    @InputFile
    abstract RegularFileProperty getLefthookConfigFile()

    @InputFile
    abstract RegularFileProperty getLefthookLocalFile()
    
    @InputFile
    abstract RegularFileProperty getLefthookRcFile()

    @OutputDirectory
    abstract DirectoryProperty getGitHooksDir()

    @OutputFile
    abstract RegularFileProperty getLefthookChecksumFile()
    
    @Inject
    abstract ExecOperations getExecOperations()

    @Inject
    public LefthookInstallTask(Project project) {
        getGitHooksDir().convention(project.layout.projectDirectory.dir(".git/hooks"))
        getLefthookChecksumFile().convention(project.layout.projectDirectory.file("lefthook.checksum"))
    }

    static register(Project project) {
        project.getLogger().info("Registering task {}", NAME)
        def taskContainer = project.getTasks()

        return taskContainer.create([name: NAME,
            type: LefthookInstallTask,
            dependsOn: [],
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
    
    // Kept for backward compatibility
    static def run = Loggy.wrap({ context ->
        return Optional.ofNullable(context)
            .map(x -> LefthookDownloadTask.run(x))
            .map(x -> LefthookRcTask.run(x))
            .map(x -> LefthookLocalTask.run(x))
            .map(x -> LefthookInitTask.run(x))
            .orElseThrow(() -> new RuntimeException("Unable to install lefthook"))
    })
}
