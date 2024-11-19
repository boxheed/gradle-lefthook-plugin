package com.fizzpod.gradle.plugins.lefthook

import org.gradle.api.Plugin
import org.gradle.api.Project

public class LefthookPlugin implements Plugin<Project> {

	public static final String NAME = "lefthook"
	public static final String GROUP = "Lefthook"
	public static final String EXE_NAME = "lefthook"

	void apply(Project project) {
		LefthookPluginExtension extension = project.extensions.create(NAME, LefthookPluginExtension)
		extension.project = project
		def downloadTask = LefthookDownloadTask.register(project)
		def downloadAllTask = LefthookDownloadAllTask.register(project)
		def versionTask = LefthookVersionTask.register(project)
		def helpTask = LefthookHelpTask.register(project)
		def rcTask = LefthookRcTask.register(project)
		def localTask = LefthookLocalTask.register(project)
		def installTask = LefthookInstallTask.register(project)
		def installAllTask = LefthookInstallAllTask.register(project)
	}
}