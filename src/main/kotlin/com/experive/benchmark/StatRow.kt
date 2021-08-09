package com.experive.benchmark

import java.text.CharacterIterator
import java.text.StringCharacterIterator
import java.util.Locale

@SuppressWarnings("LongParameterList")
class StatRow(
    val benchmark: Benchmark,
    val mode: String,
    val iterations: String,
    val score: Double,
    val error: Double,
    val unit: String
) : Comparable<StatRow> {
    override fun compareTo(other: StatRow): Int = score.compareTo(other.score)
    val scoreAsString = humanReadable(score)
    val errorAsString = "± " + humanReadable(error)
    val name: String = benchmark.name
    val args: String = benchmark.args.joinToString(",")

    companion object {
        private const val ONE_K = 1000.0
        private const val ONEHUNDRED_K = 999950.0
        fun humanReadable(doubleValue: Double): String {
            var value = doubleValue
            if (-ONE_K < value && value < ONE_K) {
                return String.format(Locale.ENGLISH, "%.2f", value)
            }
            val ci: CharacterIterator = StringCharacterIterator("kMGTPE")
            while (value <= -ONEHUNDRED_K || value >= ONEHUNDRED_K) {
                value /= ONE_K
                ci.next()
            }
            return String.format(Locale.ENGLISH, "%.1f%c", value / ONE_K, ci.current())
        }
    }
}
