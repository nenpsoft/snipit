package com.nenpsoft.snipit.common

import android.graphics.RectF

fun RectF.shrink(dx: Float, dy: Float) {
    var dx2 = minOf(this.width() / 2F, dx / 2F)
    var dy2 = minOf(this.height() / 2F, dy / 2F)

    this.left += dx2
    this.right -= dx2
    this.top += dy2
    this.bottom -= dy2
}