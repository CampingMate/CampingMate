package com.brandon.campingmate.presentation.campdetail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brandon.campingmate.databinding.ItemDetailCommentBinding
import com.brandon.campingmate.presentation.campdetail.CampCommentEntity
import com.bumptech.glide.Glide

class CommentListAdapter(
) : ListAdapter<CampCommentEntity, CommentListAdapter.CampCommentViewHolder>(
    object : DiffUtil.ItemCallback<CampCommentEntity>(){
        override fun areItemsTheSame(
            oldItem: CampCommentEntity,
            newItem: CampCommentEntity
        ): Boolean = oldItem.userId == newItem.userId

        override fun areContentsTheSame(
            oldItem: CampCommentEntity,
            newItem: CampCommentEntity
        ): Boolean = oldItem == newItem
    }
){
    abstract class CampCommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun onBind(item: CampCommentEntity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CampCommentViewHolder =
        CampCommentItemViewHolder(
            ItemDetailCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )

    override fun onBindViewHolder(holder: CampCommentViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    class CampCommentItemViewHolder(
        private val binding: ItemDetailCommentBinding,
    ) : CampCommentViewHolder(binding.root){
        override fun onBind(item: CampCommentEntity) = with(binding){
            if(item !is CampCommentEntity) {
                return@with
            }
            tvCommentContent.text = item.content
            tvCommentTime.text = item.date
            if(item.imageUrl == null){
                ivCommentImg.visibility = View.GONE
            }
            Glide.with(binding.root)
                .load(item.imageUrl)
                .into(ivCommentImg)
            ivCommentImg.clipToOutline = true
            tvCommentUsername.text = item.userName.toString()
        }
    }
}
//db설계할때 유저이름, 제목 필요없음, 캠핑장id도