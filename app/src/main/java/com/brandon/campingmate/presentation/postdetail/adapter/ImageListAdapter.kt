package com.brandon.campingmate.presentation.postdetail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.brandon.campingmate.databinding.ItemPostImageBinding
import timber.log.Timber

class ImageListAdapter(private var imageUrls: List<String>) :
    RecyclerView.Adapter<ImageListAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(private val binding: ItemPostImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(imageUrl: String) {
            binding.ivPostImage.load(imageUrl) {
                listener(onError = { _, _ ->
                    Timber.tag("IMAGE").e("Failed to load image from $imageUrl") // 이미지 로드 실패 시 로그 출력
                })
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemPostImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ImageViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return imageUrls.size
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.onBind(imageUrls[position])
    }

    fun setImageUrls(imageUrls: List<String>?) {
        if (imageUrls != null) {
            this.imageUrls = imageUrls
        }
    }
}