/* (C) 2024-2025 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import org.gradle.api.Plugin
import org.gradle.api.Project

public class LefthookPlugin implements Plugin<Project> {

	public static final String NAME = "lefthook"
	public static final String GROUP = "Lefthook"
	public static final String EXE_NAME = "lefthook"

	void apply(Project project) {
		createExtension(project)
		def downloadTask = LefthookDownloadTask.register(project)
		def downloadAllTask = LefthookDownloadAllTask.register(project)
		def versionTask = LefthookVersionTask.register(project)
		def helpTask = LefthookHelpTask.register(project)
		def rcTask = LefthookRcTask.register(project)
		def initTask = LefthookInitTask.register(project)
		def localTask = LefthookLocalTask.register(project)
		def installTask = LefthookInstallTask.register(project)

		project.afterEvaluate { proj -> 
			def options = LefthookPluginHelper.getOptions(proj)
			def config = LefthookPluginHelper.getConfig(proj)
			def autoInstall = options.autoInstall
			Loggy.debug("Lefthook Options: {}", options)
			Loggy.debug("Auto installing lefthook: {}", autoInstall)
			if(autoInstall) {
				Loggy.lifecycle("Auto installing lefthook")
				installTask.runTask()
			}
			Loggy.debug("config {}", config)
		}
	}

	private void createExtension(project) {
		project.extensions.create(NAME, LefthookPluginExtension)
    }
}
