package com.brandon.campingmate.presentation.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.brandon.campingmate.presentation.board.BoardFragment
import com.brandon.campingmate.presentation.home.HomeFragment
import com.brandon.campingmate.presentation.map.MapFragment
import com.brandon.campingmate.presentation.profile.ProfileFragment

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
            2 ->  MapFragment()
            3 -> ProfileFragment()
            else -> Fragment()
        }
    }
}