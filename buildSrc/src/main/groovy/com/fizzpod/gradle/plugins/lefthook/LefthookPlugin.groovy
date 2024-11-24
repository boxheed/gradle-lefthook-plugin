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
		def installTask = LefthookInstallTask.register(project)
	}
}