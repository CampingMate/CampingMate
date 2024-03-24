package com.brandon.campingmate.presentation.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.brandon.campingmate.R
import com.brandon.campingmate.databinding.FragmentHomeBinding
import com.brandon.campingmate.domain.model.HomeEntity
import com.brandon.campingmate.presentation.home.adapter.HomeAdapter
import com.brandon.campingmate.presentation.home.adapter.PetAdapter
import com.brandon.campingmate.presentation.home.adapter.ReviewAdapter
import com.brandon.campingmate.presentation.search.SearchActivity
import com.brandon.campingmate.presentation.splash.SplashViewModel
import com.google.android.material.chip.ChipGroup
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding ?: throw IllegalStateException("Attempt to access binding when not set.")
    private val homeViewModel by lazy { ViewModelProvider(this)[HomeViewModel::class.java] }
    private val splashViewModel: SplashViewModel by activityViewModels()

    private var districtItem = mutableListOf<HomeEntity>()
    private var themeItem = mutableListOf<HomeEntity>()
    private lateinit var districtAdapter: HomeAdapter
    private lateinit var themeAdapter: HomeAdapter
//    private val db = Firebase.firestore
//    private val allCity: Query = db.collection("camps")

    private var scrollView: ScrollView? = null
    private var scrollPosition: Int = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("Home", "#Cycle onAttach")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Home", "#Cycle onCreate")


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("Home", "#Cycle onCreateView")
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        scrollView = binding.svHome

        if(savedInstanceState != null){
            scrollPosition = savedInstanceState.getInt("SCROLL_POSITION")
            scrollView?.scrollTo(0,scrollPosition)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Home", "#Cycle onViewCreated")
//        Log.d("Home", "#csh onViewCreated city : $city")
//        holidayInfo()
//        viewModelGet("district")
//        viewModelGet("theme")
////        initView(allCity, "district")
////        initView(allCity, "theme")
//        initPetView()
//        initReviewItem()

//        Glide.with(binding.root)
//            .asGif()
//            .load(R.drawable.ic_brand_img_gif)
//            .into(binding.ivBramdImg)
        init()

        onLayoutClickListener(binding.loCategoryCar)
        onLayoutClickListener(binding.loCategoryCaravan)
        onLayoutClickListener(binding.loCategoryGeneral)
        onLayoutClickListener(binding.loCategoryGlamping)
        onLayoutClickListener(binding.loSearch)



        binding.locationLayout.setOnClickListener {
            val checkedChipId = binding.chipDistrictGroup.checkedChipId
            val checkedChipName = when(checkedChipId){
                R.id.chipCapital -> "수도권"
                R.id.chipChungcheong -> "충청도"
                R.id.chipGangwon -> "강원도"
                R.id.chipGyeongsang -> "경상도"
                R.id.chipJeolla -> "전라도"
                else -> "전체"
            }
            val intent = Intent(requireContext(), LocationActivity::class.java).apply {
                putExtra("checkedChipName", checkedChipName)
            }
            requireContext().startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("Home", "#Cycle onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("Home", "#Cycle onResume")
        scrollView?.post {
            scrollView?.scrollTo(0,scrollPosition)
            Log.d("Home", "#scroll $scrollPosition")
        }
        initReviewItem()
    }

    override fun onPause() {
        super.onPause()
        Log.d("Home", "#Cycle onPause")
        scrollPosition = scrollView?.scrollY?:0
        Log.d("Home", "#scroll $scrollPosition")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("SCROLL_POSITION", scrollPosition)
    }

    private fun init(){
        Log.d("Home", "#csh init")
        holidayInfo()
        initPetView()
        var isReview = true
        var isDistrict = true
        var isTheme = true

        splashViewModel.isGet.asLiveData().observe(viewLifecycleOwner) { isDataLoaded ->
            Log.d("Home", "#csh init observe")
            val isCityLoaded = isDataLoaded["city"]?:false
            val isThemeLoaded = isDataLoaded["theme"]?:false

            if (isCityLoaded && isDistrict) {
                Log.d("Home", "#csh init district")
                viewModelGet("district")
                isDistrict=false
                if(isReview) {
                    initReviewItem()
                    isReview=false
                }
            }
            if (isThemeLoaded && isTheme) {
                Log.d("Home", "#csh init theme")
                viewModelGet("theme")
                isTheme = false
            }

        }
//        splashViewModel.loadData()
    }

    private fun initPetView(){
        //반려동물
        homeViewModel.loadPetItem()
        homeViewModel.petItem.observe(viewLifecycleOwner){petItemList ->
            if(isAdded){
                val context = requireContext()
                val petAdapter = PetAdapter(context, petItemList)
                binding.rvPetItem.adapter = petAdapter
                binding.rvPetItem.layoutManager =
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                binding.rvPetItem.itemAnimator = null
            }
        }
    }

    private fun initReviewItem(){
//        Log.d("Home", "#csh initReviewItem()")
        homeViewModel.loadReviewItem()
        homeViewModel.reviewItem.observe(viewLifecycleOwner){
//            Log.d("Home", "#csh it: $it")
            val reviewData = mutableListOf<HomeEntity>()
            if(!it.isNullOrEmpty()){
                reviewData.addAll(it)
                reviewData.sortByDescending { it.commentList.size }
//                Log.d("Home", "#csh reviewData: $reviewData")
//                Log.d("Home", "#csh reviewData size: ${reviewData.size}")
            }else{
//                Log.d("Home", "#csh reviewData empty: ${splashViewModel.allCityData.value}")
                reviewData.addAll(splashViewModel.allCityData.value!!)
                reviewData.shuffle()
//                Log.d("Home", "#csh reviewData empty: $reviewData")
            }

            val context = requireContext()
            val reviewAdapter = ReviewAdapter(context, reviewData)
            binding.rvReviewItem.adapter = reviewAdapter
            binding.rvReviewItem.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            binding.rvReviewItem.itemAnimator = null
        }
    }

    private fun viewModelGet(select:String){
        Log.d("Home","#csh viewModelGet()")

        if(select == "district") {
            districtAdapter = HomeAdapter(requireContext(), splashViewModel.allCityData.value)
            binding.rvDistrictItem.adapter = districtAdapter
            binding.rvDistrictItem.layoutManager =
                GridLayoutManager(context, 2, GridLayoutManager.HORIZONTAL, false)
            binding.rvDistrictItem.itemAnimator = null
        }

        else {
            themeAdapter = HomeAdapter(requireContext(), splashViewModel.allThemeData.value)
            binding.rvThemeItem.adapter = themeAdapter
            binding.rvThemeItem.layoutManager =
                GridLayoutManager(context, 2, GridLayoutManager.HORIZONTAL, false)
            binding.rvThemeItem.itemAnimator = null
        }
        selectChip()
    }

    private fun selectChip() {
        Log.d("Home", "selectChip()")
        binding.chipAllCity.isChecked=true
        binding.chipAllTeme.isChecked=true
        val chipGroup = ChipGroup.OnCheckedChangeListener { group, checkedId ->
            Log.d("Home", "#csh 1 isChecked ${binding.chipAllCity.isChecked}")
            when (checkedId) {
                R.id.chipCapital -> initDistrictView("Capital")
                R.id.chipChungcheong -> initDistrictView("Chungcheong")
                R.id.chipGangwon -> initDistrictView("Gangwon")
                R.id.chipGyeongsang -> initDistrictView("Gyeongsang")
                R.id.chipJeolla -> initDistrictView("Jeolla")
                R.id.chipSpringFlower -> initThemeView("SpringFlower")
                R.id.chipWalk -> initThemeView("Walk")
                R.id.chipActivity -> initThemeView("Activity")
                R.id.chipSwim -> initThemeView("Swim")
                R.id.chipSunset -> initThemeView("Sunset")
                R.id.chipFallFlower -> initThemeView("FallFlower")
                R.id.chipWinterFlower -> initThemeView("WinterFlower")
                else -> {
                    if(group == binding.chipDistrictGroup) {
                        Log.d("Home", "#csh 2 isChecked ${binding.chipAllCity.isChecked}")
//                        if(binding.chipAllCity.isChecked==true) {
//                            Log.d("Home", "#csh 3 isChecked ${binding.chipAllCity.isChecked}")
//                            binding.chipAllCity.isCheckable = false
//                            binding.chipAllCity.isChecked=false
//                        }else{
//                            binding.chipAllCity.isCheckable = true
//                        }
//                            binding.chipAllCity.isChecked=false
//                        }else{
//                            binding.chipAllCity.isCheckable = true
//                        }
                        binding.chipAllCity.isChecked=true
                        viewModelGet("district")
                    }
                    else {
                        viewModelGet("theme")
                        binding.chipAllTeme.isChecked=true
                    }
                }
            }
        }
        binding.chipDistrictGroup.setOnCheckedChangeListener(chipGroup)
        binding.chipThemeGroup.setOnCheckedChangeListener(chipGroup)

    }

    private fun initDistrictView(data: String) {
        Log.d("Home", "1. data=$data")
        homeViewModel.loadDistrictItem(data)
        homeViewModel.districtItem.observe(viewLifecycleOwner){districtItemList ->
            if(isAdded ){
                Log.d("Home", "#csh initDistrictView isAdded")
                val context = requireContext()
                districtAdapter = HomeAdapter(context, districtItemList)
                binding.rvDistrictItem.adapter = districtAdapter
                binding.rvDistrictItem.layoutManager =
                    GridLayoutManager(context, 2, GridLayoutManager.HORIZONTAL, false)
                binding.rvDistrictItem.itemAnimator = null
            }
        }
//        districtItem.clear()
//        val result = when(data){
//            "Capital" -> allCity.whereIn("doNm", listOf("서울시", "경기도", "인천시")).limit(10)
//            "Chungcheong" ->allCity.whereIn("doNm", listOf("충청남도", "충청북도", "세종시", "대전시")).limit(10)
//            "Gyeongsang" -> allCity.whereIn("doNm", listOf("경상북도", "경상남도", "부산시", "울산시", "대구시")).limit(10)
//            "Jeolla" -> allCity.whereIn("doNm", listOf("전라북도", "전라남도", "광주시", "제주도")).limit(10)
//            "Gangwon" -> allCity.whereIn("doNm", listOf("강원도")).limit(10)
//            else -> {
//                Toast.makeText(requireContext(), "지역칩 오류", Toast.LENGTH_SHORT).show()
//                throw IllegalArgumentException("Invalid data value: $data")
//            }
//        }
//        Log.d("Home", "2. data=$data")
//        result.get().addOnSuccessListener { documents ->
//            for (document in documents) {
//                val dataList = document.toObject(HomeEntity::class.java)
//                districtItem.add(dataList)
////                Log.d("Home","item : $dataItem")
//            }
//            Log.d("Home", "districtItem:$districtItem")
//            if(isAdded){
//                val context = requireContext()
//                districtAdapter = HomeAdapter(context, districtItem)
//                binding.rvDistrictItem.adapter = districtAdapter
//                binding.rvDistrictItem.layoutManager =
//                    GridLayoutManager(context, 2, GridLayoutManager.HORIZONTAL, false)
//                binding.rvDistrictItem.itemAnimator = null
//            }
//
//        }.addOnFailureListener { exception ->
//            Log.d("Home", "districtItem fail")
//        }
    }

    private fun initThemeView(data: String) {
        Log.d("Home", "1. data=$data")
        val db = Firebase.firestore
        val allCity: Query = db.collection("camps")
        themeItem.clear()
        val result = when(data){
            "Swim" -> allCity.whereArrayContains("themaEnvrnCl", "여름물놀이").limit(10)
            "Walk" -> allCity.whereArrayContains("themaEnvrnCl", "걷기길").limit(10)
            "Activity" -> allCity.whereArrayContains("themaEnvrnCl", "액티비티").limit(10)
            "SpringFlower" -> allCity.whereArrayContains("themaEnvrnCl", "봄꽃여행").limit(10)
            "WinterFlower" -> allCity.whereArrayContains("themaEnvrnCl", "겨울눈꽃명소").limit(10)
            "FallFlower" -> allCity.whereArrayContains("themaEnvrnCl", "가을단풍명소").limit(10)
            "Sunset" -> allCity.whereArrayContains("themaEnvrnCl", "일몰명소").limit(10)
            else -> throw IllegalArgumentException("Invalid data value: $data")
        }
//        Log.d("Home", "2. data=$data")
        result.get().addOnSuccessListener { documents ->
            for (document in documents) {
                val dataList = document.toObject(HomeEntity::class.java)
                themeItem.add(dataList)
//                Log.d("Home","item : $dataItem")
            }
            Log.d("Home", "themeItem:$themeItem")
            if(isAdded){
                val context = requireContext()
                themeAdapter = HomeAdapter(context, themeItem)
                binding.rvThemeItem.adapter = themeAdapter
                binding.rvThemeItem.layoutManager =
                    GridLayoutManager(context, 2, GridLayoutManager.HORIZONTAL, false)
                binding.rvThemeItem.itemAnimator = null
            }

        }.addOnFailureListener { exception ->
            Log.d("Home", "themeItem fail")
        }
    }

    private fun onLayoutClickListener(layout: LinearLayout){
        layout.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java).apply{
                var temp:String=""
                when(layout){
                    binding.loCategoryCar -> temp="자동차야영장"
                    binding.loCategoryCaravan -> temp="카라반"
                    binding.loCategoryGeneral ->temp="일반야영장"
                    binding.loCategoryGlamping ->temp="글램핑"
                    binding.loSearch ->temp="검색바"
                }
                putExtra("searchData", temp)
            }
            requireContext().startActivity(intent)

        }
    }

    private fun holidayInfo(){
        Log.d("Home", "#csh holidayInfo start")
        homeViewModel.loadHolidayData()
        homeViewModel.holidayItems.observe(viewLifecycleOwner){ holidayList ->
            if(!holidayList.isNullOrEmpty()){
                val nextHoliday = holidayList.first()
                binding.tvHolidayName.text = "다음 휴일인 ${nextHoliday?.dateName}까지 "
                binding.tvDday.text = "${nextHoliday?.dDay}일"
            }
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}