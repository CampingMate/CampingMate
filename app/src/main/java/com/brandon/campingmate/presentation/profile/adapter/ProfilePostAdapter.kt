package com.brandon.campingmate.presentation.profile.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brandon.campingmate.databinding.ItemPostBinding
import com.brandon.campingmate.domain.model.Post
import com.brandon.campingmate.presentation.postdetail.PostDetailActivity
import com.brandon.campingmate.utils.toFormattedString
import com.bumptech.glide.Glide

class ProfilePostAdapter : ListAdapter<Post, ProfilePostAdapter.Holder>(diffUtil) {
    inner class Holder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Post) {
            with(binding) {
                if(data.imageUrls.isNullOrEmpty()){
                    ivPostImage.isVisible = false
                }else {
                    Glide.with(binding.root).load(data.imageUrls.firstOrNull()).into(ivPostImage)
                }
                tvTitle.text = data.title
                tvContent.text = data.content
                tvTimestamp.text = data.timestamp.toFormattedString()

                binding.root.setOnClickListener {
                    val postId = data.postId
                    val intent = Intent(binding.root.context, PostDetailActivity::class.java).apply {
                        putExtra("extra_post_id",postId)
                    }
                    binding.root.context.startActivity(intent)
                }
            }
        }
    }

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<Post>() {
            override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}