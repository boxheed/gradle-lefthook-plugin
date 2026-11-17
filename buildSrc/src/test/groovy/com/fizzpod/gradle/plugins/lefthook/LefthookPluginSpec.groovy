/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import okhttp3.*
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class LefthookPluginSpec extends Specification {

    @Rule
    TemporaryFolder temporaryFolder

    private Call.Factory originalClient
    private Call.Factory mockClient

    def setup() {
        originalClient = HttpClientProvider.getClient()
        mockClient = Mock(Call.Factory)
        HttpClientProvider.setClient(mockClient)
    }

    def cleanup() {
        HttpClientProvider.setClient(originalClient)
    }

    private Response createResponse(int code, String content, String mediaTypeStr = "application/json") {
        MediaType mediaType = MediaType.parse(mediaTypeStr)
        ResponseBody body = ResponseBody.create(content, mediaType)
        Request request = new Request.Builder().url("https://api.github.com").build()
        return new Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message(code == 200 ? "OK" : "Error")
            .body(body)
            .build()
    }
    
    def "warmup"() {
        when:
        try {
             ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
        } catch (Throwable t) {
            // ignore
        }
        then:
        true
    }
    
    def "initialise plugin"() {
        setup:
        
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
        when:
            def plugin = new LefthookPlugin()
            plugin.apply(project)
        then: 
            project.getTasksByName(LefthookDownloadTask.NAME, false) != null
            !project.getTasksByName(LefthookDownloadTask.NAME, false).isEmpty()
            project.getTasksByName(LefthookResolveVersionTask.NAME, false) != null
            !project.getTasksByName(LefthookResolveVersionTask.NAME, false).isEmpty()
            project.getExtensions().findByName(LefthookPlugin.NAME) != null
    }

    def "run lefthookDownloadTask downloads binary when missing"() {
        setup:
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
            def plugin = new LefthookPlugin()
            plugin.apply(project)

            def versionFile = new File(temporaryFolder.getRoot(), ".lefthook/version.txt")
            versionFile.parentFile.mkdirs()
            versionFile.text = "v1.9.10"

            def os = OS.getOs(null)
            def arch = OS.getArch(null)
            def releaseJson = """{
                "tag_name": "v1.9.10",
                "assets": [
                    {
                        "name": "lefthook_1.9.10_${os.id}_${arch.id}",
                        "browser_download_url": "https://example.com/lefthook_1.9.10_${os.id}_${arch.id}"
                    }
                ]
            }"""

            mockClient.newCall(_ as Request) >> { Request req ->
                Call call = Mock(Call)
                if (req.url().toString().contains("api.github.com")) {
                    call.execute() >> createResponse(200, releaseJson)
                } else {
                    call.execute() >> createResponse(200, "binary-bytes", "application/octet-stream")
                }
                return call
            }

            def task = project.tasks.named(LefthookDownloadTask.NAME, LefthookDownloadTask).get()

        when:
            task.runTask()

        then:
            def binary = task.getLefthookBinary().get().asFile
            binary.exists()
            binary.text == "binary-bytes"
            binary.canExecute()
    }

    def "run lefthookDownloadTask skips download when binary already exists"() {
        setup:
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
            def plugin = new LefthookPlugin()
            plugin.apply(project)

            def versionFile = new File(temporaryFolder.getRoot(), ".lefthook/version.txt")
            versionFile.parentFile.mkdirs()
            versionFile.text = "v1.9.11"

            def os = OS.getOs(null)
            def arch = OS.getArch(null)
            def binary = new File(temporaryFolder.getRoot(), ".lefthook/" + LefthookInstallation.getBinaryName("v1.9.11", os, arch))
            binary.text = "existing-binary"

            def task = project.tasks.named(LefthookDownloadTask.NAME, LefthookDownloadTask).get()

        when:
            task.runTask()

        then:
            0 * mockClient.newCall(_)
            binary.text == "existing-binary"
    }

    def "run lefthookDownloadTask fails when version file is missing"() {
        setup:
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
            def plugin = new LefthookPlugin()
            plugin.apply(project)
            def task = project.tasks.named(LefthookDownloadTask.NAME, LefthookDownloadTask).get()

        when:
            task.runTask()

        then:
            thrown(FileNotFoundException)
    }

    def "run lefthookDownloadAllTask downloads all platform binaries"() {
        setup:
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
            def plugin = new LefthookPlugin()
            plugin.apply(project)

            def versionFile = new File(temporaryFolder.getRoot(), ".lefthook/version.txt")
            versionFile.parentFile.mkdirs()
            versionFile.text = "v1.9.12"

            def releaseJson = '''{
                "tag_name": "v1.9.12",
                "assets": [
                    {"name": "lefthook_1.9.12_Linux_x86_64", "browser_download_url": "https://example.com/linux_amd64"},
                    {"name": "lefthook_1.9.12_Linux_arm64", "browser_download_url": "https://example.com/linux_arm64"},
                    {"name": "lefthook_1.9.12_MacOS_x86_64", "browser_download_url": "https://example.com/mac_amd64"},
                    {"name": "lefthook_1.9.12_MacOS_arm64", "browser_download_url": "https://example.com/mac_arm64"},
                    {"name": "lefthook_1.9.12_Windows_x86_64", "browser_download_url": "https://example.com/win_amd64"}
                ]
            }'''

            mockClient.newCall(_ as Request) >> { Request req ->
                Call call = Mock(Call)
                if (req.url().toString().contains("api.github.com")) {
                    call.execute() >> createResponse(200, releaseJson)
                } else {
                    call.execute() >> createResponse(200, "platform-binary", "application/octet-stream")
                }
                return call
            }

            def task = project.tasks.named(LefthookDownloadAllTask.NAME, LefthookDownloadAllTask).get()

        when:
            task.runTask()

        then:
            new File(temporaryFolder.getRoot(), ".lefthook/lefthook_v1.9.12_Linux_x86_64").exists()
            new File(temporaryFolder.getRoot(), ".lefthook/lefthook_v1.9.12_Linux_arm64").exists()
            new File(temporaryFolder.getRoot(), ".lefthook/lefthook_v1.9.12_MacOS_x86_64").exists()
            new File(temporaryFolder.getRoot(), ".lefthook/lefthook_v1.9.12_MacOS_arm64").exists()
            new File(temporaryFolder.getRoot(), ".lefthook/lefthook_v1.9.12_Windows_x86_64.exe").exists()
    }

    def "run lefthookDownloadAllTask propagates API errors"() {
        setup:
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
            def plugin = new LefthookPlugin()
            plugin.apply(project)

            def versionFile = new File(temporaryFolder.getRoot(), ".lefthook/version.txt")
            versionFile.parentFile.mkdirs()
            versionFile.text = "v1.9.13"

            mockClient.newCall(_ as Request) >> {
                Call call = Mock(Call)
                call.execute() >> createResponse(500, "Internal Server Error", "text/plain")
                return call
            }

            def task = project.tasks.named(LefthookDownloadAllTask.NAME, LefthookDownloadAllTask).get()

        when:
            task.runTask()

        then:
            def e = thrown(Exception)
            (e instanceof IOException ? e : e.cause) instanceof IOException
    }

   
    def "run lefthookHelpTask"() {
        setup:
            def os = OS.getOs(null)
            def arch = OS.getArch(null)
            def binaryName = LefthookInstallation.getBinaryName("v1.0.0", os, arch)
            def lefthookDir = new File(temporaryFolder.getRoot(), ".lefthook")
            def binary = new File(lefthookDir, binaryName)
            binary.createParentDirectories()
            binary.text = """#!/bin/sh
                echo "bananas" """
            binary.setExecutable(true)

            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
                when:
                    def plugin = new LefthookPlugin()
                    plugin.apply(project)
                    def task = project.getTasksByName(LefthookHelpTask.NAME, false).iterator().next()
                    task.getLefthookBinary().set(binary)
                    task.runTask()
                then:              !project.getTasksByName(LefthookHelpTask.NAME, false).isEmpty()
    }

    def "run lefthookInstallTask"() {
        setup:
            def os = OS.getOs(null)
            def arch = OS.getArch(null)
            def binaryName = LefthookInstallation.getBinaryName("v1.0.0", os, arch)
            def lefthookDir = new File(temporaryFolder.getRoot(), ".lefthook")
            def binary = new File(lefthookDir, binaryName)
            binary.createParentDirectories()
            binary.text = """#!/bin/sh
                echo "bananas" """
            binary.setExecutable(true)
        
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
        when:
            def plugin = new LefthookPlugin()
            plugin.apply(project)
            def task = project.getTasksByName(LefthookInstallTask.NAME, false).iterator().next()
            task.getLefthookBinary().set(binary)
            task.runTask()
        then: 
            !project.getTasksByName(LefthookInstallTask.NAME, false).isEmpty()
    }

    def "run lefthookLocalTask"() {
        setup:
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
        when:
            def plugin = new LefthookPlugin()
            plugin.apply(project)
            def task = project.tasks.named(LefthookLocalTask.NAME, LefthookLocalTask).get()
            task.runTask()
        then:
            def localFile = task.getLefthookLocalFile().get().asFile
            localFile.exists()
            localFile.text.contains("rc: ")
            localFile.text.contains(".lefthookrc")
    }

    def "run lefthookRcTask"() {
        setup:
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
            def lefthookDir = new File(temporaryFolder.getRoot(), ".lefthook")
            def binary = new File(lefthookDir, "lefthook_v1.0.0_Linux_amd64")
            binary.createParentDirectories()
            binary.text = "dummy content"
            binary.setExecutable(true)
        when:
            def plugin = new LefthookPlugin()
            plugin.apply(project)
            project.extensions.getByType(LefthookPluginExtension).rc.set("export FOO=bar")
            def task = project.tasks.named(LefthookRcTask.NAME, LefthookRcTask).get()
            task.setLefthookBinary(binary)
            task.runTask()
        then:
            def rcFile = task.getLefthookRcFile().get().asFile
            rcFile.exists()
            rcFile.text.contains("export LEFTHOOK_BIN=${binary.absolutePath}")
            rcFile.text.contains("export FOO=bar")
    }

    def "run lefthookRcTask throws when lefthookBinary is not set"() {
        setup:
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
            def plugin = new LefthookPlugin()
            plugin.apply(project)
            def task = project.tasks.named(LefthookRcTask.NAME, LefthookRcTask).get()
            task.getLefthookBinary().set((File) null)

        when:
            task.runTask()

        then:
            thrown(Exception)
    }

    def "run lefthookYmlTask resolves config closures only at execution time and writes yaml"() {
        setup:
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
            def plugin = new LefthookPlugin()
            plugin.apply(project)
            boolean closureEvaluated = false
            project.extensions.getByType(LefthookPluginExtension).config.set([
                "pre-commit": [
                    "commands": [
                        {
                            closureEvaluated = true
                            return "lint"
                        }: ["run": "./gradlew spotlessCheck"]
                    ]
                ]
            ])
            def task = project.tasks.named(LefthookYmlTask.NAME, LefthookYmlTask).get()

        expect:
            !closureEvaluated

        when:
            task.runTask()

        then:
            closureEvaluated
            def ymlFile = task.getLefthookConfigFile().get().asFile
            ymlFile.exists()
            ymlFile.text.contains("pre-commit:")
            ymlFile.text.contains("lint:")
            ymlFile.text.contains("./gradlew spotlessCheck")
    }

    def "run lefthookYmlTask throws when lefthookLocation is not set"() {
        setup:
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
            def plugin = new LefthookPlugin()
            plugin.apply(project)
            def task = project.tasks.named(LefthookYmlTask.NAME, LefthookYmlTask).get()
            task.getLefthookLocation().convention((Directory) null)
            task.getLefthookLocation().set((File) null)

        when:
            task.runTask()

        then:
            thrown(Exception)
    }

    def "run lefthookVersionTask"() {
        setup:
            def os = OS.getOs(null)
            def arch = OS.getArch(null)
            def binaryName = LefthookInstallation.getBinaryName("v1.0.0", os, arch)
            def lefthookDir = new File(temporaryFolder.getRoot(), ".lefthook")
            def binary = new File(lefthookDir, binaryName)
            binary.createParentDirectories()
            binary.text = "#!/bin/sh\necho 1.0.0\n"
            binary.setExecutable(true)

            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
            def plugin = new LefthookPlugin()
            plugin.apply(project)
            def task = project.tasks.named(LefthookVersionTask.NAME, LefthookVersionTask).get()
            task.getLefthookBinary().set(binary)

        when:
            task.runTask()

        then:
            noExceptionThrown()
    }

    def "autoInstall attaches lefthookInstall to configured task when task exists"() {
        setup:
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
            def assembleTask = project.tasks.register("assemble").get()
            def plugin = new LefthookPlugin()
            plugin.apply(project)
            project.extensions.getByType(LefthookPluginExtension).autoInstall.set(true)

        when:
            project.evaluate()

        then:
            assembleTask.taskDependencies.getDependencies(assembleTask).any { it.name == LefthookInstallTask.NAME }
    }

    def "autoInstall does not fail when configured autoTaskName does not exist"() {
        setup:
            Project project = ProjectBuilder.builder().withProjectDir(temporaryFolder.getRoot()).build()
            def plugin = new LefthookPlugin()
            plugin.apply(project)
            def ext = project.extensions.getByType(LefthookPluginExtension)
            ext.autoInstall.set(true)
            ext.autoTaskName.set("missingTask")

        when:
            project.evaluate()

        then:
            noExceptionThrown()
    }
    
    
    
}
