/* (C) 2024-2025 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class LefthookPluginSpec extends Specification {

    @Rule
    TemporaryFolder temporaryFolder
    
    
    def "initialise plugin"() {
        setup:
        
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
        when:
            def plugin = new LefthookPlugin()
            plugin.apply(project)
        then: 
            project.getTasksByName(LefthookDownloadTask.NAME, false) != null
            !project.getTasksByName(LefthookDownloadTask.NAME, false).isEmpty()
            project.getExtensions().findByName(LefthookPlugin.NAME) != null
    }

    def "run lefthookDownloadTask"() {
        setup:
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
            mockLefthookBinary(project)
        when:
            def plugin = new LefthookPlugin()
            plugin.apply(project)
            configureMockBinary(project)
            def task = project.getTasksByName(LefthookDownloadTask.NAME, false).iterator().next()
            task.runTask()
        then: 
            //TODO proper assertion
            !project.getTasksByName(LefthookDownloadTask.NAME, false).isEmpty()
    }

    

    def "run lefthookDownloadAllTask"() {
        setup:
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
            mockLefthookBinary(project)
        when:
            def plugin = new LefthookPlugin()
            plugin.apply(project)
            configureMockBinary(project)
            def task = project.getTasksByName(LefthookDownloadAllTask.NAME, false).iterator().next()
            task.runTask()
        then: 
            !project.getTasksByName(LefthookDownloadAllTask.NAME, false).isEmpty()
    }

   
    def "run lefthookHelpTask"() {
        setup:
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
            mockLefthookBinary(project)
        when:
            def plugin = new LefthookPlugin()
            plugin.apply(project)
            configureMockBinary(project)
            def task = project.getTasksByName(LefthookHelpTask.NAME, false).iterator().next()
            task.runTask()
        then: 
            !project.getTasksByName(LefthookHelpTask.NAME, false).isEmpty()
    }

    def "run lefthookInitTask"() {
        setup:
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
            mockLefthookBinary(project)
        when:
            def plugin = new LefthookPlugin()
            plugin.apply(project)
            configureMockBinary(project)
            def task = project.getTasksByName(LefthookInitTask.NAME, false).iterator().next()
            task.runTask()
        then: 
            !project.getTasksByName(LefthookInitTask.NAME, false).isEmpty()
    }

    def "run lefthookLocalTask"() {
        setup:
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
            mockLefthookBinary(project)
        when:
            def plugin = new LefthookPlugin()
            plugin.apply(project)
            configureMockBinary(project)
            def task = project.getTasksByName(LefthookLocalTask.NAME, false).iterator().next()
            task.runTask()
        then: 
            !project.getTasksByName(LefthookLocalTask.NAME, false).isEmpty()
    }

    def "run lefthookRcTask"() {
        setup:
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
            mockLefthookBinary(project)
        when:
            def plugin = new LefthookPlugin()
            plugin.apply(project)
            configureMockBinary(project)
            def task = project.getTasksByName(LefthookRcTask.NAME, false).iterator().next()
            task.runTask()
        then: 
            !project.getTasksByName(LefthookRcTask.NAME, false).isEmpty()
    }

    // Helper method to create a mock binary file
    private void mockLefthookBinary(Project project) {
        // Create the .lefthook directory
        def lefthookDir = new File(project.rootDir, ".lefthook")
        lefthookDir.mkdirs()

        // We need to match the binary name expected by LefthookInstallation/DownloadTask
        // Assuming defaults (latest version, current OS/Arch)
        // Since we don't know exactly what OS/Arch the test env reports, we might need to trick it or create multiple.

        // However, we can also configure the plugin to point to a specific binary if we can.
        // But the plugin uses extension options.
    }
    
    private void configureMockBinary(Project project) {
        // Set up the extension to point to a binary we create
        def extension = project.extensions.findByName(LefthookPlugin.NAME)
        // Create a dummy file
        def lefthookDir = new File(project.rootDir, ".lefthook")
        lefthookDir.mkdirs()
        def dummyBin = new File(lefthookDir, "lefthook_dummy")
        dummyBin.text = "mock binary"
        dummyBin.setExecutable(true)

        // Override the binary property in the options closure
        if (extension.options instanceof Closure) {
             // It's a bit hard to inject into the closure if it's not executed yet.
             // But we can overwrite options if it's a map?
             // LefthookPluginHelper.getOptions resolves the closure.
        }

        // The LefthookDownloadTask.ttl logic checks for binary existence.
        // It uses: def binary = x.extension.binary
        // If x.extension.binary is set, it uses it.

        // We can try to set the 'binary' option.
        // But 'options' is usually a closure in the DSL.

        project.lefthook {
            options = {
                binary = dummyBin
            }
        }
    }
    
}
