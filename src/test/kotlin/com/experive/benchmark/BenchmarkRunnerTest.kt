package com.experive.benchmark

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.lang.Thread.sleep
import java.util.concurrent.TimeUnit


internal class BenchmarkRunnerTest {

  @Test
  internal fun testComparisonInAvgtMode() {
    val runner = BenchmarkRunner()
    runner.add(this::testSleep, 10)
    runner.add(this::testSleep, 100)
    runner.runAll()
  }

  @Test
  internal fun testComparisonInThroughputMode() {
    val runner = BenchmarkRunner(mode = Mode.Throughput(), timeUnit = TimeUnit.SECONDS)
    runner.add(this::testSleep, 10)
    runner.add(this::testSleep, 100)
    runner.runAll()
  }

  @Test
  internal fun verifyThatHooksAreCalled() {
    val beforeAll = mockk<Block>(relaxed = true)
    val afterAll = mockk<Block>(relaxed = true)
    val beforeEach = mockk<Block>(relaxed = true)
    val afterEach = mockk<Block>(relaxed = true)
    val runner = BenchmarkRunner()
      .doBeforeAll(beforeAll)
      .doAfterAll(afterAll)
      .doAfterEach(afterEach)
      .doBeforeEach(beforeEach)
    runner.add(this::testSleep, 10)
    runner.runAll()

    verify(exactly = 1) { beforeAll() }
    verify(exactly = 1) { afterAll() }
    verify(exactly = 16) { beforeEach() }
    verify(exactly = 16) { afterEach() }

  }

  internal fun testSleep(amount: Int) { sleep(amount.toLong()) }
}