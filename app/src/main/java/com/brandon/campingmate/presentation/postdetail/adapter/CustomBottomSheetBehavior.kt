package com.brandon.campingmate.presentation.postdetail.adapter

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior

class CustomBottomSheetBehavior<V : View>(context: Context, attrs: AttributeSet) :
    BottomSheetBehavior<V>(context, attrs) {

    private var isUpwardScrollEnabled: Boolean = false

    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && state == STATE_COLLAPSED) {
            isUpwardScrollEnabled = true
        }
        return super.onInterceptTouchEvent(parent, child, event)
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        if (!isUpwardScrollEnabled) {
            parent.requestDisallowInterceptTouchEvent(false)
        }
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            isUpwardScrollEnabled = false
        }
        return super.onTouchEvent(parent, child, event)
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: V,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        isUpwardScrollEnabled = axes == ViewCompat.SCROLL_AXIS_VERTICAL
        return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
    }
}
