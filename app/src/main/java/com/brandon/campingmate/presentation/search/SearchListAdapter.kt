package com.brandon.campingmate.presentation.search

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brandon.campingmate.domain.model.CampEntity
import com.brandon.campingmate.databinding.ItemBigCampBinding
import com.brandon.campingmate.presentation.campdetail.CampDetailActivity
import com.bumptech.glide.Glide

class SearchListAdapter(
) : ListAdapter<CampEntity, SearchListAdapter.SearchViewHolder>(
    object : DiffUtil.ItemCallback<CampEntity>(){
        override fun areItemsTheSame(
            oldItem: CampEntity,
            newItem: CampEntity
        ): Boolean = if(oldItem is CampEntity && newItem is CampEntity){
            oldItem.contentId == newItem.contentId
        } else{
            oldItem == newItem
        }
        override fun areContentsTheSame(
            oldItem: CampEntity,
            newItem: CampEntity
        ): Boolean = oldItem == newItem
    }
){
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
    ) : SearchViewHolder(binding.root){
        override fun onBind(item: CampEntity) = with(binding){
            if(item !is CampEntity) {
                return@with
            }
            Glide.with(binding.root).load(item.firstImageUrl).into(binding.ivBigItem)
            tvBigItemName.text = item.facltNm
            tvBigItemAddr.text = item.addr1
            tvBigItemLineIntro.text = item.lineIntro
            tvBigItemTag.text = item.lctCl.toString()
            ivBigItem.clipToOutline = true

            binding.root.setOnClickListener {
                val myData = CampEntity(
                    addr1 =item.addr1, contentId = item.contentId, facltNm = item.facltNm,
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