package com.brandon.campingmate.presentation.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.brandon.campingmate.R
import com.brandon.campingmate.databinding.FragmentHomeBinding
import com.brandon.campingmate.domain.model.CampEntity
import com.brandon.campingmate.presentation.campdetail.CampDetailActivity
import com.brandon.campingmate.presentation.home.adapter.HomeAdapter
import com.brandon.campingmate.presentation.home.adapter.PetAdapter
import com.bumptech.glide.Glide
import com.google.android.material.chip.ChipGroup
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var districtItem = mutableListOf<CampEntity?>()
    private var petItem = mutableListOf<CampEntity?>()
    private var themeItem = mutableListOf<CampEntity?>()
    private var dataItem = mutableListOf<CampEntity?>()
    private lateinit var districtAdapter: HomeAdapter
    private lateinit var themeAdapter: HomeAdapter
    private val db = Firebase.firestore
    private val allCity: Query = db.collection("camps")
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

        init()
        initView(allCity)

        //지역별
        dataCapital = allCity.whereIn("doNm", listOf("서울시", "경기도", "인천시")).limit(10)
        dataChungcheong = allCity.whereIn("doNm", listOf("충청남도", "충청북도", "세종시", "대전시")).limit(10)
        dataGyeongsang =
            allCity.whereIn("doNm", listOf("경상북도", "경상남도", "부산시", "울산시", "대구시")).limit(10)
        dataJeolla = allCity.whereIn("doNm", listOf("전라북도", "전라남도", "광주시", "제주도")).limit(10)
        dataGangwon = allCity.whereIn("doNm", listOf("강원도")).limit(10)

        //테마
        dataSwim = allCity.whereIn("themaEnvrnCl", listOf("여름물놀이")).limit(10)
        dataWalk = allCity.whereIn("themaEnvrnCl", listOf("걷기길")).limit(10)
        dataActivity = allCity.whereIn("themaEnvrnCl", listOf("액티비티")).limit(10)
        dataSpringFlower = allCity.whereIn("themaEnvrnCl", listOf("봄꽃여행")).limit(10)
        dataWinterFlower = allCity.whereIn("themaEnvrnCl", listOf("겨울눈꽃명소")).limit(10)
        dataFallFlower = allCity.whereIn("themaEnvrnCl", listOf("가을단풍명소")).limit(10)
        dataSunset = allCity.whereIn("themaEnvrnCl", listOf("일몰명소")).limit(10)


        //반려동물
        val dataPet = allCity.whereIn("animalCmgCl", listOf("가능", "가능(소형견)")).limit(10)
        dataPet.get().addOnSuccessListener { documents ->
            for (document in documents) {
                val dataList = document.toObject(CampEntity::class.java)
                petItem.add(dataList)
//                Log.d("Home","item : $dataItem")
            }
            val petAdapter = PetAdapter(requireContext(), petItem)
            binding.rvPetItem.adapter = petAdapter
            binding.rvPetItem.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            binding.rvPetItem.itemAnimator = null
        }




        binding.cvSearch.setOnClickListener {
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

    private fun init() {
        //지역별
        dataCapital = allCity.whereIn("doNm", listOf("서울시", "경기도", "인천시")).limit(10)
        dataChungcheong = allCity.whereIn("doNm", listOf("충청남도", "충청북도", "세종시", "대전시")).limit(10)
        dataGyeongsang =
            allCity.whereIn("doNm", listOf("경상북도", "경상남도", "부산시", "울산시", "대구시")).limit(10)
        dataJeolla = allCity.whereIn("doNm", listOf("전라북도", "전라남도", "광주시", "제주도")).limit(10)
        dataGangwon = allCity.whereIn("doNm", listOf("강원도")).limit(10)

        //테마
        dataSwim = allCity.whereIn("themaEnvrnCl", listOf("여름물놀이")).limit(10)
        dataWalk = allCity.whereIn("themaEnvrnCl", listOf("걷기길")).limit(10)
        dataActivity = allCity.whereIn("themaEnvrnCl", listOf("액티비티")).limit(10)
        dataSpringFlower = allCity.whereIn("themaEnvrnCl", listOf("봄꽃여행")).limit(10)
        dataWinterFlower = allCity.whereIn("themaEnvrnCl", listOf("겨울눈꽃명소")).limit(10)
        dataFallFlower = allCity.whereIn("themaEnvrnCl", listOf("가을단풍명소")).limit(10)
        dataSunset = allCity.whereIn("themaEnvrnCl", listOf("일몰명소")).limit(10)

    }

    private fun initDistrictView(data: Query) {
        districtItem.clear()
        data.get().addOnSuccessListener { documents ->
            for (document in documents) {
                val dataList = document.toObject(CampEntity::class.java)
                districtItem.add(dataList)
//                Log.d("Home","item : $dataItem")
            }
            Log.d("Home", "districtItem:$districtItem")
            districtAdapter = HomeAdapter(requireContext(), districtItem)
            binding.rvDistrictItem.adapter = districtAdapter
            binding.rvDistrictItem.layoutManager =
                GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
            binding.rvDistrictItem.itemAnimator = null
        }.addOnFailureListener { exception ->
            Log.d("Home", "districtItem fail")
        }
    }

    private fun initThemeView(data: Query) {
        themeItem.clear()
        data.get().addOnSuccessListener { documents ->
            for (document in documents) {
                val dataList = document.toObject(CampEntity::class.java)
                themeItem.add(dataList)
//                Log.d("Home","item : $dataItem")
            }
            Log.d("Home", "themeItem:$themeItem")
            themeAdapter = HomeAdapter(requireContext(), themeItem)
            binding.rvThemeItem.adapter = themeAdapter
            binding.rvThemeItem.layoutManager =
                GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
            binding.rvThemeItem.itemAnimator = null
        }.addOnFailureListener { exception ->
            Log.d("Home", "districtItem fail")
        }
    }


    private fun initView(data: Query) {
        Log.d("Home", "initView()-data:$data")
        var limitData = data
        var view = binding.rvDistrictItem
        dataItem.clear()
        if (data == allCity)
            limitData = data.limit(10)

        limitData.get().addOnSuccessListener { documents ->
            for (document in documents) {
                val dataList = document.toObject(CampEntity::class.java)
                dataItem.add(dataList)
//                Log.d("Home","item : $dataItem")
            }
            view = when (data) {
                dataSwim, dataWalk, dataActivity, dataSpringFlower, dataWinterFlower, dataFallFlower, dataSunset -> binding.rvThemeItem
//                dataCapital, dataChungcheong, dataGyeongsang, dataJeolla, dataGangwon -> binding.rvDistrictItem
                else -> view
            }
            Log.d("Home", "view=$view")
            Log.d("Home", "dataItem=$dataItem")
            if (view == binding.rvDistrictItem) {
                districtAdapter = HomeAdapter(requireContext(), dataItem)
                view.adapter = districtAdapter
                view.layoutManager =
                    GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
                view.itemAnimator = null

            } else if (view == binding.rvThemeItem) {
                themeAdapter = HomeAdapter(requireContext(), dataItem)
                view.adapter = themeAdapter
                view.layoutManager =
                    GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
                view.itemAnimator = null

            }
            selectDistrict()
            selectTheme()
        }
    }

    private fun selectDistrict() {
        Log.d("Home", "selectChip()")
        val chipGroup = ChipGroup.OnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chipCapital -> initDistrictView(dataCapital)
                R.id.chipChungcheong -> initDistrictView(dataChungcheong)
                R.id.chipGangwon -> initDistrictView(dataGangwon)
                R.id.chipGyeongsang -> initDistrictView(dataGyeongsang)
                R.id.chipJeolla -> initDistrictView(dataJeolla)
                else -> Log.d("Home", "chip else")
            }
        }
        binding.chipDistrictGroup.setOnCheckedChangeListener(chipGroup)

    }
    private fun selectTheme(){
        val chipGroup = ChipGroup.OnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.chipSpringFlower -> initThemeView(dataSpringFlower)
                R.id.chipWalk -> initThemeView(dataWalk)
                R.id.chipActivity -> initThemeView(dataActivity)
                R.id.chipSwim -> initThemeView(dataSwim)
                R.id.chipSunset -> initThemeView(dataSunset)
                R.id.chipFallFlower -> initThemeView(dataFallFlower)
                R.id.chipWinterFlower -> initThemeView(dataWinterFlower)
                else -> Log.d("Home", "chip else")
            }
        }
        binding.chipThemeGroup.setOnCheckedChangeListener(chipGroup)
    }
    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}