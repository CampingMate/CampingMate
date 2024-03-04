package com.brandon.campingmate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brandon.campingmate.data.CampModel
import com.brandon.campingmate.databinding.ItemBigCampBinding
import com.bumptech.glide.Glide

class SearchListAdapter(
) : ListAdapter<CampModel, SearchListAdapter.VideoViewHolder>(
    object : DiffUtil.ItemCallback<CampModel>(){
        override fun areItemsTheSame(
            oldItem: CampModel,
            newItem: CampModel
        ): Boolean = if(oldItem is CampModel && newItem is CampModel){
            oldItem.contentId == newItem.contentId
        } else{
            oldItem == newItem
        }
        override fun areContentsTheSame(
            oldItem: CampModel,
            newItem: CampModel
        ): Boolean = oldItem == newItem
    }
){
    abstract class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun onBind(item: CampModel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder =
        VideoItemViewHolder(
            ItemBigCampBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    class VideoItemViewHolder(
        private val binding: ItemBigCampBinding,
    ) : VideoViewHolder(binding.root){
        override fun onBind(item: CampModel) = with(binding){
            if(item !is CampModel) {
                return@with
            }
            Glide.with(binding.root).load(item.firstImageUrl).into(binding.ivBigItem)
            tvBigItemName.text = item.facltNm
            tvBigItemAddr.text = item.addr1
            tvBigItemLineIntro.text = item.lineIntro
            tvBigItemTag.text = item.lctCl.toString()
            ivBigItem.clipToOutline = true
        }
    }
}