/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class LefthookResolveVersionTaskSpec extends Specification {

    @Rule
    TemporaryFolder temporaryFolder

    Project project
    LefthookResolveVersionTask task

    def setup() {
        project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
        project.plugins.apply(LefthookPlugin)
        task = project.tasks.findByName(LefthookResolveVersionTask.NAME)
    }

    def "initialise task"() {
        expect:
            task.getLefthookVersion().get() == "latest"
            task.getLefthookRepository().get() == "evilmartians/lefthook"
            task.getTtl().get() == 86400000L
            task.getVersionLocation().get().asFile == new File(temporaryFolder.getRoot(), ".lefthook")
            task.getResolvedVersionFile().get().asFile == new File(temporaryFolder.getRoot(), ".lefthook/version.txt")
    }

    def "run task with fixed version"() {
        setup:
            task.getLefthookVersion().set("1.5.0")
        when:
            task.runTask()
        then:
            File versionFile = task.getResolvedVersionFile().get().asFile
            versionFile.exists()
            versionFile.text == "1.5.0"
    }

    def "run task with latest version"() {
        setup:
            task.getLefthookVersion().set("latest")
        when:
            task.runTask()
        then:
            File versionFile = task.getResolvedVersionFile().get().asFile
            versionFile.exists()
            versionFile.text != null
            versionFile.text.length() > 0
    }

    def "run task uses cached version within TTL"() {
        setup:
            task.getLefthookVersion().set("1.6.0")
            File versionFile = task.getResolvedVersionFile().get().asFile
            versionFile.parentFile.mkdirs()
            versionFile.text = "old-version"
            // File is fresh, so it should be within TTL
        when:
            task.runTask()
        then:
            versionFile.text == "old-version"
    }

    def "run task refreshes version after TTL expires"() {
        setup:
            task.getLefthookVersion().set("1.7.0")
            File versionFile = task.getResolvedVersionFile().get().asFile
            versionFile.parentFile.mkdirs()
            versionFile.text = "old-version"
            versionFile.setLastModified(System.currentTimeMillis() - 86400001L) // 1 day + 1 ms ago
        when:
            task.runTask()
        then:
            versionFile.text == "1.7.0"
    }

}
