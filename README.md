# mini-benchmark

mini-benchmark is a tool for building, running, and analysing nano/micro/milli/macro benchmarks written in Kotlin.
If you need precise benchmarks, you should use JMH. 

# Usage

```kotlin
  // instantiate a benchmark runner
  val runner = BenchmarkRunner(mode = Mode.Throughput(), timeUnit = TimeUnit.MILLISECONDS)
  
  // add methods to test, with their parameters
  runner.add(this::testSleep, 1)
  
  // call runAll to execute all the methods
  runner.runAll()

```

Sample output:
```
# Warmup: 5 iterations
# Measurement: 10 iterations
# Benchmark mode: com.experive.benchmark.Mode$Throughput@7235f92b, ops/s
# Warming up
# Warmup Iteration ( 0 / 5 ) - 508.57 ops/s
# Warmup Iteration ( 1 / 5 ) - 581.36 ops/s
# Warmup Iteration ( 2 / 5 ) - 585.14 ops/s
# Warmup Iteration ( 3 / 5 ) - 564.88 ops/s
# Warmup Iteration ( 4 / 5 ) - 560.07 ops/s
# Warmup Iteration ( 5 / 5 ) - 571.33 ops/s
# Benchmarking
Iteration 1: 543.48 ops/s
Iteration 2: 546.03 ops/s
Iteration 3: 350.27 ops/s
Iteration 4: 367.46 ops/s
Iteration 5: 361.55 ops/s
Iteration 6: 345.36 ops/s
Iteration 7: 590.14 ops/s
Iteration 8: 363.82 ops/s
Iteration 9: 362.86 ops/s
Iteration 10: 543.21 ops/s
# Warming up
# Warmup Iteration ( 0 / 5 ) - 77.76 ops/s
# Warmup Iteration ( 1 / 5 ) - 63.81 ops/s
# Warmup Iteration ( 2 / 5 ) - 63.49 ops/s
# Warmup Iteration ( 3 / 5 ) - 63.18 ops/s
# Warmup Iteration ( 4 / 5 ) - 66.59 ops/s
# Warmup Iteration ( 5 / 5 ) - 62.97 ops/s
# Benchmarking
Iteration 1: 67.56 ops/s
Iteration 2: 67.95 ops/s
Iteration 3: 62.98 ops/s
Iteration 4: 67.69 ops/s
Iteration 5: 66.29 ops/s
Iteration 6: 67.97 ops/s
Iteration 7: 67.57 ops/s
Iteration 8: 63.04 ops/s
Iteration 9: 67.79 ops/s
Iteration 10: 63.87 ops/s

| Benchmark | Args |       Mode | Cnt |   Score |     Error | Units |
|-----------|------|------------|-----|---------|-----------|-------|
| testSleep |    1 | Throughput |  10 | 437.419 | ± 152.726 | ops/s |
| testSleep |   10 | Throughput |  10 |  66.271 |   ± 1.696 | ops/s |
```

## Configure your runner
|Parameter|Default|Description|
|---------|-------|-----------|
|warmup|5|Number of iterations performed before starting to record the execution. The warmup allows to reduce the overhead caused by the framework and allow the JVM to perform the necessary optimization to the code under test|
|iterations|10|Number of iterations measured, the result will always be the average of the executions, to have a reliable output you should tune the number of iterations accordingly|
|timeUnit|MILLISECONDS|Unit of measure used in the output, all measurements are done in nanos|
|mode|Mode.Avgt|There are two modes: Average time and Throughput. Throughput calculates the number of method call per unit|
