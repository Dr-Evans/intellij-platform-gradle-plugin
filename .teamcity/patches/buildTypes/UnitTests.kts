package patches.buildTypes

import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the buildType with id = 'UnitTests'
accordingly, and delete the patch script.
*/
changeBuildType(RelativeId("UnitTests")) {
    expectSteps {
        gradle {
            name = "Unit Tests – Gradle 6.7.1"
            tasks = "check -PtestGradleVersion=6.7.1"
        }
        gradle {
            name = "Unit Tests – Gradle 6.9.2"
            tasks = "check -PtestGradleVersion=6.9.2"
        }
        gradle {
            name = "Unit Tests – Gradle 7.5.1"
            tasks = "check -PtestGradleVersion=7.5.1"
        }
    }
    steps {
        insert(3) {
            maven {
                goals = "clean test"
                pomLocation = ".teamcity/pom.xml"
                runnerArgs = "-Dmaven.test.failure.ignore=true"
            }
        }
    }

    requirements {
        add {
            equals("teamcity.agent.jvm.os.name", "Linux")
        }
    }
}
