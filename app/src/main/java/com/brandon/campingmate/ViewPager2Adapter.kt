package com.brandon.campingmate

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPager2Adapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 5
    }

    override fun createFragment(position: Int): Fragment {
        return when (position){
            0 ->  HomeFragment()
            1 ->  BoardFragment()
            2 ->  ChatFragment()
            3 -> MapFragment()
            4 ->  ProfileFragment()
            else -> Fragment()
        }
    }
}