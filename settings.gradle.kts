plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

rootProject.name = "approvej"

include("bom")

include("modules:core")

include("modules:json-jackson")

include("modules:yaml-jackson")

include("manual")
