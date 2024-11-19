package com.fizzpod.gradle.plugins.lefthook

import org.gradle.api.Project
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

public class LefthookLocalTask extends DefaultTask {

    public static final String NAME = "lefthookLocal"

    private Project project

    @Inject
    public LefthookLocalTask(Project project) {
        this.project = project
    }

    static register(Project project) {
        project.getLogger().info("Registering task {}", NAME)
        def taskContainer = project.getTasks()

        return taskContainer.create([name: NAME,
            type: LefthookLocalTask,
            dependsOn: [],
            group: LefthookPlugin.GROUP,
            description: 'Creates the lefthookrc file'])
    }

    @TaskAction
    def runTask() {

        def extension = project[LefthookPlugin.NAME]
        def context = [:]
        context.project = project
        context.extension = extension
        LefthookLocalTask.run(context)
    }

    static def run = { context ->
        def status = Optional.ofNullable(context)
            .map(x -> LefthookRcTask.run(x))
            .map(x -> LefthookLocalTask.writeLocal(x))
            .orElseThrow(() -> new RuntimeException("Unable to run lefthook"))
        return status
    }

    static def writeLocal = Loggy.wrap( { x ->
        def binary = x.binary.getAbsolutePath()
        def lefthookLocal = x.project.file('lefthook-local.yml')
        def rcPath = x.rc.getAbsolutePath()
        lefthookLocal.withWriter { writer ->
            writer.writeLine "rc: ${rcPath}"
        }
        return x
    })


        

}