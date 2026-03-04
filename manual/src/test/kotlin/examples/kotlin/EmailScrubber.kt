package examples.kotlin

import org.approvej.scrub.Replacement
import org.approvej.scrub.Replacements
import org.approvej.scrub.StringScrubber

class EmailScrubber private constructor(private val replacement: Replacement<String>) :
  StringScrubber { // <1>

  constructor() : this(Replacements.numbered("email")) // <2>

  override fun apply(value: String): String { // <3>
    val findings = mutableMapOf<String, Int>()
    return Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}").replace(value) { match ->
      val group = match.value
      findings.putIfAbsent(group, findings.size + 1)
      replacement.apply(group, findings[group]!!).toString()
    }
  }

  override fun replacement(replacement: Replacement<String>): StringScrubber { // <4>
    return EmailScrubber(replacement)
  }
}
