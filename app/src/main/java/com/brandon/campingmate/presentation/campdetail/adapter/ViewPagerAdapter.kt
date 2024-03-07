package com.brandon.campingmate.presentation.campdetail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.brandon.campingmate.R
import com.bumptech.glide.Glide

class ViewPagerAdapter(imageList: MutableList<String>) : RecyclerView.Adapter<ViewPagerAdapter.PagerViewHolder>() {
    var item = imageList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PagerViewHolder((parent))

    override fun getItemCount(): Int = item.size

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
//        holder.image.setImageResource(item[position])
        Glide.with(holder.itemView.context)
            .load(item[position])
            .centerCrop()
            .into(holder.image)
    }

    inner class PagerViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder
        (LayoutInflater.from(parent.context).inflate(R.layout.item_camp_image, parent, false)){

        val image = itemView.findViewById<ImageView>(R.id.imageCamp)
    }
}