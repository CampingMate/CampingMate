package com.brandon.campingmate.presentation.home.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brandon.campingmate.R
import com.brandon.campingmate.databinding.ItemMiddleCampBinding
import com.brandon.campingmate.domain.model.CampEntity
import com.brandon.campingmate.domain.model.HomeEntity
import com.brandon.campingmate.presentation.campdetail.CampDetailActivity
import com.bumptech.glide.Glide

class PetAdapter(private val mContext: Context, var mItems: MutableList<HomeEntity?>) :
    RecyclerView.Adapter<PetAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemMiddleCampBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: PetAdapter.Holder, position: Int) {
        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, CampDetailActivity::class.java).apply {
                putExtra("campData", mItems[position]?.contentId)
            }
            mContext.startActivity(intent)
        }
        if (mItems[position]?.firstImageUrl == "")
            holder.campImg.setImageResource(R.drawable.ic_login_img)
        else {
            Glide.with(mContext)
                .load(mItems[position]?.firstImageUrl)
                .into(holder.campImg)
        }

        holder.campName.text = mItems[position]?.facltNm
        holder.campIntro.text = mItems[position]?.lineIntro

        val addr = mItems[position]?.addr1
        val split = addr?.split(" ")
        holder.campCity.text = "${split?.get(0)} ${split?.get(1)}"
    }

    override fun getItemCount() = mItems.size

    inner class Holder(binding: ItemMiddleCampBinding) : RecyclerView.ViewHolder(binding.root) {
        val campImg = binding.ivCampPetImg
        val campName = binding.tvCampName
        val campIntro = binding.tvLineIntro
        val campCity = binding.tvDistrict
//        val campLineIntro
    }
}