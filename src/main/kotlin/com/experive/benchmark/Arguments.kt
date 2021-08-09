package com.experive.benchmark

typealias Parameters = () -> Array<out Any?>

fun arguments(vararg arguments: Any?): Parameters = { arguments }
