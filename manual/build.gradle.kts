plugins {
  alias(libs.plugins.asciidoctor)
  java
  alias(libs.plugins.kotlinJvm)
  `java-test-fixtures`
}

repositories { mavenCentral() }

val asciidoctorExt: Configuration by configurations.creating

dependencies {
  asciidoctorExt(libs.asciidoctorBlockSwitch)

  testImplementation(project(":modules:core"))

  testImplementation(platform(libs.junitBom))
  testImplementation(libs.junitJupiterApi)
  testImplementation(libs.junitJupiterParams)
  testImplementation(libs.assertjCore)

  testRuntimeOnly(libs.junitPlatformLauncher)
  testRuntimeOnly(libs.junitJupiterEngine)
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }

tasks.withType<Test> { useJUnitPlatform() }

tasks.withType<org.asciidoctor.gradle.jvm.AsciidoctorTask> {
  baseDirFollowsSourceFile()
  configurations("asciidoctorExt")
}

asciidoctorj { modules { diagram.use() } }
