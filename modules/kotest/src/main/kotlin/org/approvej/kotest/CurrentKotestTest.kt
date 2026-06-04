package org.approvej.kotest

import io.kotest.core.test.TestCase
import java.util.Optional
import org.approvej.approve.CurrentTestLocator
import org.approvej.approve.TestLocation

/**
 * Holds the Kotest [TestCase] currently being executed on this thread.
 *
 * It is set by [ApprovejKotestExtension] for the duration of each test and read by
 * [KotestTestLocator].
 */
internal val currentKotestTest = ThreadLocal<TestCase?>()

/**
 * [CurrentTestLocator] that reports the Kotest test currently running on this thread.
 *
 * Discovered by ApproveJ via the [java.util.ServiceLoader] mechanism. The running test is captured
 * by [ApprovejKotestExtension], which must be registered in the project's Kotest configuration.
 */
class KotestTestLocator : CurrentTestLocator {

  override fun currentTest(): Optional<TestLocation> {
    val testCase = currentKotestTest.get() ?: return Optional.empty()
    val testCaseName = testCase.descriptor.testParts().joinToString(separator = " ")
    return Optional.of(TestLocation(testCase.spec::class.java, testCaseName))
  }
}
