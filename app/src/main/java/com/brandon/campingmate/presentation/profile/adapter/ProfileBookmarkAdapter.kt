package com.brandon.campingmate.presentation.profile.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brandon.campingmate.R
import com.brandon.campingmate.databinding.ItemBookmarkedBinding
import com.brandon.campingmate.domain.model.CampEntity
import com.brandon.campingmate.presentation.campdetail.CampDetailActivity
import com.bumptech.glide.Glide

class ProfileBookmarkAdapter : ListAdapter<CampEntity, ProfileBookmarkAdapter.Holder>(diffUtil) {
    inner class Holder(val binding: ItemBookmarkedBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: CampEntity) {
            with(binding) {
                if (data.firstImageUrl.isNullOrBlank()) {
                    ivCampImg.setImageResource(R.drawable.default_camping)
                } else {
                    Glide.with(binding.root).load(data.firstImageUrl).into(binding.ivCampImg)
                }
                tvCampName.text = data.facltNm
                tvCampAddr.text = data.addr1
                if (data.lctCl.toString() == "[]") {
                    tvCampType.text = "[일반]"
                } else {
                    tvCampType.text = data.lctCl.toString()
                }
                tvCampInduty.text = data.induty.toString()

                binding.root.setOnClickListener {
                    val campId = data.contentId
                    val intent = Intent(binding.root.context, CampDetailActivity::class.java).apply {
                        putExtra("campData", campId)
                    }
                    binding.root.context.startActivity(intent)
                }
            }
        }
    }

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<CampEntity>() {
            override fun areItemsTheSame(oldItem: CampEntity, newItem: CampEntity): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: CampEntity, newItem: CampEntity): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemBookmarkedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}