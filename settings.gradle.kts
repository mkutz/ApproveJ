plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0" }

rootProject.name = "approvej"

include("bom")

include("modules:core")

include("modules:json-jackson")

include("manual")
