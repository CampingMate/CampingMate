package com.brandon.campingmate.presentation.home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.brandon.campingmate.presentation.search.SearchFragment
import com.brandon.campingmate.databinding.FragmentHomeBinding
import com.brandon.campingmate.presentation.home.adapter.HomeAdapter
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    var dataItem=mutableListOf<HomeDistrictThemeModel?>()
    private lateinit var homeAdapter: HomeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = Firebase.firestore
        val allCitys = db.collection("camps")
        val dataCapital = allCitys.whereIn("doNm", listOf("서울시", "경기도", "인천시"))
        val dataChungcheong = allCitys.whereIn("doNm", listOf("충청남도", "충청북도", "세종시", "대전시"))
        val dataGyeongsang = allCitys.whereIn("doNm", listOf("경상북도", "경상남도", "부산시", "울산시", "대구시"))
        val dataJeolla = allCitys.whereIn("doNm", listOf("전라북도", "전라남도", "광주시", "제주도"))
        val dataGangwon = allCitys.whereIn("doNm", listOf("강원도"))

        homeAdapter = HomeAdapter(requireContext(), dataItem)
        binding.rvDistrictItem.adapter = homeAdapter
        binding.rvDistrictItem.layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
        binding.rvDistrictItem.itemAnimator = null

        binding.rvThemeItem.adapter = homeAdapter
        binding.rvDistrictItem.layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
        binding.rvDistrictItem.itemAnimator = null

        homeAdapter.itemClick = object : HomeAdapter.ItemClick {
            override fun onClick(view: View, position: Int) {
//                val intent = Intent(requireContext(), ::class.java)
//                startActivity(intent)
//                supportFragmentManager.commit {
//                    replace(R.id.frameLayout, frag)
//                    setReorderingAllowed(true)
//                    addToBackStack("")
//                }
            }
        }


        binding.loSearch.setOnClickListener {
//            val intent = Intent(requireContext(), SearchFragment::class.java)
//            startActivity(intent)
        }
        binding.loCategoryCar.setOnClickListener {
//            val intent = Intent(requireContext(), SearchFragment::class.java)
//            startActivity(intent)
        }
        binding.loCategoryCaravan.setOnClickListener {
//            val intent = Intent(requireContext(), SearchFragment::class.java)
//            startActivity(intent)
        }
        binding.loCategoryGeneral.setOnClickListener {
//            val intent = Intent(requireContext(), SearchFragment::class.java)
//            startActivity(intent)
        }
        binding.loCategoryGlamping.setOnClickListener {
//            val intent = Intent(requireContext(), SearchFragment::class.java)
//            startActivity(intent)
        }
    }

    companion object {

    }
}