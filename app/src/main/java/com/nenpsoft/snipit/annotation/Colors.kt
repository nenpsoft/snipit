package com.nenpsoft.snipit.annotation

import android.graphics.Color as AGColor

enum class Color(val value: Int) {
    Black(AGColor.BLACK),
    White(AGColor.WHITE),
    Orange(AGColor.parseColor("#FF8717")),
    Yellow(AGColor.parseColor("#FFF800")),
    Red(AGColor.parseColor("#FE0000")),
    Pink(AGColor.parseColor("#FD0E6B")),
    Blue(AGColor.parseColor("#1A80FF")),
    Green(AGColor.parseColor("#01E901")),
}

object Colors {
    val supported =
        listOf(Color.Black, Color.White, Color.Orange,  Color.Yellow, Color.Red, Color.Pink, Color.Blue, Color.Green)
    var current: Color = Color.Black

    fun valueOf(v: Int): Color {
        return supported.find {
            it.value == v
        } ?: Color.Black
    }
}



