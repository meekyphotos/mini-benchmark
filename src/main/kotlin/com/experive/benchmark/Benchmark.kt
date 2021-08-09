package com.experive.benchmark

import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

class Benchmark(val underTest: KFunction<*>, val args: Array<out Any?>) {
    val name = nameOf(underTest)

    companion object {
        fun nameOf(fn: KFunction<*>): String {
            return fn.javaMethod?.declaringClass?.simpleName + '_' + fn.name
        }
    }
}
