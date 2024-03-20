package com.brandon.campingmate.presentation.postdetail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.brandon.campingmate.databinding.ItemPostdetailImageBinding
import timber.log.Timber

class PostDetailImageListAdapter(private var imageUrls: List<String>) :
    RecyclerView.Adapter<PostDetailImageListAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(private val binding: ItemPostdetailImageBinding) :
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
        val binding = ItemPostdetailImageBinding.inflate(
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