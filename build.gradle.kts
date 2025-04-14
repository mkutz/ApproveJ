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
          stagingRepository("modules/core/build/staging-deploy")
          stagingRepository("modules/json-jackson/build/staging-deploy")
          stagingRepository("bom/build/staging-deploy")
          url = "https://central.sonatype.com/api/v1/publisher"
        }
      }
    }
  }
  release { github { update { enabled = true } } }
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
    googleJavaFormat().reflowLongStrings()
    removeUnusedImports()
    leadingTabsToSpaces(2)
  }

  kotlinGradle {
    target("**/*.gradle.kts")
    targetExclude("**/build/**/*.gradle.kts")
    ktfmt().googleStyle()
  }

  freshmark { target("*.md") }
}
