package com.brandon.campingmate

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brandon.campingmate.SearchFragment.Companion.campList
import com.brandon.campingmate.databinding.ItemBigCampBinding
import com.bumptech.glide.Glide

class SearchListAdapter(
) : ListAdapter<CampModel, SearchListAdapter.SearchViewHolder>(
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
    abstract class SearchViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun onBind(item: CampModel)
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
    ) : SearchViewHolder(binding.root){
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

            binding.root.setOnClickListener {
                val myData = CampModel(addr1=item.addr1, contentId = item.contentId, facltNm = item.facltNm,
                    wtrplCo = item.wtrplCo, brazierCl = item.brazierCl, sbrsCl = item.sbrsCl, posblFcltyCl = item.posblFcltyCl,
                    hvofBgnde = item.hvofBgnde, hvofEnddle = item.hvofEnddle, toiletCo = item.toiletCo, swrmCo = item.swrmCo,
                    featureNm = item.featureNm, induty = item.induty, tel = item.tel, homepage = item.homepage, resveCl = item.resveCl,
                    siteBottomCl1 = item.siteBottomCl1, siteBottomCl2 = item.siteBottomCl2, siteBottomCl3 = item.siteBottomCl3,
                    siteBottomCl4 = item.siteBottomCl4, siteBottomCl5 = item.siteBottomCl5, glampInnerFclty = item.glampInnerFclty,
                    caravInnerFclty = item.caravInnerFclty, intro = item.intro, themaEnvrnCl = item.themaEnvrnCl)

                val intent = Intent(binding.root.context, CampDetailActivity::class.java).apply {
                    putExtra("campData", myData)
                }
                binding.root.context.startActivity(intent)
            }
        }
    }
}