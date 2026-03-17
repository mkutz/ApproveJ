@file:Suppress("UnstableApiUsage")

import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.jetbrains.intellij.platform.gradle.TestFrameworkType

buildscript {
  repositories { mavenCentral() }
  dependencies { classpath(libs.commonmark) }
}

plugins {
  alias(libs.plugins.kotlin.jvm)
  jacoco
  `jvm-test-suite`
  alias(libs.plugins.intellij.platform)
}

kotlin { jvmToolchain(21) }

repositories {
  mavenCentral()
  intellijPlatform { defaultRepositories() }
}

intellijPlatform {
  pluginConfiguration {
    id = "org.approvej"
    name = "ApproveJ"
    version = project.version.toString()
    description =
      """
      <p>IDE support for <a href="https://approvej.org">ApproveJ</a>, an approval testing library for the JVM.</p>

      <h3>Features</h3>
      <ul>
        <li><b>Diff viewer</b> — side-by-side comparison of received and approved files from
            editor banners, context menus, or gutter icon popups.</li>
        <li><b>One-click approve</b> — approve a received file with a single click; fully undoable.</li>
        <li><b>Bidirectional navigation</b> — gutter icons on <code>approve()…byFile()</code> chains
            navigate to the approved file. Editor banners on approved and received files link back
            to the test method.</li>
        <li><b>Dangling approval inspection</b> — highlights <code>approve()</code> calls missing a
            terminal method and offers quick fixes.</li>
      </ul>

      <p>See the <a href="https://approvej.org/#intellij_plugin">documentation</a> for details.</p>
      """
        .trimIndent()
    vendor { name = "ApproveJ" }
    ideaVersion { sinceBuild = "251" }
    changeNotes = provider { extractChangeNotes(project.version.toString()) }
  }
  publishing { token = providers.environmentVariable("INTELLIJ_MARKETPLACE_TOKEN") }
}

fun extractChangeNotes(version: String): String {
  val changelog = project.file("CHANGELOG.md")
  if (!changelog.exists()) return ""
  val lines = changelog.readLines()
  val versionStart = lines.indexOfFirst { it.trimEnd() == "## v$version" }
  if (versionStart == -1) return ""
  val versionEnd =
    lines
      .drop(versionStart + 1)
      .indexOfFirst { it.startsWith("## v") }
      .let { if (it == -1) lines.size else it + versionStart + 1 }
  val markdown = lines.subList(versionStart + 1, versionEnd).joinToString("\n").trim()
  val document = Parser.builder().build().parse(markdown)
  return HtmlRenderer.builder().build().render(document).trim()
}

/* Use JUnit 5.11 for IntelliJ platform tests to avoid version conflicts with IntelliJ's test
framework */
dependencies {
  intellijPlatform {
    intellijIdeaCommunity(libs.versions.intellij.ide)
    bundledPlugin("com.intellij.java")
    testFramework(TestFrameworkType.Platform)
    testFramework(TestFrameworkType.Plugin.Java)
  }

  testImplementation(libs.assertj.core)
  testImplementation(libs.junit5.jupiter.api)
  testImplementation(libs.junit5.jupiter.params)
  testImplementation(libs.opentest4j)
  testImplementation(libs.junit4)

  testRuntimeOnly(libs.junit5.platform.launcher)
  testRuntimeOnly(libs.junit5.jupiter.engine)
  testRuntimeOnly(libs.junit5.vintage.engine)
}

// Force JUnit 5.11 versions over JUnit 6 from BOM
val junit5Version = libs.versions.junit5.get()
val junit5PlatformVersion = libs.versions.junitPlatform5.get()

configurations.testRuntimeClasspath {
  resolutionStrategy.eachDependency {
    if (requested.group == "org.junit.platform") useVersion(junit5PlatformVersion)
    if (requested.group == "org.junit.jupiter") useVersion(junit5Version)
    if (requested.group == "org.junit.vintage") useVersion(junit5Version)
  }
}

configurations.testCompileClasspath {
  resolutionStrategy.eachDependency {
    if (requested.group == "org.junit.platform") useVersion(junit5PlatformVersion)
    if (requested.group == "org.junit.jupiter") useVersion(junit5Version)
  }
}

tasks.test { useJUnitPlatform() }

testing {
  suites {
    val unitTest by
      registering(JvmTestSuite::class) {
        useJUnitJupiter()
        dependencies {
          implementation(platform(libs.junit.bom))
          implementation(libs.junit.jupiter.api)
          implementation(libs.assertj.core)
          implementation(project())

          runtimeOnly(libs.junit.platform.launcher)
          runtimeOnly(libs.junit.jupiter.engine)
        }
      }
  }
}

// Add IntelliJ platform JARs to unitTest so production classes can be compiled against and loaded
tasks.named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileUnitTestKotlin") {
  libraries.from(configurations.getByName("intellijPlatformClasspath"))
}

tasks.named<Test>("unitTest") { classpath += configurations.getByName("intellijPlatformClasspath") }

tasks.named("check") { dependsOn(testing.suites.named("unitTest")) }

tasks.jacocoTestReport {
  mustRunAfter(tasks.check)
  executionData(fileTree(project.layout.buildDirectory) { include("**/jacoco/*.exec") })
  reports { xml.required = true }
}
