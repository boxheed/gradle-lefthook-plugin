/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import java.util.concurrent.Callable

public class LefthookPlugin implements Plugin<Project> {

	public static final String NAME = "lefthook"
	public static final String GROUP = "Lefthook"
	public static final String EXE_NAME = "lefthook"

	void apply(Project project) {
		def providers = project.getProviders()
		def extension = createExtension(project)
		def downloadTask = LefthookDownloadTask.register(project)
		def downloadAllTask = LefthookDownloadAllTask.register(project)
		def binaryTask = LefthookBinaryTask.register(project)
		def versionTask = LefthookVersionTask.register(project)
		def helpTask = LefthookHelpTask.register(project)
		def rcTask = LefthookRcTask.register(project)
		def localTask = LefthookLocalTask.register(project)
		def ymlTask = LefthookYmlTask.register(project)
		def installTask = LefthookInstallTask.register(project)

		versionTask.configure { task ->
			task.getLefthookBinary().set(binaryTask.getLefthookBinary())
        }

		helpTask.configure { task ->
			task.getLefthookBinary().set(binaryTask.getLefthookBinary())
        }

        rcTask.configure { task ->
           task.getLefthookBinary().set(binaryTask.getLefthookBinary())
        }
        
        localTask.configure { task ->
            task.getLefthookRcFile().set(rcTask.getLefthookRcFile())
        }
        
        ymlTask.configure { task ->
            task.getConfig().set(extension.getConfig())
        }
        
        installTask.configure { task ->
            task.getLefthookBinary().set(rcTask.getLefthookBinary())
            task.getLefthookConfigFile().set(ymlTask.getLefthookConfigFile())
            task.getLefthookLocalFile().set(localTask.getLefthookLocalFile())
            task.getLefthookRcFile().set(rcTask.getLefthookRcFile())
        }
        
        versionTask.configure { task ->
//            task.getLefthookBinary().set(rcTask.getLefthookBinary())
        }

		project.afterEvaluate { proj -> 
			def autoInstall = extension.getAutoInstall().get()
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
		}
	}

	private LefthookPluginExtension createExtension(project) {
		return project.extensions.create(NAME, LefthookPluginExtension)
    }
}
