import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class LinearVerticalItemDecoration(
    private val extraBottomSpacing: Int,
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view) // 아이템 위치
        val itemCount = state.itemCount // 아이템 총 개수

        if (position == itemCount - 1) {
            outRect.bottom = extraBottomSpacing
        }
    }
}
