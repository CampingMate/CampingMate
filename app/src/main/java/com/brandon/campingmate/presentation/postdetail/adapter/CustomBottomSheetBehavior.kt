package com.brandon.campingmate.presentation.postdetail.adapter

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.brandon.campingmate.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import timber.log.Timber

/**
 * 현재 안씀
 */
class CustomBottomSheetBehavior<V : View> : BottomSheetBehavior<V> {

    private var allowUserDragging = true

    constructor() : super()

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    init {
        state = STATE_HIDDEN
        isFitToContents = true
    }


    // 이 함수는 터치된 위치가 cl_container 내부인지 확인합니다.
    private fun isTouchInsideClContainer(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        // cl_container 뷰를 찾습니다.
        val clContainer = parent.findViewById<View>(R.id.cl_sheet_handle) ?: return false

        // cl_container의 위치를 가져옵니다.
        val location = IntArray(2)
        clContainer.getLocationOnScreen(location)

        val x = location[0]
        val y = location[1]

        val touchX = event.rawX
        val touchY = event.rawY

        // 로깅
        Timber.tag("CustomBehavior")
            .d("cl_container position: X1=$x, X2=${x + clContainer.width}, Y1=$y, Y2=${y + clContainer.height}")
        Timber.tag("CustomBehavior").d("Touch position: X=$touchX, Y=$touchY")


        // 터치 이벤트의 위치가 cl_container 내부인지 확인합니다.
        return event.rawX >= x && event.rawX <= x + clContainer.width &&
                event.rawY >= y && event.rawY <= y + clContainer.height
    }

    override fun onTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        // 터치 이벤트가 cl_container 내부에서 발생했는지 확인합니다.
        if (isTouchInsideClContainer(parent, child, event)) {
            Timber.tag("CustomBehavior").d("내부터치")
            isDraggable = true
        }
        Timber.tag("CustomBehavior").d("외부 터치")
        isDraggable = false
        return super.onTouchEvent(parent, child, event)
    }

//    /**
//     * 리사이클러뷰가 터치된 경우를 판별하는 메서드
//     */
////    private fun isTouchInsideRecyclerView(child: View, event: MotionEvent): Boolean {
////        if (child is RecyclerView) {
////            Timber.tag("BOTTOM").d("Touch child : $child")
////            val childCoords = IntArray(2)
////            child.getLocationInWindow(childCoords)
////            val x = event.rawX.toInt()
////            val y = event.rawY.toInt()
////
////            return x >= childCoords[0] && x <= childCoords[0] + child.width && y >= childCoords[1] && y <= childCoords[1] + child.height
////        }
////        return false
////    }
//
////    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
////        Timber.d("onInterceptTouchEvent: action=${event.action}, allowUserDragging=$allowUserDragging")
////        if (!allowUserDragging) {
////            return false
////        }
////        if (event.action == MotionEvent.ACTION_DOWN && isTouchInsideRecyclerView(child, event)) {
////            // 리사이클러뷰 내에서의 터치를 리사이클러뷰가 처리하도록 함
////            allowUserDragging = false
////        } else {
////            // 그 외의 경우 바텀시트의 스크롤을 허용
////            allowUserDragging = true
////        }
////        return super.onInterceptTouchEvent(parent, child, event)
////    }
//
//
//
//    override fun onTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
////        Timber.d("onTouchEvent: action=${event.action}")
//
////        Timber.tag("BOTTOM").d("parent view name: ${parent.javaClass.simpleName}")
////        Timber.tag("BOTTOM").d("child view name: ${child.javaClass.simpleName}")
////        Timber.tag("BOTTOM").d("child view: ${child}")
//
////        Timber.tag("BOTTOM").d("Current: $child")
////        val bottom = child
////        if(bottom is ViewGroup){
////            for(i in 0 until bottom.childCount){
////                val child = bottom.getChildAt(i)
////                Timber.tag("BOTTOM").d("Child: $child")
////            }
////        }
////
////
////        val touchedView = findTouchedView(parent, event.x, event.y)
////        touchedView?.let {
////            // 여기에서 touchedView를 처리합니다. 예: 로그 출력
////            Timber.tag("BOTTOM").d("Touched view: ${it.javaClass.simpleName}")
////        }
////        if (child.id == R.id.bottomSheetLayout) { // include 태그에 부여된 id
////            // bottomSheetLayout 내의 자식 뷰들을 순회하며 RecyclerView 찾기
////            val recyclerView = findRecyclerViewInChildren(child)
////            recyclerView?.let {
////                if (isPointInsideViewBounds(it, event.rawX, event.rawY)) {
////                    // RecyclerView 내부에서 터치 이벤트가 발생했습니다.
////                    Timber.tag("BOTTOM").d("RecyclerView was touched.")
////                    return true
////                }
////            }
////        }
//
//        // 그 외의 경우, 바텀시트의 기본 동작 수행
//        return false
//        return super.onTouchEvent(parent, child, event)
//    }
//
//    private fun findRecyclerViewInChildren(view: View): RecyclerView? {
//        if (view is RecyclerView) {
//            return view
//        } else if (view is ViewGroup) {
//            for (i in 0 until view.childCount) {
//                val child = view.getChildAt(i)
//                val recyclerView = findRecyclerViewInChildren(child)
//                if (recyclerView != null) return recyclerView
//            }
//        }
//        return null
//    }
//
//    private fun isPointInsideViewBounds(view: View, x: Float, y: Float): Boolean {
//        val location = IntArray(2)
//        view.getLocationOnScreen(location)
//        val viewX = location[0]
//        val viewY = location[1]
//        // 뷰의 경계 내에서 좌표가 위치하는지 확인
//        return x >= viewX && x <= viewX + view.width && y >= viewY && y <= viewY + view.height
//    }

    companion object {
        fun <V : View> from(view: V): CustomBottomSheetBehavior<V> {
            val params = view.layoutParams as? CoordinatorLayout.LayoutParams
                ?: throw IllegalArgumentException("The view is not a child of CoordinatorLayout")
            return params.behavior as? CustomBottomSheetBehavior<V>
                ?: throw IllegalArgumentException("The view is not associated with CustomBottomSheetBehavior")
        }
    }
}
