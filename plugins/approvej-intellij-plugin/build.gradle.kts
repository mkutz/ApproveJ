import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.intellij.platform)
}

repositories {
  mavenCentral()
  intellijPlatform { defaultRepositories() }
}

dependencies {
  intellijPlatform {
    intellijIdeaCommunity(libs.versions.intellij.get())
    bundledPlugin("com.intellij.java")
    testFramework(TestFrameworkType.Platform)
  }
}

intellijPlatform {
  pluginConfiguration {
    id = "org.approvej.intellij"
    name = "ApproveJ"
    version = project.version.toString()
  }
}
