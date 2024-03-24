package com.brandon.campingmate.presentation.home.adapter

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brandon.campingmate.R
import com.brandon.campingmate.domain.model.CampEntity
import com.brandon.campingmate.databinding.ItemBigCampBinding
import com.brandon.campingmate.presentation.campdetail.CampDetailActivity
import com.bumptech.glide.Glide

class MoreLocationListAdapter : ListAdapter<CampEntity, MoreLocationListAdapter.SearchViewHolder>(
    object : DiffUtil.ItemCallback<CampEntity>() {
        override fun areItemsTheSame(
            oldItem: CampEntity,
            newItem: CampEntity
        ): Boolean = oldItem.contentId == newItem.contentId

        override fun areContentsTheSame(
            oldItem: CampEntity,
            newItem: CampEntity
        ): Boolean = oldItem == newItem
    }
) {
    abstract class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun onBind(item: CampEntity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder =
        SearchItemViewHolder(
            ItemBigCampBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    class SearchItemViewHolder(
        private val binding: ItemBigCampBinding,
    ) : SearchViewHolder(binding.root) {
        override fun onBind(item: CampEntity) = with(binding) {
            if (item !is CampEntity) {
                return@with
            }
            if(item.firstImageUrl.isNullOrBlank()){
                ivBigItem.setImageResource(R.drawable.default_camping)
            } else{
                Glide.with(binding.root)
                    .load(item.firstImageUrl)
                    .into(binding.ivBigItem)
            }
            val dimColor = Color.parseColor("#0D000000")
            ivBigItem.setColorFilter(dimColor, PorterDuff.Mode.SRC_ATOP)
            if(item.lineIntro.isNullOrBlank()){
                tvBigItemLineIntro.visibility = View.GONE
            } else{
                tvBigItemLineIntro.text = item.lineIntro
                tvBigItemLineIntro.visibility = View.VISIBLE
            }
            tvBigItemName.text = item.facltNm
            tvBigItemAddr.text = item.addr1
            if(item.lctCl.toString() == "[]"){
                tvBigItemTag.text = "[일반]"
            } else{
                tvBigItemTag.text = item.lctCl.toString()
            }
            ivBigItem.clipToOutline = true

            binding.root.setOnClickListener {
                val myId = item.contentId
                val intent = Intent(binding.root.context, CampDetailActivity::class.java).apply {
                    putExtra("campData", myId)
                }
                binding.root.context.startActivity(intent)
            }
        }
    }
}