package com.experive.benchmark

import com.experive.benchmark.utils.MarkdownTableWriter
import java.util.concurrent.TimeUnit
import kotlin.reflect.KFunction
import kotlin.system.measureNanoTime

typealias Block = () -> Unit

private class Benchmark(val underTest: KFunction<*>, val args: Array<out Any?>)

class BenchmarkRunner(
  private val warmup: Int = 5,
  private val iterations: Int = 10,
  private val timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
  private val mode: Mode = Mode.Avgt()
) {

  private var beforeEach: Block = {}
  private var afterEach: Block = {}
  private var beforeAll: Block = {}
  private var afterAll: Block = {}
  private val tests = ArrayList<Benchmark>()

  fun doBeforeEach(b: Block): BenchmarkRunner {
    this.beforeEach = b
    return this
  }

  fun doAfterEach(b: Block): BenchmarkRunner {
    this.afterEach = b
    return this
  }

  fun doBeforeAll(b: Block): BenchmarkRunner {
    this.beforeAll = b
    return this
  }

  fun doAfterAll(b: Block): BenchmarkRunner {
    this.afterAll = b
    return this
  }

  fun add(underTest: KFunction<*>, vararg args: Any?) {
    tests.add(Benchmark(underTest, args))
  }

  fun runAll() {
    println("# Warmup: $warmup iterations")
    println("# Measurement: $iterations iterations")
    val unit = mode.unit(symbol(timeUnit))
    println("# Benchmark mode: $mode, $unit")
    val results = ArrayList<StatRow>()
    tests.forEach { bench ->
      beforeAll()
      try {
        if (warmup > 0) {
          println("# Warming up")
          for (i in 0..warmup) {
            beforeEach()
            val nano = measureNanoTime {
              bench.underTest.call(*bench.args)
            }
            afterEach()
            val runValue = mode.interpret(convertDuration(timeUnit, nano.toDouble()))
            println("# Warmup Iteration ( $i / $warmup ) - ${mode.getFormatted(runValue, unit)}")
          }
        }
        var maxExecution = 0.0
        var sum = 0.0
        println("# Benchmarking")
        for (i in 1..iterations) {
          beforeEach()
          val nano = measureNanoTime {
            bench.underTest.call(*bench.args)
          }
          afterEach()
          val runValue = mode.interpret(convertDuration(timeUnit, nano.toDouble()))
          sum += runValue
          maxExecution = maxOf(maxExecution, runValue)
          println("Iteration $i: ${mode.getFormatted(runValue, unit)}")
        }
        val avg = sum / iterations
        results.add(
          StatRow(
            bench.underTest.name,
            bench.args.joinToString(", "),
            mode.name,
            iterations.toString(),
            String.format("%.3f", avg),
            String.format("± %.3f", maxExecution - avg),
            unit
          )
        )
      } finally {
        afterAll()
      }
    }
    println()
    MarkdownTableWriter(results).print()
  }

  private fun symbol(target: TimeUnit): String {
    return when (target) {
      TimeUnit.NANOSECONDS -> "ns"
      TimeUnit.MICROSECONDS -> "µs"
      TimeUnit.MILLISECONDS -> "ms"
      TimeUnit.SECONDS -> "s"
      TimeUnit.MINUTES -> "m"
      TimeUnit.HOURS -> "h"
      TimeUnit.DAYS -> "d"
    }
  }

  private fun convertDuration(target: TimeUnit, nano: Double): Double {
    return when (target) {
      TimeUnit.NANOSECONDS -> nano
      TimeUnit.MICROSECONDS -> nano * 0.001
      TimeUnit.MILLISECONDS -> nano * 1e-6
      TimeUnit.SECONDS -> nano * 1e-9
      TimeUnit.MINUTES -> nano * 1.6667e-11
      TimeUnit.HOURS -> nano * 2.7778e-13
      TimeUnit.DAYS -> TimeUnit.NANOSECONDS.toDays(nano.toLong()).toDouble()
    }
  }
}