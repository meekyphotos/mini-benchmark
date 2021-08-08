package com.experive.benchmark

import com.experive.benchmark.utils.MarkdownTableWriter
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.reflect.KFunction
import kotlin.system.measureNanoTime

class BenchmarkRunner(
    /**
     * Number of iterations performed before starting to record the execution.
     * The warmup allows to reduce the overhead caused by the framework and
     * allow the JVM to perform the necessary optimization to the code under test
     */
    private val warmup: Int = 5,

    /**
     * Number of iterations measured, the result will always be the average
     * of the executions, to have a reliable output you should tune the number
     * of iterations accordingly
     */
    private val iterations: Int = 10,

    /**
     * Unit of measure used in the output, all measurements are done in nanos regardless of this parameter
     */
    private val timeUnit: TimeUnit = TimeUnit.MILLISECONDS,

    /**
     * If iteration is greater than throttle, then print an output line once every _throttle_ times
     */
    private val throttle: Int = THROTTLE,

    /**
     * Display mode: Average time or Throughput
     */
    private val mode: Mode = Mode.Avgt()
) {
    init {
        check(iterations >= 1) { "Iteration count should be greater than zero" }
        check(warmup >= 0) { "Warmup cannot be negative" }
    }

    private var beforeEach: Runnable = Runnable {}
    private var afterEach: Runnable = Runnable {}
    private var beforeAll: Runnable = Runnable {}
    private var afterAll: Runnable = Runnable {}
    private val tests = ArrayList<Benchmark>()

    /**
     * Register a runnable that is execute before each execution (incl. warmup)
     */
    fun doBeforeEach(b: Runnable): BenchmarkRunner {
        this.beforeEach = b
        return this
    }

    /**
     * Register a runnable that is executed after each execution (incl. warmup)
     */
    fun doAfterEach(b: Runnable): BenchmarkRunner {
        this.afterEach = b
        return this
    }

    /**
     * Register a runnable that is executed before each benchmark
     */
    fun doBeforeAll(b: Runnable): BenchmarkRunner {
        this.beforeAll = b
        return this
    }

    /**
     * Register a runnable that is executed after each benchmark
     */
    fun doAfterAll(b: Runnable): BenchmarkRunner {
        this.afterAll = b
        return this
    }

    /**
     * Add a new benchmark to the comparison
     */
    fun add(underTest: KFunction<*>, vararg args: Any?): BenchmarkRunner {
        tests.add(Benchmark(underTest, args))
        return this
    }

    /**
     * Execute all registered benchmark
     */
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
                println("# Benchmarking")
                val (maxExecution, sum) = doIteration(bench, unit)
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
    private fun doIteration(bench: Benchmark, unit: String): Pair<Double, Double> {
        var sum = 0.0
        var maxExecution = 0.0
        for (i in 1..iterations) {
            beforeEach.run()
            val nano = measureNanoTime {
                bench.underTest.call(*bench.args)
            }
            afterEach.run()
            val runValue = mode.interpret(convertDuration(timeUnit, nano.toDouble()))
            sum += runValue
            maxExecution = maxOf(maxExecution, runValue)
            if (i % throttle == 0 || iterations < throttle) {
                println("Iteration $i: ${mode.getFormatted(runValue, unit)}")
            }
        }
        return Pair(maxExecution, sum)
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
                if (i % throttle == 0 || warmup < throttle) {
                    println("# Warmup Iteration ( $i / $warmup ) - ${mode.getFormatted(runValue, unit)}")
                }
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
        private const val NANOSECONDS_RATE = 1
        private const val MICROSECONDS_RATE = 0.001
        private const val MILLISECONDS_RATE = 1e-6
        private const val SECONDS_RATE = 1e-9
        private const val MINUTES_RATE = 1.6667e-11
        private const val HOURS_RATE = 2.7778e-13
        private const val THROTTLE = 1000
    }
}
