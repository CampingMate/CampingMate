import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.brandon.campingmate.R
import com.brandon.campingmate.databinding.FragmentImagePickerBottomSheetBinding
import com.brandon.campingmate.presentation.common.colorpicker.ImageItem
import com.brandon.campingmate.presentation.common.colorpicker.ImagePickerAdapter
import com.brandon.campingmate.presentation.common.colorpicker.ImagePickerViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import timber.log.Timber

class ImagePicker(
    private val onSelectionComplete: ((List<Uri>) -> Unit),
    private val maxSelection: Int = 5,
    private val preselectedImages: List<Uri> = listOf(),
    private val gridCount: Int = 3,
    private val gridSpacing: Int = 4,
    private val includeEdge: Boolean = false,
    private val cornerRadius: Float = 0f,
    private val bottomSheetUsageDescription: String? = null,
) : BottomSheetDialogFragment() {

    private var _binding: FragmentImagePickerBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ImagePickerViewModel by viewModels()

    private val imagePickerAdapter = ImagePickerAdapter(::onImageSelected)
    private var selectionSnackbar: Snackbar? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImagePickerBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        setupRecyclerView()
        observeImageChange()
        setPickerMaxSelection()
        restoreSelectedImagesState()
        setupSnackbar()
    }

    private fun setupSnackbar() {
        val shapeDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = floatArrayOf(50f, 50f, 50f, 50f, 50f, 50f, 50f, 50f)
        }

        selectionSnackbar = Snackbar.make(binding.root, "", Snackbar.LENGTH_SHORT).apply {
            val snackbarView = view
            snackbarView.background = shapeDrawable
            val layoutParams = snackbarView.layoutParams as CoordinatorLayout.LayoutParams
            layoutParams.gravity = Gravity.BOTTOM or Gravity.CENTER
            layoutParams.width = FrameLayout.LayoutParams.WRAP_CONTENT
            layoutParams.setMargins(20, 20, 20, 20) // 스낵바 여백 설정
            snackbarView.layoutParams = layoutParams
        }

    }

    private fun setupView() = with(binding) {
        setupRoundedCorners(cornerRadius)
        if (bottomSheetUsageDescription != null) {
            tvDescription.text = bottomSheetUsageDescription
        }
    }

    private fun setupRoundedCorners(cornerRadius: Float) {
        val dpValue = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, cornerRadius, resources.displayMetrics
        )
        val shapeDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            color = ColorStateList.valueOf(Color.WHITE) // 배경색 설정
            cornerRadii = floatArrayOf(dpValue, dpValue, dpValue, dpValue, 0f, 0f, 0f, 0f) // 위쪽 모서리만 둥글게
        }
        binding.root.background = shapeDrawable
    }

    override fun onPause() {
        Timber.tag("PICK").d("onPause 호출됨")
        selectionSnackbar?.dismiss()
        super.onPause()
    }

    override fun onStop() {
        Timber.tag("PICK").d("onStop 호출됨")
        super.onStop()
    }

    override fun onDestroyView() {
        Timber.tag("PICK").d("onDestroyView 호출됨")
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        Timber.tag("PICK").d("onDestroy 호출됨")
        onSelectionComplete(viewModel.selectedImages)
        super.onDestroy()
    }

    private fun setPickerMaxSelection() {
        viewModel.setMaxSelection(maxSelection)
    }

    private fun restoreSelectedImagesState() {
        if (preselectedImages.isEmpty()) {
            viewModel.loadImagesFromSource(requireContext())
        } else {
            viewModel.loadImagesFromSource(requireContext(), preselectedImages)
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewImages.layoutManager = GridLayoutManager(context, gridCount)
        binding.recyclerViewImages.adapter = imagePickerAdapter

        val gridSpacingDecorator = GridSpacingItemDecoration(gridCount, gridSpacing, includeEdge)
        binding.recyclerViewImages.addItemDecoration(gridSpacingDecorator)
    }

    private fun observeImageChange() {
        lifecycleScope.launch {
            viewModel.imageItem.collect { imageItems ->
                (binding.recyclerViewImages.adapter as ImagePickerAdapter).submitList(imageItems)
            }
        }
        lifecycleScope.launch {
            viewModel.selectedImageCount.collect { total ->
                Timber.tag("PICK").d("이미지 토탈 개수 업데이트 $total:")
                if (total > 0) {
                    if (total == maxSelection) {
                        selectionSnackbar?.setBackgroundTint(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.brandColor
                            )
                        )
                    } else {
                        selectionSnackbar?.setBackgroundTint(Color.GRAY)
                    }
                    selectionSnackbar?.setText("Total: ($total/$maxSelection)") // 스낵바의 텍스트 업데이트
                    selectionSnackbar?.show()
                } else {
                    selectionSnackbar?.dismiss()
                }
            }
        }
    }

    private fun onImageSelected(imageItem: ImageItem) {
        viewModel.toggleImageSelection(imageItem)
    }

}
