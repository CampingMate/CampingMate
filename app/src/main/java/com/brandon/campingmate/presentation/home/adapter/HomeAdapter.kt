package com.brandon.campingmate.presentation.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brandon.campingmate.R
import com.brandon.campingmate.databinding.ItemSmallCampBinding
import com.brandon.campingmate.domain.model.CampEntity
import com.bumptech.glide.Glide

class HomeAdapter(private val mContext: Context, var mItems: MutableList<CampEntity?>) :
    RecyclerView.Adapter<HomeAdapter.Holder>() {

    interface ItemClick {
        fun onClick(view: View, position: Int)
    }

    var itemClick: ItemClick? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemSmallCampBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: HomeAdapter.Holder, position: Int) {
        holder.itemView.setOnClickListener {
            itemClick?.onClick(it, position)
        }
        if (mItems[position]?.firstImageUrl == "")
            holder.campImg.setImageResource(R.drawable.ic_login_img)
        else {
            Glide.with(mContext)
                .load(mItems[position]?.firstImageUrl)
                .into(holder.campImg)
        }

        holder.campName.text = mItems[position]?.facltNm
    }

    override fun getItemCount() = mItems.size

    inner class Holder(binding: ItemSmallCampBinding) : RecyclerView.ViewHolder(binding.root) {
        val campImg = binding.ivHomeCampImg
        val campName = binding.tvTitle
        //후기 캠핑장
//        val campCategory
//        val campAddr
//        val campReviewNum
//        val campReview
//        //반려동물 캠핑장
//        val campCity
//        val campLineIntro
    }
}