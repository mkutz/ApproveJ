package org.approvej.kotest

import io.kotest.core.extensions.TestCaseExtension
import io.kotest.core.test.TestCase
import io.kotest.engine.test.TestResult
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.withContext

/**
 * Kotest [TestCaseExtension] that records the currently running [TestCase] so ApproveJ can derive
 * the approved and received file paths from it.
 *
 * Register it in your project's Kotest configuration, e.g.:
 * ```kotlin
 * package io.kotest.provided
 *
 * class ProjectConfig : AbstractProjectConfig() {
 *   override val extensions = listOf(ApprovejKotestExtension())
 * }
 * ```
 *
 * The current [TestCase] is bound to the executing thread via a [ThreadLocal]. To stay reliable on
 * Kotest's default multi-threaded dispatcher, where a coroutine may resume on a different thread,
 * the value is also installed as a coroutine [asContextElement], so it is restored on whichever
 * thread the test body resumes on.
 */
class ApprovejKotestExtension : TestCaseExtension {

  override suspend fun intercept(
    testCase: TestCase,
    execute: suspend (TestCase) -> TestResult,
  ): TestResult = withContext(currentKotestTest.asContextElement(testCase)) { execute(testCase) }
}
