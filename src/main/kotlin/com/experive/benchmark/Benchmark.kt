package com.experive.benchmark

import kotlin.reflect.KFunction

internal class Benchmark(val underTest: KFunction<*>, val args: Array<out Any?>)