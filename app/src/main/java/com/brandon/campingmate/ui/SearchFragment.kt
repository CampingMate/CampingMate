package com.brandon.campingmate.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.brandon.campingmate.R
import com.brandon.campingmate.SearchListAdapter
import com.brandon.campingmate.SearchViewModel
import com.brandon.campingmate.data.CampModel
import com.brandon.campingmate.databinding.FragmentSearchBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val listAdapter: SearchListAdapter by lazy { SearchListAdapter() }

    private val viewModel by lazy {
        ViewModelProvider(this)[SearchViewModel::class.java]
    }

    lateinit var behavior: BottomSheetBehavior<ConstraintLayout>
    val firebaseDatabase = FirebaseDatabase.getInstance()
    val db = Firebase.firestore

    private val activatedChips = mutableListOf<String>()
    private val indutyList = mutableListOf<String>()
    private val doNmList = mutableListOf<String>()
    private val themaEnvrnClList = mutableListOf<String>()

    companion object{
        var campList = mutableListOf<CampModel>()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        initView()
        initViewModel()
        return binding.root
    }

    private fun initViewModel() =with(viewModel){
//        resetBtn.observe(viewLifecycleOwner){

        }

    private fun initView() =with(binding){
        bottomSheet()
        recyclerView.adapter = listAdapter
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        val chipIds = arrayOf(
            R.id.chipAll,
            R.id.chipCaravan,
            R.id.chipCar,
            R.id.chipBase,
            R.id.chipGlamping,
            R.id.chipSeoul,
            R.id.chipBusan,
            R.id.chipDaegu,
            R.id.chipIncheon,
            R.id.chipGwangju,
            R.id.chipDaejeon,
            R.id.chipUlsan,
            R.id.chipSejong,
            R.id.chipGyeonggi,
            R.id.chipGangwon,
            R.id.chipChungbuk,
            R.id.chipChungnam,
            R.id.chipJeonbuk,
            R.id.chipJeonnam,
            R.id.chipGyeongbuk,
            R.id.chipGyeongnam,
            R.id.chipJeju,
            R.id.chipBathroom,
            R.id.chipShower,
            R.id.chipBrazier,
            R.id.chipElectronic,
            R.id.chipRefrigerator,
            R.id.chipFireSee,
            R.id.chipAircon,
            R.id.chipBed,
            R.id.chipTv,
            R.id.chipWarmer,
            R.id.chipInnerBathroom,
            R.id.chipInnerShower,
            R.id.chipInternet,
            R.id.chipSummerWater,
            R.id.chipFishing,
            R.id.chipAnimal,
            R.id.chipWalking,
            R.id.chipActivity,
            R.id.chipSpringFlower,
            R.id.chipFallLeaves,
            R.id.chipWinterSnow,
            R.id.chipSunset,
            R.id.chipWaterLeisure,
            R.id.chipGrass,
            R.id.chipCrushedStone,
            R.id.chipTech,
            R.id.chipGravel,
            R.id.chipSoil
        )
        btnReset.setOnClickListener {
            for(chipId in chipIds){
                val chip = root.findViewById<Chip>(chipId)
                chip.isChecked = false
            }
        }
        btnApply.setOnClickListener {
            //todo: 활성화된 칩 내용으로 필터링해서 검색
            activatedChips.clear()

            for(chipId in chipIds){
                val chip = root.findViewById<Chip>(chipId)
                if(chip.isChecked){
                    activatedChips.add(chip.text.toString())
                    if (chip.text.toString() in listOf("카라반", "차박", "일반야영", "글램핑")) {
                        val value = if(chip.text.toString() == "차박") {
                            "자동차야영지"
                        } else if(chip.text.toString() == "일반야영") {
                            "일반야영지"
                        } else{
                            chip.text.toString()
                        }
                        indutyList.add(value)
                    } else if(chip.text.toString() in listOf("여름 물놀이", "낚시", "걷기길", "봄꽃여행", "액티비티", "가을 단풍명소", "겨울 눈꽃명소", "일몰명소", "수상레저")){
                        val value = if(chip.text.toString() == "여름 물놀이") {
                            "여름물놀이"
                        } else if(chip.text.toString() == "가을 단풍명소") {
                            "가을단풍명소"
                        } else if(chip.text.toString() == "겨울 눈꽃명소") {
                            "겨울 눈꽃명소"
                        } else{
                            chip.text.toString()
                        }
                        themaEnvrnClList.add(value)
                    } else{
                        doNmList.add(chip.text.toString())
                    }
                }
            }
            callData()
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun callData() {
        var baseQuery: Query = db.collection("camps")
        // "카라반" 인덕션 필터
        val chipCaravanQuery = baseQuery.whereArrayContains("induty", "카라반")
        val middleQuery = chipCaravanQuery.whereArrayContains("themaEnvrnCl", "여름물놀이")
        val finalQuery = chipCaravanQuery.whereEqualTo("doNm", "강원도")
        val checking = baseQuery.whereArrayContains("induty", "카라반").whereEqualTo("doNm", "강원도")
        Log.d("dasdf", "$indutyList")
        val asdf = baseQuery.whereIn("doNm", listOf("강원도", "경기도"))
        val sdfg = asdf.whereIn("doNm", listOf("충청북도", "충청남도"))
//        val indutyFilter = baseQuery.whereIn("induty", listOf("카라반", "글램핑"))
//        val themaFilter = indutyFilter.whereIn("themaEnvrnCl", themaEnvrnClList)
//        val doNmFilter = themaFilter.whereIn("doNm", doNmList)

        checking.limit(5)
            .get()
            .addOnSuccessListener { documents ->
                val campList = mutableListOf<CampModel>()
                for (document in documents) {
                    val camp = document.toObject(CampModel::class.java)
                    campList.add(camp)
                }
                listAdapter.submitList(campList)
            }
            .addOnFailureListener { exception ->
                // 오류 처리
                // 예: Log.w("TAG", "Error getting documents.", exception)
            }
    }
    private fun applyFilter(query: Query, chipName: String): Query {
        return when (chipName) {
            "카라반" -> query.whereArrayContains("induty", "카라반")
            "차박" -> query.whereArrayContains("induty", "자동차야영지")
            "일반야영" -> query.whereArrayContains("induty", "일반야영지")
            "글램핑" -> query.whereArrayContains("induty", "글램핑")
            "서울" -> query.whereArrayContains("doNm", "서울시")
            "부산" -> query.whereArrayContains("doNm", "부산시")
            "대구" -> query.whereArrayContains("doNm", "대구시")
            "인천" -> query.whereArrayContains("doNm", "인천시")
            "광주" -> query.whereArrayContains("doNm", "광주시")
            "대전" -> query.whereArrayContains("doNm", "대전시")
            "울산" -> query.whereArrayContains("doNm", "울산시")
            "세종" -> query.whereArrayContains("doNm", "세종시")
            "경기" -> query.whereArrayContains("doNm", "경기도")
            "강원" -> query.whereArrayContains("doNm", "강원도")
            "충북" -> query.whereArrayContains("doNm", "충청북도")
            "충남" -> query.whereArrayContains("doNm", "충청남도")
            "전북" -> query.whereArrayContains("doNm", "전라북도")
            "전남" -> query.whereArrayContains("doNm", "전라남도")
            "경북" -> query.whereArrayContains("doNm", "경상북도")
            "경남" -> query.whereArrayContains("doNm", "경상남도")
            "제주" -> query.whereArrayContains("doNm", "제주시")

            "여름 물놀이" -> query.whereArrayContains("themaEnvrnCl", "여름물놀이")
            "낚시" -> query.whereArrayContains("themaEnvrnCl", "낚시")
            "애견동반" -> query.whereArrayContains("animalCmgCl", "가능")
            "걷기길" -> query.whereArrayContains("themaEnvrnCl", "걷기길")
            "액티비티" -> query.whereArrayContains("themaEnvrnCl", "액티비티")
            "봄꽃여행" -> query.whereArrayContains("themaEnvrnCl", "봄꽃여행")
            "가을 단풍명소" -> query.whereArrayContains("themaEnvrnCl", "가을단풍명소")
            "겨울 눈꽃명소" -> query.whereArrayContains("themaEnvrnCl", "겨울눈꽃명소")
            "일몰명소" -> query.whereArrayContains("themaEnvrnCl", "일몰명소")
            "수상레저" -> query.whereArrayContains("themaEnvrnCl", "수상레저")
            else -> query
        }
    }

    private fun bottomSheet() {

        behavior = BottomSheetBehavior.from(binding.bottomSheet)
        behavior.isHideable = true //이게 없었다.
        behavior.state = BottomSheetBehavior.STATE_HIDDEN // 초기 상태 설정

        binding.ivSetting.setOnClickListener{
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
            override fun onStateChanged(bottomSheet: View, newState: Int) {
            }
        })
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}