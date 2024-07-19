// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.intellij.platform.gradle

import org.gradle.api.provider.ProviderFactory
import org.jetbrains.intellij.platform.gradle.Constants.Locations
import org.jetbrains.intellij.platform.gradle.Constants.Plugin
import org.jetbrains.intellij.platform.gradle.extensions.IntelliJPlatformExtension
import org.jetbrains.intellij.platform.gradle.tasks.BuildSearchableOptionsTask
import org.jetbrains.intellij.platform.gradle.utils.Logger

/**
 * The IntelliJ Platform Gradle Plugin introduces custom Gradle properties to control some of the low-level Gradle plugin behaviors.
 * To adjust a particular feature, add a Project property to the `gradle.properties` file with the following pattern:
 *
 * ```
 * org.jetbrains.intellij.platform.<propertyName>=<value>
 * ```
 */
sealed class GradleProperties<T : Any>(val defaultValue: T) {

    /**
     * Instruct the IDE that sources are needed to be downloaded when working with IntelliJ Platform Gradle Plugin.
     * Value is passed directly to the [Idea Gradle Plugin](https://docs.gradle.org/current/userguide/idea_plugin.html)
     * to the `idea.module.downloadSources` property.
     *
     * @see <a href="https://docs.gradle.org/current/dsl/org.gradle.plugins.ide.idea.model.IdeaModule.html#org.gradle.plugins.ide.idea.model.IdeaModule:downloadSources">IdeaModule.downloadSources</a>
     */
    object DownloadSources : GradleProperties<Boolean>(true)

    object IntellijPlatformCache : GradleProperties<String>("")

    object LocalPlatformArtifacts : GradleProperties<String>("")

    /**
     * When the [BuildSearchableOptionsTask] doesn't produce any results, e.g., when the plugin doesn't implement any settings, a warning is shown
     * to suggest disabling it for better performance with [IntelliJPlatformExtension.buildSearchableOptions].
     */
    object NoSearchableOptionsWarning : GradleProperties<Boolean>(true)

    /**
     * Due to IDE limitations, it is impossible to run the IDE in headless mode to collect searchable options for a paid plugin.
     * As paid plugins require providing a valid license and presenting a UI dialog, it is impossible to handle such a case, and the task will fail.
     * This feature flag displays the given warning when the task is run by a paid plugin.
     */
    object PaidPluginSearchableOptionsWarning : GradleProperties<Boolean>(true)

    object ProductsReleasesAndroidStudioUrl : GradleProperties<String>(Locations.PRODUCTS_RELEASES_ANDROID_STUDIO)

    object ProductsReleasesJetBrainsIdesUrl : GradleProperties<String>(Locations.PRODUCTS_RELEASES_JETBRAINS_IDES)

    /**
     * Checks whether the currently used Gradle IntelliJ Plugin is outdated and if a new release is available.
     * The plugin performs an update check on every run asking the GitHub Releases page for the redirection URL
     * to the latest version with `HEAD` HTTP request: `https://github.com/JetBrains/intellij-platform-gradle-plugin/releases/latest`.
     *
     * If the current version is outdated, the plugin will emit a warning with its current and the latest version.
     *
     * Feature respects the Gradle `--offline` mode.
     */
    object SelfUpdateCheck : GradleProperties<Boolean>(true)

    object ShimServerPort : GradleProperties<Int>(7348)

    /**
     * By default, JetBrains Cache Redirector is used when resolving Maven repositories or any resources used by the IntelliJ Platform Gradle Plugin.
     * Due to limitations, sometimes it is desired to limit the list of remote endpoints accessed by Gradle.
     *
     * It is possible to refer to the direct location (whenever it is possible) by switching off JetBrains Cache Redirector globally.
     */
    object UseCacheRedirector : GradleProperties<Boolean>(true)

    override fun toString() =
        requireNotNull(this::class.simpleName)
            .replaceFirstChar(Char::lowercase)
            .let { Plugin.ID + '.' + it }
}

inline operator fun <reified T : Any> ProviderFactory.get(property: GradleProperties<T>) =
    gradleProperty(property.toString())
        .map {
            Logger(javaClass).info("Read Gradle property: $property=$it")

            when (property.defaultValue) {
                is Boolean -> it.toBoolean()
                is Int -> it.toInt()
                is String -> it
                else -> it
            } as T
        }
        .orElse(property.defaultValue)