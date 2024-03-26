package com.brandon.campingmate.utils

import android.content.Context
import android.view.View
import androidx.appcompat.widget.SearchView

fun Float.toPx(context: Context): Int = (this * context.resources.displayMetrics.density + 0.5f).toInt()

fun Int.toPx(context: Context): Int = (this * context.resources.displayMetrics.density + 0.5f).toInt()

fun View.setDebouncedOnClickListener(debounceTime: Long = 1000L, action: (View) -> Unit) {
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0

        override fun onClick(v: View?) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime >= debounceTime) {
                lastClickTime = currentTime
                v?.let { action(it) }
            }
        }
    })
}

fun SearchView.clearText() {
    this.setQuery("", false)
}