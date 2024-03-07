package com.brandon.campingmate.presentation.profile.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
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
                    val myData = CampEntity(
                        addr1 = data.addr1,
                        contentId = data.contentId,
                        facltNm = data.facltNm,
                        wtrplCo = data.wtrplCo,
                        brazierCl = data.brazierCl,
                        sbrsCl = data.sbrsCl,
                        posblFcltyCl = data.posblFcltyCl,
                        hvofBgnde = data.hvofBgnde,
                        hvofEnddle = data.hvofEnddle,
                        toiletCo = data.toiletCo,
                        swrmCo = data.swrmCo,
                        featureNm = data.featureNm,
                        induty = data.induty,
                        tel = data.tel,
                        homepage = data.homepage,
                        resveCl = data.resveCl,
                        siteBottomCl1 = data.siteBottomCl1,
                        siteBottomCl2 = data.siteBottomCl2,
                        siteBottomCl3 = data.siteBottomCl3,
                        siteBottomCl4 = data.siteBottomCl4,
                        siteBottomCl5 = data.siteBottomCl5,
                        glampInnerFclty = data.glampInnerFclty,
                        caravInnerFclty = data.caravInnerFclty,
                        intro = data.intro,
                        themaEnvrnCl = data.themaEnvrnCl
                    )

                    val intent = Intent(binding.root.context, CampDetailActivity::class.java).apply {
                        putExtra("campData", myData)
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