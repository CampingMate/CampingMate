package com.brandon.campingmate

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.brandon.campingmate.databinding.FragmentSearchBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
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
    val db = Firebase.firestore

    private val activatedChips = mutableListOf<String>()
    private val doNmList = mutableListOf<String>()

    companion object {
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

    private fun initViewModel() = with(viewModel) {
        keywordParam.observe(viewLifecycleOwner) {
            communicateNetWork(it)
        }
        keyword.observe(viewLifecycleOwner){
//            listAdapter.submitList(it)
        }
    }

    private fun initView() = with(binding) {
        bottomSheet()
        scrollTab()
        recyclerView.adapter = listAdapter
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

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
            for (chipId in chipIds) {
                val chip = root.findViewById<Chip>(chipId)
                chip.isChecked = false
            }
        }
        btnApply.setOnClickListener {
            doNmList.clear()
            activatedChips.clear()

            for (chipId in chipIds) {
                val chip = root.findViewById<Chip>(chipId)
                if (chip.isChecked) {
                    activatedChips.add(chip.text.toString())
                    if (chip.text.toString() in listOf(
                            "서울",
                            "부산",
                            "대구",
                            "인천",
                            "광주",
                            "대전",
                            "울산",
                            "세종",
                            "경기",
                            "강원",
                            "충북",
                            "충남",
                            "전북",
                            "전남",
                            "경북",
                            "경남",
                            "제주"
                        )
                    ) {
                        val value = when (chip.text.toString()) {
                            "서울" -> "서울시"
                            "부산" -> "부산시"
                            "대구" -> "대구시"
                            "인천" -> "인천시"
                            "광주" -> "광주시"
                            "대전" -> "대전시"
                            "울산" -> "울산시"
                            "세종" -> "세종시"
                            "경기" -> "경기도"
                            "강원" -> "강원도"
                            "충북" -> "충청북도"
                            "충남" -> "충청남도"
                            "전북" -> "전라북도"
                            "전남" -> "전라남도"
                            "경북" -> "경상북도"
                            "경남" -> "경상남도"
                            "제주" -> "제주시"
                            else -> ""
                        }
                        doNmList.add(value)
                    }
                }
            }
            callData()
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        binding.ivSearch.setOnClickListener {
            val searchText = binding.tvEdit.text.toString()
            viewModel.setUpParkParameter(searchText)
        }
    }

    private fun callData() {
        var baseQuery: Query = db.collection("camps")
        var result = if (doNmList.isNotEmpty()) {
            baseQuery.whereIn("doNm", doNmList)
        } else {
            baseQuery
        }

        for (chip in activatedChips) {
            when (chip) {
                "글램핑" -> result = result.whereIn("induty1", listOf("글램핑"))
                "일반야영" -> result = result.whereIn("induty2", listOf("일반야영장"))
                "차박" -> result = result.whereIn("induty3", listOf("자동차야영장"))
                "글램핑" -> result = result.whereIn("induty4", listOf("글램핑"))
                "화장실" -> result = result.whereIn("bathroom", listOf("화장실"))
                "샤워실" -> result = result.whereIn("shower", listOf("샤워실"))
                "화로대" -> result = result.whereIn("fire", listOf("화로대"))
                "전기" -> result = result.whereIn("electronic", listOf("전기"))
                "냉장고" -> result = result.whereIn("refrigerator", listOf("냉장고"))
                "불멍" -> result = result.whereIn("firesee", listOf("불멍"))
                "에어컨" -> result = result.whereIn("aircon", listOf("에어컨"))
                "침대" -> result = result.whereIn("bed", listOf("침대"))
                "TV" -> result = result.whereIn("tv", listOf("TV"))
                "난방기구" -> result = result.whereIn("warmer", listOf("난방기구"))
                "내부화장실" -> result = result.whereIn("innerBathroom", listOf("내부화장실"))
                "내부샤워실" -> result = result.whereIn("innerShower", listOf("내부샤워실"))
                "유무선인터넷" -> result = result.whereIn("internet", listOf("유무선인터넷"))
                "애견동반" -> result = result.whereIn("animalCmgCl", listOf("가능", "가능(소형견)"))
                "여름물놀이" -> result = result.whereIn("summerPlay", listOf("여름물놀이"))
                "낚시" -> result = result.whereIn("fishing", listOf("낚시"))
                "걷기길" -> result = result.whereIn("walking", listOf("걷기길"))
                "액티비티" -> result = result.whereIn("activity", listOf("액티비티"))
                "봄꽃여행" -> result = result.whereIn("springFlower", listOf("봄꽃여행"))
                "가을단풍명소" -> result = result.whereIn("fallLeaves", listOf("가을단풍명소"))
                "겨울눈꽃명소" -> result = result.whereIn("winterSnow", listOf("겨울눈꽃명소"))
                "일몰명소" -> result = result.whereIn("sunset", listOf("일몰명소"))
                "수상레저" -> result = result.whereIn("waterLeisure", listOf("수상레저"))
                "잔디" -> result = result.whereIn("siteBottomCl1", listOf("잔디"))
                "파쇄석" -> result = result.whereIn("siteBottomCl2", listOf("파쇄석"))
                "테크" -> result = result.whereIn("siteBottomCl3", listOf("테크"))
                "자갈" -> result = result.whereIn("siteBottomCl4", listOf("자갈"))
                "맨흙" -> result = result.whereIn("siteBottomCl5", listOf("맨흙"))
                else -> Unit
            }
        }

        result.limit(1)
            .get()
            .addOnSuccessListener { documents ->
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
    private fun scrollTab() =with(binding){
        searchType.setOnClickListener {
            scrollToView(tvSearchType)
        }
        searchConvenience.setOnClickListener {
            scrollToView(tvSearchConvenience)
        }
        searchThema.setOnClickListener {
            scrollToView(tvSearchThema)
        }
        searchBottom.setOnClickListener {
            scrollToView(tvSearchBottom)
        }
    }

    private fun scrollToView(view: View) {
        binding.scrollView.post {
            binding.scrollView.smoothScrollTo(0, view.top)
        }
    }

    private fun bottomSheet() {

        behavior = BottomSheetBehavior.from(binding.bottomSheet)
        behavior.isHideable = true //이게 없었다.
        behavior.state = BottomSheetBehavior.STATE_HIDDEN // 초기 상태 설정

        binding.ivSetting.setOnClickListener {
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
