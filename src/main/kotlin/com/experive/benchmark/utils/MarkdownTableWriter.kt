package com.experive.benchmark.utils

import com.experive.benchmark.Mode
import com.experive.benchmark.StatRow
import java.util.Locale
import kotlin.math.max

class MarkdownTableWriter(private val results: List<StatRow>) {

    fun print() {
        val widths = arrayOf("Benchmark", "Args", "Mode", "Cnt", "Score", "Error", "Units")
            .map { it.length }
            .toIntArray()
        for (result in results) {
            widths[NAME_COL] = max(result.name.length, widths[NAME_COL])
            widths[ARGS_COL] = max(result.args.length, widths[ARGS_COL])
            widths[MODE_COL] = max(result.mode.length, widths[MODE_COL])
            widths[ITERATIONS_COL] = max(result.iterations.length, widths[ITERATIONS_COL])
            widths[SCORE_COL] = max(result.scoreAsString.length, widths[SCORE_COL])
            widths[ERROR_COL] = max(result.errorAsString.length, widths[ERROR_COL])
            widths[UNIT_COL] = max(result.unit.length, widths[UNIT_COL])
        }
        printRow(widths, "Benchmark", "Args", "Mode", "Cnt", "Score", "Error", "Units")
        printSeparator(widths)
        for (result in results) {
            printRow(
                widths,
                result.name,
                result.args,
                result.mode,
                result.iterations,
                result.scoreAsString,
                result.errorAsString,
                result.unit
            )
        }
    }

    fun rel(x2: Double, x1: Double, direction: Int): String {
        val chg = if (direction > 0) {
            ((x1 - x2) / x2) * PERCENT
        } else {
            ((x2 - x1) / x1) * PERCENT
        }
        return String.format(Locale.ENGLISH, "%.2f", chg) + "%"
    }

    fun printRelative(score: Double, mode: Mode) {
        val widths = arrayOf("Benchmark", "Args", "Mode", "Cnt", "Score", "Error", "Units", "Change")
            .map { it.length }
            .toIntArray()
        for (result in results) {
            widths[NAME_COL] = max(result.name.length, widths[NAME_COL])
            widths[ARGS_COL] = max(result.args.length, widths[ARGS_COL])
            widths[MODE_COL] = max(result.mode.length, widths[MODE_COL])
            widths[ITERATIONS_COL] = max(result.iterations.length, widths[ITERATIONS_COL])
            widths[SCORE_COL] = max(result.scoreAsString.length, widths[SCORE_COL])
            widths[ERROR_COL] = max(result.errorAsString.length, widths[ERROR_COL])
            widths[UNIT_COL] = max(result.unit.length, widths[UNIT_COL])
            widths[CHANGE_COL] = max(rel(result.score, score, mode.direction()).length, widths[CHANGE_COL])
        }
        printRow(widths, "Benchmark", "Args", "Mode", "Cnt", "Score", "Error", "Units", "Change")
        printSeparator(widths)
        for (result in results.sortedBy { it.score }) {
            val highlight = result.score == score
            printRow(
                widths,
                highlight,
                result.name,
                result.args,
                result.mode,
                result.iterations,
                result.scoreAsString,
                result.errorAsString,
                result.unit,
                if (highlight) {
                    rel(result.score, score, mode.direction())
                } else {
                    red(rel(result.score, score, mode.direction()))
                }
            )
        }
    }

    private fun printRow(widths: IntArray, vararg content: String) {
        for ((index, width) in widths.withIndex()) {
            print(String.format(Locale.ENGLISH, "| %${width}s ", content[index]))
        }
        println("|")
    }

    private fun printRow(widths: IntArray, highlight: Boolean, vararg content: String) {
        for ((index, width) in widths.withIndex()) {
            print("| ")
            val message = String.format(Locale.ENGLISH, "%${width}s", content[index])
            if (highlight) {
                print(green(message))
            } else {
                print(message)
            }
            print(" ")
        }
        println("|")
    }

    private fun printSeparator(widths: IntArray) {
        for (width in widths) {
            print(String.format(Locale.ENGLISH, "|-%s-", (0 until width).joinToString("") { "-" }))
        }
        println("|")
    }

    companion object {
        private const val NAME_COL = 0
        private const val ARGS_COL = 1
        private const val MODE_COL = 2
        private const val ITERATIONS_COL = 3
        private const val SCORE_COL = 4
        private const val ERROR_COL = 5
        private const val UNIT_COL = 6
        private const val CHANGE_COL = 7
        private const val PERCENT = 100.0

        fun green(text: String): String {
            return "\u001b[32m$text\u001b[0m"
        }

        fun red(text: String): String {
            return "\u001b[31m$text\u001b[0m"
        }
    }
}
