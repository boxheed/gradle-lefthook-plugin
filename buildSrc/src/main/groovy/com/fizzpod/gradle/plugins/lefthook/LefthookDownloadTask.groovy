/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

public class LefthookDownloadTask extends DefaultTask {

    public static final String NAME = "lefthookDownload"

    public static final String LEFTHOOK_INSTALL_DIR = ".lefthook"

    private Project project

    @Inject
    public LefthookDownloadTask(Project project) {
        this.project = project
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
        def context = LefthookPluginHelper.createContext(project)
        LefthookDownloadTask.run(context)
    }

    static def run = Loggy.wrap({ context ->
        return Optional.ofNullable(context)
            .map(x -> LefthookDownloadTask.location(x))
            .map(x -> LefthookDownloadTask.ttl(x))
            .map(x -> LefthookDownloadTask.download(x))
            .orElseThrow(() -> new RuntimeException("Unable to install lefthook"))
    })

    /**
    * Find the most recent binary and see if it is within ttl
    */
    static def ttl = { x ->
        def binary = x.extension.binary
        if(binary == null || !binary.exists()) {
            def location = x.location
            def arch = OS.getArch(x.extension.arch)
            def os = OS.getOs(x.extension.os)
            def ttl = x.extension.ttl
            binary = LefthookDownloadTask.resolveTtl(location, arch, os, ttl)
        }
        if(binary != null && binary.exists()) {
            x.extension.binary = binary
            x.binary = binary
        }
        return x
    }

    static def resolveTtl = {  File location, OS.Arch arch, OS.Family os, long ttl ->
        def latestBinary = null
        def currentTime = System.currentTimeMillis()
        def binaryPattern = LefthookInstallation.getBinaryName("v?(\\d+\\.\\d+\\.\\d+)", os, arch) + ".*"
        location.listFiles().each { File file ->
            if (file.name =~ binaryPattern) {
                Loggy.debug("Checking ${file.name}")
                def lastModified = file.lastModified()
                def timeDiff = currentTime - lastModified
                if (timeDiff < ttl) { 
                    Loggy.debug("${file.name} within ttl of ${ttl}")
                    if(latestBinary != null && latestBinary.lastModified() < file.lastModified()) {
                        latestBinary = file
                    } else if (latestBinary == null){
                        latestBinary = file
                    }
                }
            }
        }
        Loggy.lifecycle("found lefthook binary: ${latestBinary}")
        return latestBinary
    }

    static def download = Loggy.wrap({ x ->
        def repo = x.extension.repository
        def arch = x.extension.arch
        def os = x.extension.os
        def version = x.extension.version
        def location = x.location
        if(!x.binary || !x.binary.exists()) {
            x.binary = LefthookInstallation.install(repo, arch, os, version, location)
        }
        return x.binary? x: null
    })

    static def location = Loggy.wrap({ x ->
        def projectDir = x.project.rootDir
        def lefthookDir = x.extension.location
        x.location = new File(projectDir, lefthookDir)
        return x.location? x: null
    })
}
