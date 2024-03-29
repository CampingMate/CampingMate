package com.brandon.campingmate.presentation.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brandon.campingmate.R
import com.brandon.campingmate.databinding.ActivitySearchBinding
import com.brandon.campingmate.domain.model.CampEntity
import com.brandon.campingmate.presentation.search.adapter.SearchListAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore

class SearchActivity : AppCompatActivity() {
    private val binding by lazy {  ActivitySearchBinding.inflate(layoutInflater)  }
    private val listAdapter: SearchListAdapter by lazy { SearchListAdapter() }
    private val viewModel by lazy {
        ViewModelProvider(this)[SearchViewModel::class.java]
    }
    lateinit var behavior: BottomSheetBehavior<ConstraintLayout>
    private var temp:String? = ""
    companion object {
        var activatedChips = mutableListOf<String>()
        var doNmList = mutableListOf<String>()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
        initViewModel()
    }
    private fun initViewModel() = with(viewModel) {
        keyword.observe(this@SearchActivity){
            binding.loadingAnimation.visibility = View.GONE
            val myNewList = mutableListOf<CampEntity>()
            myNewList.addAll(it)
            listAdapter.submitList(myNewList)
            checkItem(myNewList)
        }
        myList.observe(this@SearchActivity){
            binding.loadingAnimation.visibility = View.GONE
            val myNewList = mutableListOf<CampEntity>()
            myNewList.addAll(it)
            listAdapter.submitList(myNewList)
            checkItem(myNewList)
        }
        toastMessage.observe(this@SearchActivity){
            Toast.makeText(this@SearchActivity, it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkItem(myNewList: MutableList<CampEntity>) = with(binding) {
        if(myNewList.isEmpty()){
            recyclerView.visibility = View.INVISIBLE
            tvNoResult.visibility = View.VISIBLE
        } else{
            recyclerView.visibility = View.VISIBLE
            tvNoResult.visibility = View.INVISIBLE
        }
    }

    private fun initView() = with(binding) {
        temp = intent.getStringExtra("searchData")
        setting(temp!!)
        ivArrowBack.setOnClickListener {
            finish()
        }
        //리사이클러뷰 연결
        recyclerView.adapter = listAdapter
        recyclerView.layoutManager =
            LinearLayoutManager(this@SearchActivity, LinearLayoutManager.VERTICAL, false)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    // 목록의 끝에 도달했을 때, 더 많은 데이터 로드
                    if(viewModel.isFilter){
                        if(!viewModel.isLoadingData){
                            viewModel.loadMoreData()
                        }
                    }
                    if(viewModel.isKeyword){
                        if(!viewModel.isLoadingDataKeyword){
                            viewModel.loadMoreDataKeyword()
                        }
                    }
                }
            }
        })

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
            R.id.chipBrazier,
            R.id.chipElectronic,
            R.id.chipRefrigerator,
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
//            R.id.chipGrass,
//            R.id.chipCrushedStone,
//            R.id.chipTech,
//            R.id.chipGravel,
//            R.id.chipSoil
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
            loadingAnimation.visibility = View.VISIBLE
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
            viewModel.callData()
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
            val drawable = ContextCompat.getDrawable(this@SearchActivity, R.drawable.ic_filter_checked)
            ivSetting.setImageDrawable(drawable)
        }
        /**
         * 엔터누를시 검색
         */
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // 엔터 키가 눌렸을 때 호출되는 콜백
                loadingAnimation.visibility = View.VISIBLE
                val searchText = binding.searchView.query.toString()
                viewModel.setUpParameter(searchText)
                binding.root.hideKeyboard()
                searchView.clearFocus()
                val drawable = ContextCompat.getDrawable(this@SearchActivity, R.drawable.ic_filter)
                ivSetting.setImageDrawable(drawable)
                for (chipId in chipIds) {
                    val chip = root.findViewById<Chip>(chipId)
                    chip.isChecked = false
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 검색어가 변경될 때마다 호출되는 콜백
                // newText에 변경된 검색어가 전달됩니다.
                return true
            }
        })
    }

    private fun setting(temp: String) {
        bottomSheet() //바텀시트 연결
//        scrollTab() //바텀시트 스크롤탭
        Log.d("SearchActivity", "setting진입, ${temp}")
        when(temp){
            "검색바" -> {
                binding.searchView.requestFocus()
                val manager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                manager.showSoftInput(binding.searchView, InputMethodManager.SHOW_IMPLICIT)
                binding.searchView.post {
                    if (!manager.isActive(binding.searchView)) {
                        manager.showSoftInput(binding.searchView, InputMethodManager.SHOW_IMPLICIT)
                    }
                }
            }
            "글램핑", "일반야영장", "자동차야영장", "카라반" -> {
                when(temp){
                    "글램핑" -> binding.chipGlamping.isChecked = true
                    "일반야영장" -> binding.chipBase.isChecked = true
                    "자동차야영장" -> binding.chipCar.isChecked = true
                    "카라반" -> binding.chipCaravan.isChecked = true
                }
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
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
     * 바텀시트 연결
     */
    private fun bottomSheet() {

        behavior = BottomSheetBehavior.from(binding.bottomSheet)
        behavior.isHideable = true //초기 숨김상태
        behavior.state = BottomSheetBehavior.STATE_HIDDEN // 초기 상태 설정

        binding.ivSetting.setOnClickListener {
            binding.root.hideKeyboard()
            binding.searchView.clearFocus()
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
            }
        })
        binding.ivBtnClose.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

}