package com.brandon.campingmate.utils

import android.content.Context

fun Float.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density + 0.5f).toInt()

fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density + 0.5f).toInt()
