// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.intellij.platform.gradle

import org.gradle.testkit.runner.TaskOutcome
import org.jetbrains.intellij.platform.gradle.Constants.Tasks
import org.jetbrains.intellij.platform.gradle.models.Coordinates
import org.jetbrains.intellij.platform.gradle.models.resolveLatestVersion
import kotlin.test.Test

private const val DEPENDENCIES = "dependencies"

class IntelliJPlatformDependencyValidationIntegrationTest : IntelliJPlatformIntegrationTestBase(
    resourceName = "intellij-platform-dependency-validation",
) {

    @Test
    fun `allow for no IntelliJ Platform dependency if not running tasks`() {
        build(DEPENDENCIES) {
            assertContains(
                """
                intellijPlatformDependency - IntelliJ Platform
                No dependencies
                """.trimIndent(),
                output,
            )
        }
    }

    @Test
    fun `correctly resolve IntelliJ Platform dependency`() {
        buildFile write //language=kotlin
                """
                repositories {
                    intellijPlatform {
                        defaultRepositories()
                    }
                }
                
                dependencies {
                    intellijPlatform {
                        create("$intellijPlatformType", "$intellijPlatformVersion")
                    }
                }
                """.trimIndent()

        build(DEPENDENCIES) {
            assertContains(
                """
                intellijPlatformDependencyArchive - IntelliJ Platform dependency archive
                \--- idea:ideaIC:2022.3.3
                """.trimIndent(),
                output,
            )

            assertContains(
                """
                intellijPlatformLocal - IntelliJ Platform local
                No dependencies
                """.trimIndent(),
                output,
            )

            assertContains(
                """
                intellijPlatformDependency - IntelliJ Platform
                \--- idea:ideaIC:2022.3.3
                """.trimIndent(),
                output,
            )
        }
    }

    @Test
    fun `report too many IntelliJ Platform dependencies`() {
        buildFile write //language=kotlin
                """
                repositories {
                    intellijPlatform {
                        defaultRepositories()
                    }
                }
                
                dependencies {
                    intellijPlatform {
                        create("$intellijPlatformType", "$intellijPlatformVersion")
                        create("${IntelliJPlatformType.PhpStorm}", "$intellijPlatformVersion")
                    }
                }
                """.trimIndent()

        buildAndFail(DEPENDENCIES) {
            assertContains("More than one IntelliJ Platform dependencies found.", output)
        }
    }

    @Test
    fun `inform about missing IntelliJ Platform dependency`() {
        buildFile write //language=kotlin
                """
                dependencies {
                    intellijPlatform {
                        create("$intellijPlatformType", "$intellijPlatformVersion")
                    }
                }
                """.trimIndent()

        build(DEPENDENCIES) {
            assertContains(
                """
                intellijPlatformDependency - IntelliJ Platform
                \--- idea:ideaIC:2022.3.3 FAILED
                """.trimIndent(),
                output,
            )
        }

        buildAndFail(Tasks.VERIFY_PLUGIN_PROJECT_CONFIGURATION) {
            assertContains(
                """
                > Failed to query the value of task ':verifyPluginProjectConfiguration' property 'runtimeDirectory'.
                   > Could not resolve all files for configuration ':intellijPlatformDependency'.
                      > Could not find idea:ideaIC:2022.3.3.
                """.trimIndent(),
                output,
            )
        }
    }

    @Test
    fun `correctly resolve Marketplace ZIP Signer dependency in the fixed version`() {
        buildFile write //language=kotlin
                """
                repositories {
                    intellijPlatform {
                        defaultRepositories()
                    }
                }
                
                dependencies {
                    intellijPlatform {
                        create("$intellijPlatformType", "$intellijPlatformVersion")
                        zipSigner("0.1.24")
                    }
                }
                """.trimIndent()

        build(DEPENDENCIES) {
            assertContains(
                """
                marketplaceZipSigner - Marketplace ZIP Signer
                \--- org.jetbrains:marketplace-zip-signer:{prefer 0.1.24} -> 0.1.24
                """.trimIndent(),
                output,
            )
        }
    }

    @Test
    fun `correctly resolve Marketplace ZIP Signer dependency in the latest version`() {
        buildFile write //language=kotlin
                """
                repositories {
                    intellijPlatform {
                        defaultRepositories()
                    }
                }
                
                dependencies {
                    intellijPlatform {
                        create("$intellijPlatformType", "$intellijPlatformVersion")
                        zipSigner()
                    }
                }
                
                intellijPlatform {
                    instrumentCode = false
                
                    signing {
                        certificateChainFile = file("certificate/chain.crt")
                        privateKeyFile = file("certificate/private.pem")
                    }
                }
                """.trimIndent()

        val latestVersion = Coordinates("org.jetbrains", "marketplace-zip-signer").resolveLatestVersion()
        build(DEPENDENCIES) {
            assertContains(
                """
                marketplaceZipSigner - Marketplace ZIP Signer
                \--- org.jetbrains:marketplace-zip-signer:{prefer +} -> $latestVersion
                """.trimIndent(),
                output,
            )
        }

        build(Tasks.SIGN_PLUGIN) {
            assertTaskOutcome(Tasks.SIGN_PLUGIN, TaskOutcome.SUCCESS)
        }
    }

    @Test
    fun `correctly resolve Marketplace ZIP Signer dependency in the latest version when a default dependency is used`() {
        buildFile write //language=kotlin
                """
                repositories {
                    intellijPlatform {
                        defaultRepositories()
                    }
                }
                
                dependencies {
                    intellijPlatform {
                        create("$intellijPlatformType", "$intellijPlatformVersion")
                    }
                }
                
                intellijPlatform {
                    instrumentCode = false
                
                    signing {
                        certificateChainFile = file("certificate/chain.crt")
                        privateKeyFile = file("certificate/private.pem")
                    }
                }
                """.trimIndent()

        val latestVersion = Coordinates("org.jetbrains", "marketplace-zip-signer").resolveLatestVersion()
        build(DEPENDENCIES) {
            assertContains(
                """
                marketplaceZipSigner - Marketplace ZIP Signer
                \--- org.jetbrains:marketplace-zip-signer:{prefer +} -> $latestVersion
                """.trimIndent(),
                output,
            )
        }

        build(Tasks.SIGN_PLUGIN) {
            assertTaskOutcome(Tasks.SIGN_PLUGIN, TaskOutcome.SUCCESS)
        }
    }

    // TODO: verify missing IntelliJ Platform dependency when no repositories are added
    // TODO: use IntelliJ Platform from local
}
