package com.brandon.campingmate.presentation.campdetail

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView

class CustomScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : ScrollView(context, attrs, defStyle) {
    private var enableScrolling = true
    fun setScrollingEnabled(enabled: Boolean) {
        this.enableScrolling = enabled
    }
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return if (enableScrolling) {
            super.onInterceptTouchEvent(ev)
        } else {
            false
        }
    }
}