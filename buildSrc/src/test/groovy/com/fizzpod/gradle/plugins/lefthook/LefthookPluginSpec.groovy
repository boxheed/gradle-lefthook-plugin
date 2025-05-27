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
        when:
            def plugin = new LefthookPlugin()
            plugin.apply(project)
            def task = project.getTasksByName(LefthookDownloadTask.NAME, false).iterator().next()
            task.runTask()
        then: 
            //TODO proper assertion
            !project.getTasksByName(LefthookDownloadTask.NAME, false).isEmpty()
    }

    

    def "run lefthookDownloadAllTask"() {
        setup:
        
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
        when:
            def plugin = new LefthookPlugin()
            plugin.apply(project)
            def task = project.getTasksByName(LefthookDownloadAllTask.NAME, false).iterator().next()
            task.runTask()
        then: 
            !project.getTasksByName(LefthookDownloadAllTask.NAME, false).isEmpty()
    }

   
    def "run lefthookHelpTask"() {
        setup:
        
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
        when:
            def plugin = new LefthookPlugin()
            plugin.apply(project)
            def task = project.getTasksByName(LefthookHelpTask.NAME, false).iterator().next()
            task.runTask()
        then: 
            !project.getTasksByName(LefthookHelpTask.NAME, false).isEmpty()
    }

    def "run lefthookInitTask"() {
        setup:
        
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
        when:
            def plugin = new LefthookPlugin()
            plugin.apply(project)
            def task = project.getTasksByName(LefthookInitTask.NAME, false).iterator().next()
            task.runTask()
        then: 
            !project.getTasksByName(LefthookInitTask.NAME, false).isEmpty()
    }

    def "run lefthookLocalTask"() {
        setup:
        
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
        when:
            def plugin = new LefthookPlugin()
            plugin.apply(project)
            def task = project.getTasksByName(LefthookLocalTask.NAME, false).iterator().next()
            task.runTask()
        then: 
            !project.getTasksByName(LefthookLocalTask.NAME, false).isEmpty()
    }

    def "run lefthookRcTask"() {
        setup:
        
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
        when:
            def plugin = new LefthookPlugin()
            plugin.apply(project)
            def task = project.getTasksByName(LefthookRcTask.NAME, false).iterator().next()
            task.runTask()
        then: 
            !project.getTasksByName(LefthookRcTask.NAME, false).isEmpty()
    }

    
    
    
}
