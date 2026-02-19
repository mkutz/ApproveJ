plugins {
  base
  `maven-publish`
  alias(libs.plugins.jreleaser)
  alias(libs.plugins.sonar)
  alias(libs.plugins.spotless)
}

repositories { mavenCentral() }

subprojects {
  afterEvaluate {
    if (plugins.hasPlugin("maven-publish")) {
      publishing {
        publications {
          create<MavenPublication>(name) {
            from(components.findByName("java") ?: components.getByName("javaPlatform"))
            pom {
              project.properties["mavenPomName"]?.let { name = "$it" }
              project.properties["mavenPomDescription"]?.let { description = "$it" }
              url = "https://approvej.org"
              inceptionYear = "2025"
              licenses {
                license {
                  name = "Apache-2.0"
                  url = "https://spdx.org/licenses/Apache-2.0.html"
                }
              }
              developers {
                developer {
                  id = "mkutz"
                  name = "Michael Kutz"
                }
              }
              scm {
                connection = "scm:git:https://github.com/mkutz/approvej.git"
                developerConnection = "scm:git:ssh://github.com/mkutz/approvej.git"
                url = "https://github.com/mkutz/approvej"
              }
            }
          }
        }
        repositories { maven { url = uri(layout.buildDirectory.dir("staging-deploy")) } }
      }
    }
  }
}

jreleaser {
  signing {
    active = org.jreleaser.model.Active.ALWAYS
    armored = true
  }
  deploy {
    maven {
      mavenCentral {
        create("sonatype") {
          active = org.jreleaser.model.Active.ALWAYS
          maxRetries = 60
          retryDelay = 30
          subprojects
            .filter { it.plugins.hasPlugin("maven-publish") }
            .sortedBy { it.name }
            .forEach {
              stagingRepository("${it.projectDir.relativeTo(rootDir)}/build/staging-deploy")
            }
          url = "https://central.sonatype.com/api/v1/publisher"
        }
      }
    }
  }
  release {
    github {
      overwrite = true
      update { enabled = true }
    }
  }
}

sonar {
  properties {
    property("sonar.projectKey", "mkutz_ApproveJ")
    property("sonar.organization", "mkutz")
    property("sonar.host.url", "https://sonarcloud.io")
  }
}

spotless {
  format("misc") {
    target("**/*.md", "**/*.xml", "**/*.yml", "**/*.yaml", "**/*.html", "**/*.css", ".gitignore")
    targetExclude("**/build/**/*", "**/.idea/**")
    trimTrailingWhitespace()
    endWithNewline()
    leadingTabsToSpaces(2)
  }

  java {
    target("**/*.java")
    targetExclude("**/build/**/*")
    googleJavaFormat("1.28.0").reflowLongStrings()
    removeUnusedImports()
    leadingTabsToSpaces(2)
  }

  kotlin {
    target("**/*.kt")
    targetExclude("**/build/**/*")
    ktfmt().googleStyle()
    leadingTabsToSpaces(2)
  }

  kotlinGradle {
    target("**/*.gradle.kts")
    targetExclude("**/build/**/*.gradle.kts")
    ktfmt().googleStyle()
  }

  groovy {
    target("**/*.groovy")
    targetExclude("**/build/**/*")
    greclipse()
    leadingTabsToSpaces(2)
  }

  freshmark { target("*.md") }
}

val updatePages by
  tasks.registering(Sync::class) {
    group = "documentation"
    description = "Assembles the website with manual and Javadoc"

    into(layout.buildDirectory.dir("pages"))

    // Favicon and logo
    from("favicon.png") { into("img") }
    from("logo.svg") { into("img") }

    // AsciiDoc manual
    from(project(":manual").tasks.named("asciidoctor"))

    // Cheat sheet PDF
    from(project(":manual").tasks.named("cheatSheetPdf")) { into("pdf") }

    // Javadoc for each module
    project(":modules").subprojects.forEach { module ->
      from(module.tasks.named("javadoc")) { into("javadoc/${module.name}") }
    }
  }
