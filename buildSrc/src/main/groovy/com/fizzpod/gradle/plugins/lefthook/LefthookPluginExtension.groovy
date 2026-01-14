/* (C) 2024 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.MapProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty

public abstract class LefthookPluginExtension implements GroovyInterceptable {
    /*
    static def DEFAULT_OPTIONS = [
        "version": "latest",
        "location": ".lefthook",
        "repository": "evilmartians/lefthook",
        "prefix": "v",
        "os": null,
        "arch": null,
        "project": null,
        "autoInstall": true,
        "rc": {""},
        "ttl": 1000 * 60 * 60 * 24,
        "binary": null
    ]
    */

    @Inject
    protected abstract Project getProject()

    // Abstract property for Gradle to manage
    abstract MapProperty<String, Object> getConfig()
    abstract Property<Boolean> getAutoInstall()
    abstract Property<String> getAutoTaskName()
    abstract Property<String> getVersion()
    abstract DirectoryProperty getLocation()
    abstract Property<String> getRepository()
    abstract Property<String> getRc()

    @Inject
    public LefthookPluginExtension(ObjectFactory objects) {
        // Set convention for the managed property
        getConfig().convention([:])
        getAutoInstall().convention(false)
        getAutoTaskName().convention("check")
        getVersion().convention("latest")
        getLocation().convention(project.layout.projectDirectory.dir(".lefthook"))
        getRepository().convention("evilmartians/lefthook")
        getRc().convention("")
    }
}
