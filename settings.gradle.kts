plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0" }

rootProject.name = "approvej"

include("modules:core")

include("modules:json-jackson")

include("manual")
