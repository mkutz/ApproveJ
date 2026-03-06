plugins { alias(libs.plugins.node) }

node {
  download = true
  version = "20.18.1"
}

tasks.npmInstall { inputs.file("package-lock.json") }

val npmCompile by
  tasks.registering(com.github.gradle.node.npm.task.NpmTask::class) {
    dependsOn(tasks.npmInstall)
    args = listOf("run", "compile")
    inputs.dir("src")
    inputs.file("tsconfig.json")
    outputs.dir("out")
  }

val npmTest by
  tasks.registering(com.github.gradle.node.npm.task.NpmTask::class) {
    dependsOn(npmCompile)
    args = listOf("test")
    inputs.dir("out")
  }

val npmPackage by
  tasks.registering(com.github.gradle.node.npm.task.NpmTask::class) {
    dependsOn(npmCompile)
    args = listOf("run", "package")
    inputs.dir("out")
    inputs.file("package.json")
    outputs.dir(layout.projectDirectory)
  }

val npmSetVersion by
  tasks.registering(com.github.gradle.node.npm.task.NpmTask::class) {
    args =
      listOf("version", project.version.toString(), "--no-git-tag-version", "--allow-same-version")
  }

val npmPublish by
  tasks.registering(com.github.gradle.node.npm.task.NpxTask::class) {
    dependsOn(npmSetVersion, npmCompile)
    command = "vsce"
    args = listOf("publish")
    environment = mapOf("VSCE_PAT" to (System.getenv("VSCE_PAT") ?: ""))
  }

tasks.register("check") { dependsOn(npmTest) }

tasks.register("clean") { delete("out", "node_modules") }
