package com.brandon.campingmate.presentation.postdetail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.brandon.campingmate.databinding.ItemPostdetailImageBinding
import timber.log.Timber

class PostImageListAdapter(private val onClick: (String) -> Unit) :
    ListAdapter<String, PostImageListAdapter.ImageViewHolder>(object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return true
        }
    }) {

    inner class ImageViewHolder(private val binding: ItemPostdetailImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(imageUrl: String) {
            binding.ivPostImage.load(imageUrl) {
                listener(onError = { _, _ ->
                    Timber.tag("IMAGE").e("Failed to load image from $imageUrl") // 이미지 로드 실패 시 로그 출력
                })
            }
            binding.root.setOnClickListener {
                onClick(imageUrl)
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


    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

}