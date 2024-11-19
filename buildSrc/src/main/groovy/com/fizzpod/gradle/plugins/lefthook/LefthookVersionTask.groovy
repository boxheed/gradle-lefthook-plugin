package com.fizzpod.gradle.plugins.lefthook

import org.gradle.api.Project
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

public class LefthookVersionTask extends DefaultTask {

    public static final String NAME = "lefthookVersion"

    private Project project

    @Inject
    public LefthookVersionTask(Project project) {
        this.project = project
    }

    static register(Project project) {
        project.getLogger().info("Registering task {}", NAME)
        def taskContainer = project.getTasks()

        return taskContainer.create([name: NAME,
            type: LefthookVersionTask,
            dependsOn: [],
            group: LefthookPlugin.GROUP,
            description: 'Outputs the current lefthook version'])
    }

    @TaskAction
    def runTask() {

        def extension = project[LefthookPlugin.NAME]
        def context = [:]
        context.project = project
        context.extension = extension
        def result = LefthookVersionTask.run(context)
        if(result.exit == 0) {
            Loggy.lifecycle("Lefthook version: \n{}", result.sout? result.sout: "No Changes")
        } else {
            Loggy.lifecycle("Lefthook version error: \n{}\n{}", result.serr, result.serr)
        }
    }

    static def run = { context ->
        def status = Optional.ofNullable(context)
            .map(x -> LefthookInstallTask.location(x))
            .map(x -> LefthookInstallTask.install(x))
            .map(x -> LefthookVersionTask.command(x))
            .map(x -> Command.execute(x))
            .orElseThrow(() -> new RuntimeException("Unable to run lefthook"))
        return status
    }

    static def getOut = Loggy.wrap( { x -> 
            def out = x.sout? x.sout.trim(): ""
            return out
        })

    static def command = Loggy.wrap( { x ->
        def commandParts = []
        commandParts.add(x.binary.getAbsolutePath())
        commandParts.add("version")
        x.command = commandParts.join(" ")
        return x
    } )
        

}