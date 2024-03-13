package com.brandon.campingmate.presentation.common.colorpicker

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class ImagePickerViewModel : ViewModel() {

    private val _imageItem = MutableStateFlow<List<ImageItem>>(emptyList())
    val imageItem: StateFlow<List<ImageItem>> = _imageItem

    private val _selectedImageCount = MutableStateFlow(0)
    val selectedImageCount: StateFlow<Int> = _selectedImageCount

    val selectedImages: List<Uri>
        get() = imageItem.value.filter { it.isChecked }.map { it.uri }

    private var maxSelection = 5

    fun setMaxSelection(max: Int) {
        maxSelection = max
    }

    // 갤러리 또는 기타 소스로부터 이미지를 로드하고 _images StateFlow를 업데이트하는 함수
    fun loadImagesFromSource(context: Context, preselectedImages: List<Uri>? = null) {
        viewModelScope.launch {
            val imageItems = withContext(Dispatchers.IO) {
                loadImageItemsFromLocalStorage(context, preselectedImages)
            }
            _imageItem.value = imageItems
        }
    }


    // 이미지 선택 및 선택 취소를 처리하는 함수
    fun toggleImageSelection(imageItem: ImageItem) {
        val updatedList = _imageItem.value.toMutableList()
        val currentCount = updatedList.count { it.isChecked }
        val isCurrentlySelected = imageItem.isChecked

        if (!isCurrentlySelected && currentCount < maxSelection || isCurrentlySelected) {
            val index = updatedList.indexOf(imageItem)
            if (index != -1) {
                updatedList[index] = imageItem.copy(isChecked = !imageItem.isChecked)
                _imageItem.value = updatedList
                _selectedImageCount.value = updatedList.count { it.isChecked }
            } else {
                Timber.e("선택된 이미지를 찾을 수 없습니다")
            }
        } else {
            Timber.tag("PICK").d("Selection limit reached.")
        }
    }

    // 로컬 이미지 URI를 로드하는 실제 함수
    private fun loadImageItemsFromLocalStorage(
        context: Context, preselectedImages: List<Uri>?
    ): List<ImageItem> {
        val imageItemList = mutableListOf<ImageItem>()
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val contentUri: Uri = Uri.withAppendedPath(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString()
                )

                if (preselectedImages != null && preselectedImages.contains(contentUri)) {
                    imageItemList.add(ImageItem(contentUri, true))
                } else {
                    imageItemList.add(ImageItem(contentUri, false))
                }
            }
        }

        return imageItemList.toList()
    }

}
