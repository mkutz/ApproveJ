package org.approvej.intellij

internal object ApproveJProperties {

  const val FILE_NAME = "approvej.properties"

  data class PropertyDef(
    val key: String,
    val knownValues: List<String>? = null,
    val deprecated: Boolean = false,
    val deprecationMessage: String? = null,
  )

  val ALL: List<PropertyDef> =
    listOf(
      PropertyDef(
        "defaultPrintFormat",
        listOf("singleLineString", "multiLineString", "json", "yaml"),
      ),
      PropertyDef("defaultFileReviewer", listOf("none", "automatic", "script", "ai")),
      PropertyDef("reviewerScript"),
      PropertyDef("reviewerAiCommand"),
      PropertyDef("inventoryEnabled", listOf("true", "false")),
      PropertyDef("defaultInlineValueReviewer", listOf("none", "automatic", "script", "ai")),
      PropertyDef(
        key = "defaultFileReviewerScript",
        deprecated = true,
        deprecationMessage =
          "'defaultFileReviewerScript' is deprecated." +
            " Use 'defaultFileReviewer = script' and 'reviewerScript = ...' instead.",
      ),
    )

  val BY_KEY: Map<String, PropertyDef> = ALL.associateBy { it.key }
}
