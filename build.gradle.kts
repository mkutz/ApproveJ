plugins {
  base
  alias(libs.plugins.jreleaser)
  alias(libs.plugins.sonar)
  alias(libs.plugins.spotless)
}

repositories { mavenCentral() }

jreleaser {
  signing {
    active = org.jreleaser.model.Active.ALWAYS
    armored = true
    mode = org.jreleaser.model.Signing.Mode.MEMORY
  }
  deploy {
    maven {
      mavenCentral {
        create("sonatype") {
          url = "https://central.sonatype.com/api/v1/publisher"
          stagingRepository("build/pre-deploy")
        }
      }
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
