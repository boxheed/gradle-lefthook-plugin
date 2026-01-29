/* (C) 2024-2026 */
/* SPDX-License-Identifier: Apache-2.0 */
package com.fizzpod.gradle.plugins.lefthook

import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

public abstract class LefthookPluginExtension implements GroovyInterceptable {
    
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
        getAutoTaskName().convention("assemble")
        getVersion().convention("latest")
        getLocation().convention(getProject().layout.projectDirectory.dir(".lefthook"))
        getRepository().convention("evilmartians/lefthook")
        getRc().convention("")
    }
}
