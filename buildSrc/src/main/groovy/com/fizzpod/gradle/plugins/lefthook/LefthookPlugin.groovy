/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import org.gradle.api.Plugin
import org.gradle.api.Project

public class LefthookPlugin implements Plugin<Project> {

	public static final String NAME = "lefthook"
	public static final String GROUP = "Lefthook"
	public static final String EXE_NAME = "lefthook"

	void apply(Project project) {
		def extension = createExtension(project)
		def downloadTask = LefthookDownloadTask.register(project)
		def downloadAllTask = LefthookDownloadAllTask.register(project)
		def versionTask = LefthookVersionTask.register(project)
		def helpTask = LefthookHelpTask.register(project)
		def rcTask = LefthookRcTask.register(project)
		def initTask = LefthookInitTask.register(project)
		def localTask = LefthookLocalTask.register(project)
		def installTask = LefthookInstallTask.register(project)
        
        rcTask.configure { task ->
            task.getLefthookBinary().set(downloadTask.getLefthookLocation().map { directory ->
                def dirFile = directory.getAsFile()
                def os = OS.getOs(null)
                def arch = OS.getArch(null)
                def binary = LefthookInstallation.findBinary(dirFile, os, arch)
                if(binary == null) {
                    throw new RuntimeException("Lefthook binary not found in " + dirFile)
                }
                return directory.file(binary.name)
            })
        }
        
        localTask.configure { task ->
            task.getLefthookRcFile().set(rcTask.getLefthookRcFile())
        }
        
        initTask.configure { task ->
            task.getConfig().set(extension.getConfig())
        }
        
        installTask.configure { task ->
            task.getLefthookBinary().set(rcTask.getLefthookBinary())
            task.getLefthookConfigFile().set(initTask.getLefthookConfigFile())
            task.getLefthookLocalFile().set(localTask.getLefthookLocalFile())
            task.getLefthookRcFile().set(rcTask.getLefthookRcFile())
        }

		project.afterEvaluate { proj -> 
			def autoInstall = extension.getAutoInstall().get();
			def autoTaskName = extension.getAutoTaskName().get()
			Loggy.debug("Auto installing lefthook: {}", autoInstall)
			if(autoInstall) {
				Loggy.lifecycle("Auto installing lefthook")
				def checkTask = proj.tasks.findByName(autoTaskName)
				if(checkTask) {
					checkTask.dependsOn installTask
				} else {
					Loggy.warn("Lefthook auto install requested, but '${autoTaskName}' task not found. Please ensure the 'check' task exists or manually call 'lefthookInstall'.")
				}
			}
			//Loggy.debug("config {}", config)
		}
	}

	private LefthookPluginExtension createExtension(project) {
		return project.extensions.create(NAME, LefthookPluginExtension)
    }
}