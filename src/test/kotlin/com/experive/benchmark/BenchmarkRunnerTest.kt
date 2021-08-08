package com.experive.benchmark

import com.google.common.truth.Truth
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.lang.Thread.sleep
import java.util.concurrent.TimeUnit
import java.util.stream.Stream

@Suppress("MemberVisibilityCanBePrivate")
internal class BenchmarkRunnerTest {

    @Test
    internal fun testComparisonInAvgtMode() {
        val runner = BenchmarkRunner()
        runner.add(this::testSleep, 1)
            .add(this::testSleep, 10)
            .runAll()
    }

    @Test
    internal fun testComparisonInThroughputMode() {
        val runner = BenchmarkRunner(mode = Mode.Throughput(), timeUnit = TimeUnit.SECONDS)
        runner.add(this::testSleep, 1)
        runner.add(this::testSleep, 10)
        runner.runAll()
    }

    @Test
    internal fun throttleOutputWhenIterationIsOver1000() {
        val runner = BenchmarkRunner(mode = Mode.Throughput(), timeUnit = TimeUnit.SECONDS, iterations = 10000)
        runner.add(this::noop)
        runner.runAll()
    }

    @ParameterizedTest
    @MethodSource("timeUnits")
    internal fun testUnitIsConvertedCorrectly(timeUnit: TimeUnit) {
        val runner = BenchmarkRunner(
            mode = Mode.Throughput(),
            warmup = 1,
            iterations = 2,
            timeUnit = timeUnit
        )
        runner.add(this::noop)
        runner.runAll()
    }

    @Test
    internal fun verifyThatHooksAreCalled() {
        val beforeAll = mockk<Runnable>(relaxed = true)
        val afterAll = mockk<Runnable>(relaxed = true)
        val beforeEach = mockk<Runnable>(relaxed = true)
        val afterEach = mockk<Runnable>(relaxed = true)
        val mockFunction = spyk(this::testSleep)
        val runner = BenchmarkRunner()
            .doBeforeAll(beforeAll)
            .doAfterAll(afterAll)
            .doAfterEach(afterEach)
            .doBeforeEach(beforeEach)
        runner.add(mockFunction, 10)
        runner.runAll()

        verify(exactly = 1) { beforeAll.run() }
        verify(exactly = 1) { afterAll.run() }
        verify(exactly = 16) { beforeEach.run() }
        verify(exactly = 16) { afterEach.run() }
        verify(exactly = 16) { mockFunction.call(10) }
    }

    @Test
    internal fun shouldNotExecuteWarmupRun() {
        val mockFunction = spyk(this::noop)
        BenchmarkRunner(warmup = 0, iterations = 1)
            .add(mockFunction)
            .runAll()
        verify(exactly = 1) { mockFunction.call() }
    }

    @Test
    @DisplayName("If iteration count is negative or zero, it should throw illegal argument")
    internal fun shouldThrowExceptionWhenInitializingWarmup() {
        val t = assertThrows<IllegalStateException> {
            BenchmarkRunner(warmup = -1)
        }
        Truth.assertThat(t).hasMessageThat().isEqualTo("Warmup cannot be negative")
    }

    @Test
    @DisplayName("If iteration count is negative or zero, it should throw illegal argument")
    internal fun shouldThrowExceptionWhenInitializing() {
        val t = assertThrows<IllegalStateException> {
            BenchmarkRunner(iterations = 0)
        }
        Truth.assertThat(t).hasMessageThat().isEqualTo("Iteration count should be greater than zero")
    }

    fun testSleep(amount: Int) {
        sleep(amount.toLong())
    }

    fun noop() = Unit

    companion object {
        @JvmStatic
        fun timeUnits(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(TimeUnit.NANOSECONDS),
                Arguments.of(TimeUnit.MICROSECONDS),
                Arguments.of(TimeUnit.MILLISECONDS),
                Arguments.of(TimeUnit.SECONDS),
                Arguments.of(TimeUnit.MINUTES),
                Arguments.of(TimeUnit.HOURS),
                Arguments.of(TimeUnit.DAYS),
            )
        }
    }
}
