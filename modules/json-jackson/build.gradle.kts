repositories { mavenCentral() }

dependencies {
  api(libs.jspecify)
  api(libs.jacksonDatabind)
  api(libs.jsonpath)
  api(project(":modules:core"))

  testImplementation(libs.jacksonJsr310)
  testImplementation(platform(libs.junitBom))
  testImplementation(libs.junitJupiterApi)
  testImplementation(libs.junitJupiterParams)
  testImplementation(libs.assertjCore)

  testRuntimeOnly(libs.junitPlatformLauncher)
  testRuntimeOnly(libs.junitJupiterEngine)
}
