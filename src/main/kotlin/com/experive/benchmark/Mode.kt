package com.experive.benchmark

sealed class Mode(val name: String) {
  abstract fun interpret(amount: Double): Double
  abstract fun unit(baseUnit: String): String
  fun getFormatted(amount: Double, baseUnit: String): String {
    return String.format("%.2f %s", amount, baseUnit)
  }
  class Avgt(): Mode("avgt")  {
    override fun interpret(amount: Double) = amount
    override fun unit(baseUnit: String): String = baseUnit
  }

  class Throughput(): Mode("Throughput")  {
    override fun interpret(amount: Double) = 1 / amount
    override fun unit(baseUnit: String): String = "ops/$baseUnit"
  }

}