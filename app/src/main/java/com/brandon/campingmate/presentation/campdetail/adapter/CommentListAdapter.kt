package com.brandon.campingmate.presentation.campdetail.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brandon.campingmate.databinding.ItemDetailCommentBinding
import com.brandon.campingmate.domain.model.CampCommentEntity
import com.brandon.campingmate.presentation.campdetail.CampDetailActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class CommentListAdapter(
    private val onClick: (String) -> Unit
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
            onClick
        )

    override fun onBindViewHolder(holder: CampCommentViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    class CampCommentItemViewHolder(
        private val binding: ItemDetailCommentBinding,
        private val onClick: (String) -> Unit,
    ) : CampCommentViewHolder(binding.root){
        override fun onBind(item: CampCommentEntity) = with(binding){
            if(item !is CampCommentEntity) {
                return@with
            }
            tvCommentContent.text = item.content
            tvCommentTime.text = item.date
            if(item.imageUrl.toString().isBlank()){
                ivCommentImg.visibility = View.GONE
            }
            Glide.with(binding.root)
                .load(item.imageUrl)
                .into(ivCommentImg)
            ivCommentImg.clipToOutline = true
            ivCommentImg.setOnClickListener {
                onClick(item.imageUrl.toString())
            }
            tvCommentUsername.text = item.userName.toString()
            Glide.with(binding.root)
                .load(item.userProfile)
                .into(ivUserProfile)
            ivSideMenu.setOnClickListener {
                showBottomSheetCommentMenu(item)
            }
        }
        private fun showBottomSheetCommentMenu(comment: CampCommentEntity) {
            val context = binding.root.context as? CampDetailActivity
            context?.showBottomSheetCommentMenu(comment)
        }

    }
}