/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

public class LefthookRcTask extends DefaultTask {

    public static final String NAME = "lefthookRc"

    private Project project

    @Inject
    public LefthookRcTask(Project project) {
        this.project = project
    }

    static register(Project project) {
        project.getLogger().info("Registering task {}", NAME)
        def taskContainer = project.getTasks()

        return taskContainer.create([name: NAME,
            type: LefthookRcTask,
            dependsOn: [],
            group: LefthookPlugin.GROUP,
            description: 'Creates the lefthookrc file'])
    }

    @TaskAction
    def runTask() {
        def context = LefthookPluginHelper.createContext(project)
        LefthookRcTask.run(context)
    }

    static def run = { context ->
        def status = Optional.ofNullable(context)
            .map(x -> LefthookDownloadTask.run(x))
            .map(x -> LefthookRcTask.writeRc(x))
            //TODO should this run
            .map(x -> LefthookRcTask.command(x))
            .map(x -> Command.execute(x))
            .orElseThrow(() -> new RuntimeException("Unable to run lefthook"))
        return status
    }

    static def writeRc = Loggy.wrap( { x ->
        def binary = x.binary.getAbsolutePath()
        def rc = new File(x.location, ".lefthookrc")
        rc.withWriter { writer ->
            writer.writeLine "export LEFTHOOK_BIN=${binary}"
            writer.writeLine x.extension.rc.call()
        }
        x.rc = rc
        return x
    })

    static def writeLocal = Loggy.wrap( { x ->
        def binary = x.binary.getAbsolutePath()
        def lefthookLocal = x.project.file('lefthook-local.yml')
        def rcPath = x.rc.getAbsolutePath()
        lefthookLocal.withWriter { writer ->
            writer.writeLine "rc: ${rcPath}"
        }
        return x
    })

    static def getOut = Loggy.wrap( { x -> 
        def out = x.sout? x.sout.trim(): ""
        return out
    })

    static def command = Loggy.wrap( { x ->
        def commandParts = []
        commandParts.add(x.binary.getAbsolutePath())
        commandParts.add("install")
        commandParts.add("-f")
        x.command = commandParts.join(" ")
        return x
    } )
        

}
