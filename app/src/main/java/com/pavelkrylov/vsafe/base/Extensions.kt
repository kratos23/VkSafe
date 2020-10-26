package com.pavelkrylov.vsafe.base

import android.content.res.Resources

val Int.pxToDp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

/**
 * Converts DP to Pixel.
 */
val Int.dpToPx: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val Int.dpToPxFloat : Float
get() = (this * Resources.getSystem().displayMetrics.density)

