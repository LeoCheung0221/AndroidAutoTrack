package com.tufusi.track.transform.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by LeoCheung on 2020/12/2.
 * @description
 */
class TufusiDataAsmPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        TufusiDataExtension extension = project.extensions.create("tufusiAsmTrack", TufusiDataExtension)

        boolean enableTufusiDataAsmPlugin = true
        Properties properties = new Properties()
        if (project.rootProject.file('gradle.properties').exists()) {
            properties.load(project.rootProject.file('gradle.properties').newDataInputStream())
            enableTufusiDataAsmPlugin = Boolean.parseBoolean(properties.getProperty("tufusiAsmTrack.enablePlugin", "true"))
        }

        if (enableTufusiDataAsmPlugin) {
            println "This is Tufusi Track ASM Gradle Plugin"

            AppExtension appExtension = project.extensions.findByType(AppExtension.class)
            appExtension.registerTransform(new TufusiDataTransform(project, extension))
        } else {
            println("------------已关闭TUFUSI#ASM插件--------------")
        }
    }
}
