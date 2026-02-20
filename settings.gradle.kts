plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

rootProject.name = "approvej"

include("bom")

include("modules:core")

include("plugins:approvej-gradle-plugin")

include("plugins:approvej-maven-plugin")

include("modules:json-jackson")

include("modules:json-jackson3")

include("modules:yaml-jackson")

include("modules:yaml-jackson3")

include("modules:http")

include("modules:http-wiremock")

include("manual")
