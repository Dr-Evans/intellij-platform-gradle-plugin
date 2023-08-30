// Copyright 2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

@file:Suppress("unused")

package org.jetbrains.intellij.platform.gradleplugin.dependencies

import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.create
import org.jetbrains.intellij.platform.gradleplugin.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradleplugin.IntelliJPlatformType.*
import org.jetbrains.intellij.platform.gradleplugin.IntelliJPluginConstants.INTELLIJ_PLATFORM_CONFIGURATION_NAME

internal fun DependencyHandlerScope.intellijPlatform(
    type: IntelliJPlatformType,
    version: String,
    configurationName: String = INTELLIJ_PLATFORM_CONFIGURATION_NAME,
) = add(configurationName, create(
    group = type.groupId,
    name = type.artifactId,
    version = version,
))

internal fun DependencyHandlerScope.intellijPlatform(
    type: IntelliJPlatformType,
    versionProvider: Provider<String>,
    configurationName: String = INTELLIJ_PLATFORM_CONFIGURATION_NAME,
) = addProvider(configurationName, versionProvider.map {
    create(
        group = type.groupId,
        name = type.artifactId,
        version = it,
    )
})

//    return when (type) {
//        IntellijIdeaUltimate -> create(
//            group = "com.jetbrains.intellij.idea",
//            name = "ideaIU",
//            version = version,
//        )
//
//        CLion -> create(
//            group = "com.jetbrains.intellij.clion",
//            name = "clion",
//            version = version,
//        )
//
//        PyCharmProfessional -> create(
//            group = "com.jetbrains.intellij.pycharm",
//            name = "pycharmPY",
//            version = version,
//        )
//
//        PyCharmCommunity -> create(
//            group = "com.jetbrains.intellij.pycharm",
//            name = "pycharmPC",
//            version = version,
//        )
//
//        GoLand -> create(
//            group = "com.jetbrains.intellij.goland",
//            name = "goland",
//            version = version,
//        )
//
//        PhpStorm -> create(
//            group = "com.jetbrains.intellij.phpstorm",
//            name = "phpstorm",
//            version = version,
//        )
//
//        Rider -> create(
//            group = "com.jetbrains.intellij.rider",
//            name = "riderRD",
//            version = version,
//        hasSources = (sources && releaseType != IntelliJPluginConstants.RELEASE_TYPE_SNAPSHOTS).ifFalse {
//            warn(context, "IDE sources are not available for Rider SNAPSHOTS")
//        },
//        )
//
//        Gateway -> create(
//            group = "com.jetbrains.gateway",
//            name = "JetBrainsGateway",
//            version = version,
//        hasSources = false,
//        )
//
//        AndroidStudio -> create(
//            group = "com.google.android.studio",
//            name = "android-studio",
//            version = version,
//        hasSources = false,
//            ext = when {
//                OperatingSystem.current().isLinux -> "tar.gz"
//                else -> "zip"
//            },
//        )
//    {
//        with(it) {
//            Files.list(resolveAndroidStudioPath(this))
//                .forEach { entry -> Files.move(entry, resolve(entry.fileName), StandardCopyOption.REPLACE_EXISTING) }
//        }
//    }
//    }

fun DependencyHandlerScope.intellijPlatform(type: String, version: String) = intellijPlatform(IntelliJPlatformType.fromCode(type), version)
fun DependencyHandlerScope.intellijPlatform(type: String, version: Provider<String>) = intellijPlatform(IntelliJPlatformType.fromCode(type), version)

fun DependencyHandlerScope.androidStudio(version: String) = intellijPlatform(AndroidStudio, version)
fun DependencyHandlerScope.androidStudio(version: Provider<String>) = intellijPlatform(AndroidStudio, version)

fun DependencyHandlerScope.clion(version: String) = intellijPlatform(CLion, version)
fun DependencyHandlerScope.clion(version: Provider<String>) = intellijPlatform(CLion, version)

fun DependencyHandlerScope.gateway(version: String) = intellijPlatform(Gateway, version)
fun DependencyHandlerScope.gateway(version: Provider<String>) = intellijPlatform(Gateway, version)

fun DependencyHandlerScope.goland(version: String) = intellijPlatform(GoLand, version)
fun DependencyHandlerScope.goland(version: Provider<String>) = intellijPlatform(GoLand, version)

fun DependencyHandlerScope.intellijIdeaCommunity(version: String) = intellijPlatform(IntellijIdeaCommunity, version)
fun DependencyHandlerScope.intellijIdeaCommunity(version: Provider<String>) = intellijPlatform(IntellijIdeaCommunity, version)

fun DependencyHandlerScope.intellijIdeaUltimate(version: String) = intellijPlatform(IntellijIdeaUltimate, version)
fun DependencyHandlerScope.intellijIdeaUltimate(version: Provider<String>) = intellijPlatform(IntellijIdeaUltimate, version)

fun DependencyHandlerScope.phpstorm(version: String) = intellijPlatform(PhpStorm, version)
fun DependencyHandlerScope.phpstorm(version: Provider<String>) = intellijPlatform(PhpStorm, version)

fun DependencyHandlerScope.pycharmProfessional(version: String) = intellijPlatform(PyCharmProfessional, version)
fun DependencyHandlerScope.pycharmProfessional(version: Provider<String>) = intellijPlatform(PyCharmProfessional, version)

fun DependencyHandlerScope.pycharmCommunity(version: String) = intellijPlatform(PyCharmCommunity, version)
fun DependencyHandlerScope.pycharmCommunity(version: Provider<String>) = intellijPlatform(PyCharmCommunity, version)

fun DependencyHandlerScope.rider(version: String) = intellijPlatform(Rider, version)
fun DependencyHandlerScope.rider(version: Provider<String>) = intellijPlatform(Rider, version)