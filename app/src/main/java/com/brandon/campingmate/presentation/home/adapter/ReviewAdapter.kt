package com.brandon.campingmate.presentation.home.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brandon.campingmate.R
import com.brandon.campingmate.databinding.ItemReviewCampBinding
import com.brandon.campingmate.domain.model.HomeEntity
import com.brandon.campingmate.presentation.campdetail.CampDetailActivity
import com.bumptech.glide.Glide

class ReviewAdapter(private val mContext: Context, var mItems: MutableList<HomeEntity>) : RecyclerView.Adapter<ReviewAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewAdapter.Holder {
        val binding = ItemReviewCampBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun getItemCount() = mItems.size

    override fun onBindViewHolder(holder: ReviewAdapter.Holder, position: Int) {
        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, CampDetailActivity::class.java).apply {
                putExtra("campData", mItems[position].contentId)
            }
            mContext.startActivity(intent)
        }

        if (mItems[position].firstImageUrl == "")
            holder.campImg.setImageResource(R.drawable.default_camping)
        else {
            Glide.with(mContext)
                .load(mItems[position].firstImageUrl)
                .into(holder.campImg)
        }

        val indutyList = listOf<String?>(mItems[position].induty1, mItems[position].induty2, mItems[position].induty3, mItems[position].induty4)
        var category:String = ""
        indutyList.forEach { it ->
            if(!it.isNullOrEmpty())
                category += "${it} "
        }
        holder.campCategory.text = category
        holder.campTitle.text = mItems[position].facltNm
        holder.campAddress.text = mItems[position].addr1
        holder.campReviewSize.text = mItems[position].commentList.size.toString()

        if(mItems[position].commentList.isNullOrEmpty()) {
            holder.campReviewLayout.visibility = View.GONE
            holder.campReview.visibility = View.GONE
        }else{
            holder.campReview.text = mItems[position].commentList.last().get("content").toString()
        }
    }

    inner class Holder(binding: ItemReviewCampBinding) : RecyclerView.ViewHolder(binding.root){
        val campImg = binding.ivCampReviewImg
        val campCategory = binding.tvReviewCategory
        val campTitle = binding.tvReviewTitle
        val campAddress = binding.tvReviewAddress
        val campReview = binding.tvReviewContent
        val campReviewLayout = binding.cvReviewLayout
        val campReviewSize = binding.tvReviewCount
    }
}