package com.brandon.campingmate.presentation.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.brandon.campingmate.R
import com.brandon.campingmate.databinding.FragmentSearchBinding
import com.brandon.campingmate.domain.model.CampEntity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val listAdapter: SearchListAdapter by lazy { SearchListAdapter() }

    private val viewModel by lazy {
        ViewModelProvider(this)[SearchViewModel::class.java]
    }

    lateinit var behavior: BottomSheetBehavior<ConstraintLayout>


    companion object {
        var activatedChips = mutableListOf<String>()
        var doNmList = mutableListOf<String>()
        var campList = mutableListOf<CampEntity>()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        initView()
        initViewModel()
        return binding.root
    }

    private fun initViewModel() = with(viewModel) {
        keyword.observe(viewLifecycleOwner){
            listAdapter.submitList(it)
        }
        myList.observe(viewLifecycleOwner){
            listAdapter.submitList(it)
        }
    }

    private fun initView() = with(binding) {
        bottomSheet() //바텀시트 연결
        scrollTab() //바텀시트 스크롤탭
        //리사이클러뷰 연결
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
        /**
         * 초기화버튼 클릭시 칩 초기화
         */
        btnReset.setOnClickListener {
            for (chipId in chipIds) {
                val chip = root.findViewById<Chip>(chipId)
                chip.isChecked = false
            }
        }
        /**
         * 적용하기 버튼 클릭시 firestore필터링 후 검색
         */
        btnApply.setOnClickListener {
            doNmList.clear()
            activatedChips.clear()
//            viewModel.clearCampList()

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
            viewModel.callData()
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
        /**
         * 엔터누를시 검색
         */
        tvEdit.setOnKeyListener{_, KeyCode, event ->
            if((event.action==KeyEvent.ACTION_DOWN) && (KeyCode==KeyEvent.KEYCODE_ENTER)){
//                viewModel.clearCampList()
                val searchText = binding.tvEdit.text.toString()
                viewModel.setUpParkParameter(searchText)
                binding.root.hideKeyboard()
                return@setOnKeyListener true
            } else{
                return@setOnKeyListener false
            }
        }
        /**
         * 텍스트 변화감지
         */
        tvEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 텍스트 변경 전 동작
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 텍스트가 변경될 때 동작
                updateImage()
            }
            override fun afterTextChanged(s: Editable?) {
                // 텍스트 변경 후 동작
            }
        })
        /**
         * editText포커스될때 검색이미지 invisible
         */
        tvEdit.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if(hasFocus){
                ivSearch.visibility = View.INVISIBLE
            } else{
                ivSearch.visibility = View.VISIBLE
            }
        }
        ivDelete.setOnClickListener {
            tvEdit.text.clear()
        }
    }

    /**
     * x이미지 활성화/비활성화
     */
    fun updateImage(){
        if(binding.tvEdit.text.toString().isNotBlank()){
            binding.ivDelete.visibility = View.VISIBLE
        } else{
            binding.ivDelete.visibility = View.INVISIBLE
        }
    }

    /**
     * 키보드 숨김처리
     */
    private fun View.hideKeyboard(){
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    /**
     * 바텀시트 스크롤
     */
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

    /**
     * 바텀시트 연결
     */
    private fun bottomSheet() {

        behavior = BottomSheetBehavior.from(binding.bottomSheet)
        behavior.isHideable = true //초기 숨김상태
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
