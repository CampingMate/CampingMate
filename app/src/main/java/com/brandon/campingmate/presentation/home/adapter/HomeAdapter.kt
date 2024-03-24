package com.brandon.campingmate.presentation.home.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brandon.campingmate.R
import com.brandon.campingmate.databinding.ItemSmallCampBinding
import com.brandon.campingmate.domain.model.HomeEntity
import com.brandon.campingmate.presentation.campdetail.CampDetailActivity
import com.bumptech.glide.Glide

class HomeAdapter(private val mContext: Context, var mItems: MutableList<HomeEntity>?) :

    RecyclerView.Adapter<HomeAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding =
            ItemSmallCampBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: HomeAdapter.Holder, position: Int) {
        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, CampDetailActivity::class.java).apply {
                putExtra("campData", mItems?.get(position)?.contentId)
            }
            mContext.startActivity(intent)
        }
        if (mItems?.get(position)?.firstImageUrl == "")
            holder.campImg.setImageResource(R.drawable.default_camping)
        else {
            val dimColor = Color.parseColor("#0D000000")
            holder.campImg.setColorFilter(dimColor, PorterDuff.Mode.SRC_ATOP)
            Glide.with(mContext)
                .load(mItems?.get(position)?.firstImageUrl)
                .into(holder.campImg)
        }

        holder.campName.text = mItems?.get(position)?.facltNm
    }

    override fun getItemCount(): Int = mItems?.size!!

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