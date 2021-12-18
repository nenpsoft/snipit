package com.nenpsoft.snipit.annotation

/**
A simple utility to generate the vector drawable paths of arrows. This utility prints two paths:
the first is an arrow pointing to the top left, the second to the top right.
 */

// Size of the vector drawable.
val size = 48

// Arrow tip angle.
val alpha = Math.PI / 6.0

// Arrow tip size.
val x = 15.0

// Arrow shaft thickness.
val d = 3.0

fun main() {
    val ta = Math.tan(alpha)
    val t1 = (1 - ta) * x
    val t2 = (1 + ta) * x
    val d1 = x - d / 2
    val d2 = x + d / 2

    print(
        t1 to t2,
        d1 to d2,
        d2 to d1,
        t2 to t1
    )
}

fun f(d: Double): String {
    return "%.2f".format(d + 0.005)
}

fun print(
    v1: Pair<Double, Double>,
    v2: Pair<Double, Double>,
    v3: Pair<Double, Double>,
    v4: Pair<Double, Double>
) {
    print(
        "M${f(v1.first)},${f(v1.second)}L${f(v2.first)},${f(v2.second)}L${size - d},${size}L${size},${size - d}L${
            f(
                v3.first
            )
        },${f(v3.second)}L${f(v4.first)},${f(v4.second)}L0,0z\n"
    )
    print(
        "M${f(size - v1.first)},${f(v1.second)}L${f(size - v2.first)},${f(v2.second)}L${d},${size}L0,${size - d}L${
            f(
                size - v3.first
            )
        },${f(v3.second)}L${f(size - v4.first)},${f(v4.second)}L${size},0z\n"
    )
}