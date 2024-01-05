// Copyright 2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.intellij.platform.gradle.artifacts.transform

import com.jetbrains.plugin.structure.base.plugin.PluginCreationFail
import com.jetbrains.plugin.structure.base.plugin.PluginCreationSuccess
import com.jetbrains.plugin.structure.base.problems.PluginProblem
import com.jetbrains.plugin.structure.intellij.plugin.IdePlugin
import com.jetbrains.plugin.structure.intellij.plugin.IdePluginManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.transform.InputArtifact
import org.gradle.api.artifacts.transform.TransformAction
import org.gradle.api.artifacts.transform.TransformOutputs
import org.gradle.api.artifacts.transform.TransformParameters
import org.gradle.api.artifacts.type.ArtifactTypeDefinition
import org.gradle.api.artifacts.type.ArtifactTypeDefinition.ZIP_TYPE
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Classpath
import org.gradle.kotlin.dsl.registerTransform
import org.gradle.work.DisableCachingByDefault
import org.jetbrains.intellij.platform.gradle.IntelliJPluginConstants.Configurations.Attributes
import org.jetbrains.intellij.platform.gradle.asPath
import org.jetbrains.intellij.platform.gradle.model.BundledPlugin
import org.jetbrains.intellij.platform.gradle.model.BundledPlugins
import java.nio.file.Path
import kotlin.io.path.createTempDirectory
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.pathString

@DisableCachingByDefault(because = "Not worth caching")
abstract class BundledPluginsListTransformer : TransformAction<TransformParameters.None> {

    @get:Classpath
    @get:InputArtifact
    abstract val inputArtifact: Provider<FileSystemLocation>

    private val manager = IdePluginManager.createManager(createTempDirectory())

    override fun transform(outputs: TransformOutputs) {
        val input = inputArtifact.asPath
        val json = Json { ignoreUnknownKeys = true }
        val data = input.resolve("plugins")
            .listDirectoryEntries()
            .filter { it.isDirectory() }
            .mapNotNull { pluginDirectory ->
                pluginDirectory.resolvePlugin()?.run {
                    BundledPlugin(
                        id = pluginId ?: "",
                        name = pluginName ?: "",
                        version = pluginVersion ?: "",
                        path = pluginDirectory.pathString,
                        dependencies = dependencies.filterNot { it.isOptional }.map { it.id },
                    )
                }
            }
            .let { BundledPlugins(it) }

        outputs
            .file("bundled-plugins.json")
            .writeText(json.encodeToString(data))
    }

    private fun Path.resolvePlugin(): IdePlugin? {
        val creationResult = manager.createPlugin(this, false, IdePluginManager.PLUGIN_XML)

        return when (creationResult) {
            is PluginCreationSuccess -> creationResult.plugin
            is PluginCreationFail -> {
                val problems = creationResult.errorsAndWarnings.filter { it.level == PluginProblem.Level.ERROR }.joinToString()
//                warn(context, "Cannot create plugin from file '$artifact': $problems")
                null
            }

            else -> {
//                warn(context, "Cannot create plugin from file '$artifact'. $creationResult")
                null
            }
        }
    }
}

internal fun DependencyHandler.applyBundledPluginsListTransformer() {
    // ZIP archives fetched from the IntelliJ Maven repository
    artifactTypes.maybeCreate(ZIP_TYPE)
        .attributes
        .attribute(Attributes.bundledPluginsList, false)

    // Local IDEs pointed with intellijPlatformLocal dependencies helper
    artifactTypes.maybeCreate(ArtifactTypeDefinition.DIRECTORY_TYPE)
        .attributes
        .attribute(Attributes.bundledPluginsList, false)

    registerTransform(BundledPluginsListTransformer::class) {
        from
            .attribute(Attributes.extracted, true)
            .attribute(Attributes.collected, false)
            .attribute(Attributes.bundledPluginsList, false)
        to
            .attribute(Attributes.extracted, true)
            .attribute(Attributes.collected, false)
            .attribute(Attributes.bundledPluginsList, true)
    }
}