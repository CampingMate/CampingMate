package com.brandon.campingmate.presentation.postdetail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brandon.campingmate.databinding.ItemPostCommentBinding
import com.brandon.campingmate.databinding.ItemPostLoadingBinding
import com.brandon.campingmate.databinding.ItemPostUnknownBinding
import com.bumptech.glide.Glide

class PostCommentListAdapter :
    ListAdapter<PostCommentListItem, PostCommentListAdapter.PostCommentViewHolder>(object :
        DiffUtil.ItemCallback<PostCommentListItem>() {
        override fun areItemsTheSame(
            oldItem: PostCommentListItem, newItem: PostCommentListItem
        ): Boolean {
            return when {
                oldItem is PostCommentListItem.PostCommentItem && newItem is PostCommentListItem.PostCommentItem -> oldItem == newItem
                oldItem is PostCommentListItem.Loading && newItem is PostCommentListItem.Loading -> true
                else -> false
            }
        }

        override fun areContentsTheSame(
            oldItem: PostCommentListItem, newItem: PostCommentListItem
        ): Boolean {
            return when {
                oldItem is PostCommentListItem.PostCommentItem && newItem is PostCommentListItem.PostCommentItem -> true
                oldItem is PostCommentListItem.Loading && newItem is PostCommentListItem.Loading -> true
                else -> false
            }
        }

    }) {

    abstract class PostCommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun onBind(item: PostCommentListItem)
    }

    inner class PostCommentItemViewHolder(private val binding: ItemPostCommentBinding) :
        PostCommentViewHolder(binding.root) {
        override fun onBind(item: PostCommentListItem) {
            with(binding) {
                if (item is PostCommentListItem.PostCommentItem) {
                    tvUserName.text = item.authorName
                    tvComment.text = item.content
                    tvTimestamp.text = item.timestamp
                    Glide.with(binding.root).load(item.authorImageUrl).into(binding.ivUserProfile)

                    ivSideMenu.setOnClickListener {

                    }
                }
            }
        }
    }

    inner class PostCommentLoadingViewHolder(private val binding: ItemPostLoadingBinding) :
        PostCommentViewHolder(binding.root) {
        override fun onBind(item: PostCommentListItem) = Unit
    }

    inner class PostCommentUnknownViewHolder(private val binding: ItemPostUnknownBinding) :
        PostCommentViewHolder(binding.root) {
        override fun onBind(item: PostCommentListItem) = Unit
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PostCommentListItem.PostCommentItem -> PostCommentListViewType.ITEM
            is PostCommentListItem.Loading -> PostCommentListViewType.LOADING
            else -> PostCommentListViewType.UNKNOWN
        }.ordinal
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostCommentViewHolder {
        return when (PostCommentListViewType.from(viewType)) {
            PostCommentListViewType.ITEM -> PostCommentItemViewHolder(
                ItemPostCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

            PostCommentListViewType.LOADING -> PostCommentLoadingViewHolder(
                ItemPostLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

            else -> PostCommentUnknownViewHolder(
                ItemPostUnknownBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: PostCommentViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }
}