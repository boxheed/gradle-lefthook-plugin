package com.fizzpod.gradle.plugins.lefthook

import org.gradle.api.Project
import org.apache.commons.io.FileUtils

public class LefthookScriptInstaller {

    private Project project
    private List<String> stack

    public LefthookScriptInstaller(Project project, List<String> stack) {
        this.project = project
        this.stack = stack
    }

    def install(def resource) {
        def res = resource
        if(resource instanceof Closure) {
            res = resource.call();
        }
        def context = LefthookPluginHelper.createContext(this.project)
        context.resource = res
        context.stack = this.stack
        return LefthookScriptInstaller.doInstall(context)
    } 

    static def guid = { ->
        return UUID.randomUUID().toString()
    }

    def reverse(def value) {
        return value.reverse()
    }

    static def doInstall = Loggy.wrap( { context ->
        def config = Optional.ofNullable(context)
            .map(x -> LefthookDownloadTask.location(x))
            .map(x -> LefthookScriptInstaller.resolveDownloader(x))
            .map(x -> LefthookScriptInstaller.resolveHookLocation(x))
            .map(x -> LefthookScriptInstaller.resolveHookFile(x))
            .map(x -> LefthookScriptInstaller.download(x))
            //.map(x -> LefthookScriptInstaller.setExecute(x))
            .map(x -> LefthookScriptInstaller.createConfig(x))
            .orElseThrow(() -> new RuntimeException("Unable to install " + resource))
        return config
    })

    static def resolveDownloader = Loggy.wrap( { x ->

        def resource = x.resource
        if(resource =~ "http.*") {
            x.downloader = LefthookScriptInstaller.httpDownloader
        } else if(resource =~ "classpath.*") {
            x.downloader = LefthookScriptInstaller.classpathDownloader
        }

        return x
    })

    static def httpDownloader = { url, file ->
        if(!file.exists()) {
            FileUtils.copyURLToFile(new URL(url), file, 120000, 120000)
        }
    }

    static def classpathDownloader = { url, file ->
        return x
    }

    static def resolveHookLocation = Loggy.wrap( { x ->
        String hookName = x.stack[0]
        x.hooklocation = new File(x.location, hookName)
        return x
    })

    static def resolveHookFile = Loggy.wrap( { x ->
        def resource = x.resource
        def pathSegments = resource.split("/")
        // Get the last segment (assuming there's at least one)
        def hookFileName = pathSegments[-1]
        x.hookFile = new File(x.hooklocation, hookFileName)
        return x
    })

    static def download = Loggy.wrap( { x ->
        x.downloader(x.resource, x.hookFile)
        //make the hookFile executable
        x.hookFile.setExecutable(true)
        return x
    })

    static def createConfig = Loggy.wrap( { x ->
        def hookName = x.hookFile.getName()
        def config = [:]

        config[hookName] = ["runner":"bash"]
    /*
            hookName: 
            x.hookFile.getName(): [
                "runner": "bash"
            ]
        ]
        */
        return hookName
      //  return x
    })

}