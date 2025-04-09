plugins {
  jacoco
  `java-library`
  `maven-publish`
  signing
  alias(libs.plugins.jreleaser)
}

subprojects {
  apply(plugin = "maven-publish")
  apply(plugin = "java-library")
  apply(plugin = "jacoco")
  apply(plugin = "signing")

  java {
    withJavadocJar()
    withSourcesJar()
    toolchain { languageVersion = JavaLanguageVersion.of(21) }
  }

  tasks.withType<Test> { useJUnitPlatform() }

  tasks.jacocoTestReport { reports { xml.required = true } }

  publishing {
    publications {
      create<MavenPublication>(name) {
        artifactId = name
        from(components["java"] ?: components["javaPlatform"])
        pom {
          project.properties["mavenPomName"]?.let { name = it.toString() }
          project.properties["mavenPomDescription"]?.let { description = it.toString() }
          url = "https://approvej.org"
          licenses {
            license {
              name.set("The Apache License, Version 2.0")
              url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
          }
          developers {
            developer {
              id = "mkutz"
              name = "Michael Kutz"
              email = "mail@michael-kutz.de"
            }
          }
          scm {
            connection = "scm:git:git://github.com/mkutz/approvej.git"
            developerConnection = "scm:git:ssh://github.com/mkutz/approvej.git"
            url = "https://github.com/mkutz/approvej"
          }
        }
      }
    }
    repositories {
      maven {
        name = "LocalMavenWithChecksums"
        url = uri(layout.buildDirectory.dir("staging-deploy"))
      }
      maven {
        name = "PreDeploy"
        url = uri(layout.buildDirectory.dir("pre-deploy"))
      }
    }
  }

  signing {
    sign(publishing.publications)
    useGpgCmd()
  }
}
