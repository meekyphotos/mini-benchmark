package com.experive.benchmark.utils

import com.experive.benchmark.StatRow
import kotlin.math.max

class MarkdownTableWriter(val results: List<StatRow>) {

  fun print() {
    val widths = arrayOf("Benchmark", "Args","Mode", "Cnt", "Score", "Error", "Units").map { it.length }.toIntArray()
    for (result in results) {
      widths[0] = max(result.name.length, widths[0])
      widths[1] = max(result.args.length, widths[1])
      widths[2] = max(result.mode.length, widths[2])
      widths[3] = max(result.iterations.length, widths[3])
      widths[4] = max(result.score.length, widths[4])
      widths[5] = max(result.error.length, widths[5])
      widths[6] = max(result.unit.length, widths[6])
    }
    printRow(widths,"Benchmark", "Args","Mode", "Cnt", "Score", "Error", "Units")
    printSeparator(widths)
    for (result in results) {
      printRow(widths, result.name, result.args, result.mode, result.iterations, result.score, result.error, result.unit)
    }
  }

  private fun printRow(widths: IntArray, vararg content: String) {
    for ((index, width) in widths.withIndex()) {
      print(String.format("| %${width}s ", content[index]))
    }
    println("|")
  }

  private fun printSeparator(widths: IntArray) {
    for ((index, width) in widths.withIndex()) {
      print(String.format("|-%s-", (0 until width).joinToString("") { "-" }))
    }
    println("|")
  }

}