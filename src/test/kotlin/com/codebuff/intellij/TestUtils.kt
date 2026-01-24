package com.codebuff.intellij

import java.io.File

object TestUtils {

    fun loadPluginXml(): String {
        return File("src/main/resources/META-INF/plugin.xml").readText()
    }

    fun loadBuildGradle(): String {
        return File("build.gradle.kts").readText()
    }
}
