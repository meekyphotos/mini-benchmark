package com.experive.benchmark

import java.util.Locale

sealed class Mode(val name: String) {
    abstract fun interpret(amount: Double): Double
    abstract fun unit(baseUnit: String): String
    abstract fun bestOf(a: StatRow?, b: StatRow): StatRow
    abstract fun direction(): Int

    fun getFormatted(amount: Double, baseUnit: String): String {
        return String.format(Locale.ENGLISH, "%.2f %s", amount, baseUnit)
    }

    class Avgt : Mode("avgt") {
        override fun interpret(amount: Double) = amount
        override fun unit(baseUnit: String): String = baseUnit
        override fun bestOf(a: StatRow?, b: StatRow): StatRow {
            if (a == null) return b
            return minOf(a, b)
        }

        override fun direction(): Int = 1
    }

    class Throughput : Mode("Throughput") {
        override fun interpret(amount: Double) = 1 / amount
        override fun unit(baseUnit: String): String = "ops/$baseUnit"
        override fun bestOf(a: StatRow?, b: StatRow): StatRow {
            if (a == null) return b
            return maxOf(a, b)
        }

        override fun direction(): Int = -1
    }
}
