package com.brandon.campingmate.presentation.postdetail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brandon.campingmate.databinding.ItemPostCommentBinding

class PostDetailCommentListAdapter :
    ListAdapter<PostCommentListItem, PostDetailCommentListAdapter.ViewHolder>(object :
        DiffUtil.ItemCallback<PostCommentListItem>() {
        override fun areItemsTheSame(
            oldItem: PostCommentListItem, newItem: PostCommentListItem
        ): Boolean {
            return oldItem.commentId == newItem.commentId
        }

        override fun areContentsTheSame(
            oldItem: PostCommentListItem, newItem: PostCommentListItem
        ): Boolean {
            return true
        }

    }) {


    inner class ViewHolder(private val binding: ItemPostCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(postCommentListItem: PostCommentListItem) {
            with(binding) {
                tvUserName.text = postCommentListItem.authorName
                tvComment.text = postCommentListItem.content
                tvTimestamp.text = postCommentListItem.timestamp
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPostCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}