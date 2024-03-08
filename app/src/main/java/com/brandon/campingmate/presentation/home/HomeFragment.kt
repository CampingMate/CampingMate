package com.brandon.campingmate.presentation.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.brandon.campingmate.databinding.FragmentHomeBinding
import com.brandon.campingmate.domain.model.CampEntity
import com.brandon.campingmate.presentation.campdetail.CampDetailActivity
import com.brandon.campingmate.presentation.home.adapter.HomeAdapter
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Attempt to access binding when not set.")

    private var dataItem = mutableListOf<CampEntity?>()
    private lateinit var homeAdapter: HomeAdapter
    private val db = Firebase.firestore
    private val allCity = db.collection("camps")
    private lateinit var dataCapital: Query
    private lateinit var dataChungcheong: Query
    private lateinit var dataGyeongsang: Query
    private lateinit var dataJeolla: Query
    private lateinit var dataGangwon: Query
    private lateinit var dataSwim: Query
    private lateinit var dataWalk: Query
    private lateinit var dataActivity: Query
    private lateinit var dataSpringFlower: Query
    private lateinit var dataWinterFlower: Query
    private lateinit var dataFallFlower: Query
    private lateinit var dataSunset: Query
//    private lateinit var dataPet: Query


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(allCity)

        //지역별
        dataCapital = allCity.whereIn("doNm", listOf("서울시", "경기도", "인천시")).limit(10)
        dataChungcheong = allCity.whereIn("doNm", listOf("충청남도", "충청북도", "세종시", "대전시")).limit(30)
        dataGyeongsang = allCity.whereIn("doNm", listOf("경상북도", "경상남도", "부산시", "울산시", "대구시")).limit(30)
        dataJeolla = allCity.whereIn("doNm", listOf("전라북도", "전라남도", "광주시", "제주도")).limit(30)
        dataGangwon = allCity.whereIn("doNm", listOf("강원도")).limit(30)

        //테마
        dataSwim = allCity.whereIn("themaEnvrnCl", listOf("여름물놀이")).limit(30)
        dataWalk = allCity.whereIn("themaEnvrnCl", listOf("걷기길")).limit(30)
        dataActivity = allCity.whereIn("themaEnvrnCl", listOf("액티비티")).limit(30)
        dataSpringFlower = allCity.whereIn("themaEnvrnCl", listOf("봄꽃여행")).limit(30)
        dataWinterFlower = allCity.whereIn("themaEnvrnCl", listOf("겨울눈꽃명소")).limit(30)
        dataFallFlower = allCity.whereIn("themaEnvrnCl", listOf("가을단풍명소")).limit(30)
        dataSunset = allCity.whereIn("themaEnvrnCl", listOf("일몰명소")).limit(30)



        initView(dataSwim)

        //반려동물
        val dataPet = allCity.whereEqualTo("animalCmgCl", "").limit(30)

//        dataCapital.get().addOnSuccessListener { it ->
//            for(document in it.documents){
//                val dataList = document.toObject(CampEntity::class.java)
//                dataItem.add(dataList)
//                Log.d("Home","item : $dataItem")
//            }
//            homeAdapter = HomeAdapter(requireContext(), dataItem)
//            binding.rvDistrictItem.adapter = homeAdapter
//            binding.rvDistrictItem.layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
//            binding.rvDistrictItem.itemAnimator = null
//        }

//        dataItem=selectItem(dataCapital)

//        homeAdapter = HomeAdapter(requireContext(), dataItem)
//        binding.rvDistrictItem.adapter = homeAdapter
//        binding.rvDistrictItem.layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
//        binding.rvDistrictItem.itemAnimator = null

//        binding.rvThemeItem.adapter = homeAdapter
//        binding.rvDistrictItem.layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
//        binding.rvDistrictItem.itemAnimator = null

//        homeAdapter = HomeAdapter(requireContext(), dataItem)
//        Log.d("Home", "dataItem: $dataItem")
//        homeAdapter.itemClick = object : HomeAdapter.ItemClick {
//            override fun onClick(view: View, position: Int) {
//                val intent = Intent(requireContext(), CampDetailActivity::class.java)
//                startActivity(intent)
////                supportFragmentManager.commit {
////                    replace(R.id.frameLayout, frag)
////                    setReorderingAllowed(true)
////                    addToBackStack("")
////                }
//            }
//        }


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

    private fun initView(data: Query) {
        var limitData = data
        var view = binding.rvDistrictItem
        if (data == allCity)
            limitData = data.limit(10)

        limitData.get().addOnSuccessListener { it ->
            for (document in it.documents) {
                val dataList = document.toObject(CampEntity::class.java)
                dataItem.add(dataList)
//                Log.d("Home","item : $dataItem")
            }
            view = when (data) {
                dataSwim, dataWalk, dataActivity, dataSpringFlower, dataWinterFlower, dataFallFlower, dataSunset -> binding.rvThemeItem
                else -> view
            }
            homeAdapter = HomeAdapter(requireContext(), dataItem)
            view.adapter = homeAdapter
            view.layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
            view.itemAnimator = null

            homeAdapter.itemClick = object : HomeAdapter.ItemClick {
                override fun onClick(view: View, position: Int) {
                    val intent = Intent(requireContext(), CampDetailActivity::class.java)
                    intent.putExtra("campData", dataItem[position])
                    startActivity(intent)
                }
            }
        }
    }

    private fun selectChip(data: Query): MutableList<CampEntity?> {
        var item = mutableListOf<CampEntity?>()
        data.get().addOnSuccessListener { it ->
            for (document in it.documents) {
                val dataList = document.toObject(CampEntity::class.java)
                item.add(dataList)
                Log.d("Home", "item : $item")
            }
        }
        return item
    }

    companion object
}