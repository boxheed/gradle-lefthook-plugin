package com.fizzpod.gradle.plugins.lefthook

import org.gradle.api.Project
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject
import org.yaml.snakeyaml.Yaml

public class LefthookInitTask extends DefaultTask {

    public static final String NAME = "lefthookInit"

    private Project project

    @Inject
    public LefthookInitTask(Project project) {
        this.project = project
    }

    static register(Project project) {
        project.getLogger().info("Registering task {}", NAME)
        def taskContainer = project.getTasks()

        return taskContainer.create([name: NAME,
            type: LefthookInitTask,
            dependsOn: [],
            group: LefthookPlugin.GROUP,
            description: 'Installs/creates the lefthook.yml file'])
    }

    @TaskAction
    def runTask() {
        def context = LefthookPluginHelper.createContext(project)
        def result = LefthookInitTask.run(context)
        if(result.exit == 0) {
            Loggy.lifecycle("Lefthook init: \n{}", result.sout? result.sout: "No Changes")
        } else {
            Loggy.lifecycle("Lefthook init error: \n{}\n{}", result.serr, result.serr)
        }
    }

    static def run = { context ->
        def status = Optional.ofNullable(context)
            .map(x -> LefthookDownloadTask.run(x))
            .map(x -> LefthookRcTask.run(x))
            .map(x -> LefthookInitTask.writeLocal(x))
            .map(x -> LefthookInitTask.writeConfig(x))
            .map(x -> LefthookInitTask.command(x))
            .map(x -> Command.execute(x))
            .orElseThrow(() -> new RuntimeException("Unable to run lefthook"))
        return status
    }

    static def writeRc = Loggy.wrap( { x ->
        def binary = x.binary.getAbsolutePath()
        def rc = new File(x.location, ".lefthookrc")
        def lefthookLocal = x.project.file('lefthook-local.yml')
        rc.withWriter { writer ->
            writer.writeLine "export LEFTHOOK_BIN=${binary}"
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

    static def writeConfig = Loggy.wrap( { x ->
        def binary = x.binary.getAbsolutePath()
        def lefthookLocal = x.project.file('lefthook.yml')
        def rcPath = x.rc.getAbsolutePath()
        lefthookLocal.withWriter { writer ->
            Yaml yaml = new Yaml()
            yaml.dump(x.config, writer)
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