import android.graphics.Rect
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val spacing: Int,
    private val includeEdge: Boolean,
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view) // 아이템 위치
        val column = position % spanCount // 현재 위치의 열 인덱스
        val itemCount = state.itemCount // 아이템 총 개수
        val extraBottomSpacing = 500// 마지막 줄 바닥 여백 추가

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount
            outRect.right = (column + 1) * spacing / spanCount

            // 첫 번째 줄의 경우 상단 여백 추가
            if (position < spanCount) {
                outRect.top = spacing
            }
            // 기본 바닥 여백 추가
            outRect.bottom = spacing

            // 마지막 줄의 경우 추가적인 바닥 여백 적용
            Log.e("SIZE", "position: $position, itemCount: $itemCount")
            if (position == itemCount - 1) {
                Log.e("SIZE", "마지막 도달")
                outRect.bottom = extraBottomSpacing
            }
        } else {
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount

            if (position >= spanCount) {
                outRect.top = spacing
            }
            Log.e("SIZE", "position: $position, itemCount: $itemCount")
            // 마지막 줄의 경우에만 추가 바닥 여백 적용
            if (position == itemCount - 1) {
                Log.e("SIZE", "마지막 도달")
                outRect.bottom = extraBottomSpacing
            }
        }
    }
}
