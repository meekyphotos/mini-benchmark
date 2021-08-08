package com.experive.benchmark

import com.experive.benchmark.utils.MarkdownTableWriter
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.reflect.KFunction
import kotlin.system.measureNanoTime

class BenchmarkRunner(
    private val warmup: Int = 5,
    private val iterations: Int = 10,
    private val timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
    private val mode: Mode = Mode.Avgt()
) {

    private var beforeEach: Runnable = Runnable {}
    private var afterEach: Runnable = Runnable {}
    private var beforeAll: Runnable = Runnable {}
    private var afterAll: Runnable = Runnable {}
    private val tests = ArrayList<Benchmark>()

    fun doBeforeEach(b: Runnable): BenchmarkRunner {
        this.beforeEach = b
        return this
    }

    fun doAfterEach(b: Runnable): BenchmarkRunner {
        this.afterEach = b
        return this
    }

    fun doBeforeAll(b: Runnable): BenchmarkRunner {
        this.beforeAll = b
        return this
    }

    fun doAfterAll(b: Runnable): BenchmarkRunner {
        this.afterAll = b
        return this
    }

    fun add(underTest: KFunction<*>, vararg args: Any?): BenchmarkRunner {
        tests.add(Benchmark(underTest, args))
        return this
    }

    fun runAll() {
        println("# Warmup: $warmup iterations")
        println("# Measurement: $iterations iterations")
        val unit = mode.unit(symbol(timeUnit))
        println("# Benchmark mode: $mode, $unit")
        val results = ArrayList<StatRow>()
        tests.forEach { bench ->
            beforeAll.run()
            try {
                doWarmUp(bench, unit)
                var maxExecution = 0.0
                var sum = 0.0
                println("# Benchmarking")
                val pair = doIteration(bench, sum, maxExecution, unit)
                maxExecution = pair.first
                sum = pair.second
                val avg = sum / iterations
                results.add(
                    StatRow(
                        bench.underTest.name,
                        bench.args.joinToString(", "),
                        mode.name,
                        iterations.toString(),
                        String.format(Locale.ENGLISH, "%.3f", avg),
                        String.format(Locale.ENGLISH, "± %.3f", maxExecution - avg),
                        unit
                    )
                )
            } finally {
                afterAll.run()
            }
        }
        println()
        MarkdownTableWriter(results).print()
    }

    @SuppressWarnings("SpreadOperator")
    private fun doIteration(bench: Benchmark, sum: Double, maxExecution: Double, unit: String): Pair<Double, Double> {
        var sum1 = sum
        var maxExecution1 = maxExecution
        for (i in 1..iterations) {
            beforeEach.run()
            val nano = measureNanoTime {
                bench.underTest.call(*bench.args)
            }
            afterEach.run()
            val runValue = mode.interpret(convertDuration(timeUnit, nano.toDouble()))
            sum1 += runValue
            maxExecution1 = maxOf(maxExecution1, runValue)
            println("Iteration $i: ${mode.getFormatted(runValue, unit)}")
        }
        return Pair(maxExecution1, sum1)
    }

    @SuppressWarnings("SpreadOperator")
    private fun doWarmUp(bench: Benchmark, unit: String) {
        if (warmup > 0) {
            println("# Warming up")
            for (i in 0..warmup) {
                beforeEach.run()
                val nano = measureNanoTime {
                    bench.underTest.call(*bench.args)
                }
                afterEach.run()
                val runValue = mode.interpret(convertDuration(timeUnit, nano.toDouble()))
                println("# Warmup Iteration ( $i / $warmup ) - ${mode.getFormatted(runValue, unit)}")
            }
        }
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
            TimeUnit.NANOSECONDS -> nano * NANOSECONDS_RATE
            TimeUnit.MICROSECONDS -> nano * MICROSECONDS_RATE
            TimeUnit.MILLISECONDS -> nano * MILLISECONDS_RATE
            TimeUnit.SECONDS -> nano * SECONDS_RATE
            TimeUnit.MINUTES -> nano * MINUTES_RATE
            TimeUnit.HOURS -> nano * HOURS_RATE
            TimeUnit.DAYS -> TimeUnit.NANOSECONDS.toDays(nano.toLong()).toDouble()
        }
    }

    companion object {
        const val NANOSECONDS_RATE = 1
        const val MICROSECONDS_RATE = 0.001
        const val MILLISECONDS_RATE = 1e-6
        const val SECONDS_RATE = 1e-9
        const val MINUTES_RATE = 1.6667e-11
        const val HOURS_RATE = 2.7778e-13
    }
}
