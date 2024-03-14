package com.brandon.campingmate.presentation.search.adapter

import android.content.Intent
import android.util.Log
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

class SearchListAdapter : ListAdapter<CampEntity, SearchListAdapter.SearchViewHolder>(
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
        Log.d("Search", "onBindViewHolder - Position: $position")
        holder.onBind(getItem(position))
    }

    class SearchItemViewHolder(
        private val binding: ItemBigCampBinding,
    ) : SearchViewHolder(binding.root) {
        override fun onBind(item: CampEntity) = with(binding) {
            Log.d("Search", "onBind - Item: $item")
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
//            ivBigItem.setColorFilter(R.color.blackDimmed) 먼가 이상함..
            if(item.lineIntro.isNullOrBlank()){
                tvBigItemLineIntro.visibility = View.GONE
            } else{
                tvBigItemLineIntro.text = item.lineIntro
                tvBigItemLineIntro.visibility = View.VISIBLE
            }
            Log.d("checkImage", "${item.lctCl}")
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