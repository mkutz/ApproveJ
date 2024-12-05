plugins { `java-platform` }

dependencies {
  constraints {
    (parent?.subprojects)
      ?.filter { it != project && it.name != "manual" && it.subprojects.isEmpty() }
      ?.sortedBy { it.name }
      ?.forEach { api(it) }
  }
}
