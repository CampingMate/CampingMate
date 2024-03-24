package com.brandon.campingmate.presentation.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brandon.campingmate.databinding.ActivityLocationBinding
import com.brandon.campingmate.domain.model.CampEntity
import com.brandon.campingmate.presentation.home.adapter.MoreLocationListAdapter

class LocationActivity : AppCompatActivity() {
    private val binding by lazy { ActivityLocationBinding.inflate(layoutInflater) }
    private val listAdapter: MoreLocationListAdapter by lazy { MoreLocationListAdapter() }
    private val viewList = mutableListOf<View>()
    private val viewModel by lazy {
        ViewModelProvider(this)[LocationViewModel::class.java]
    }
    private var choiceLocation: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
        initViewModel()
    }

    private fun initViewModel() = with(viewModel) {
        myList.observe(this@LocationActivity) {
            binding.loadingAnimation.visibility = View.GONE
            val myNewList = mutableListOf<CampEntity>()
            myNewList.addAll(it)
            listAdapter.submitList(myNewList)
        }
    }

    private fun initView() = with(binding) {
        val selectedChipName = intent.getStringExtra("checkedChipName")
        choiceLocation = selectedChipName!!
        locationRecycler.adapter = listAdapter
        locationRecycler.layoutManager =
            LinearLayoutManager(this@LocationActivity, LinearLayoutManager.VERTICAL, false)
        locationRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    // 목록의 끝에 도달했을 때, 더 많은 데이터 로드
                    if (!viewModel.isLoading) {
                        viewModel.loadMoreData(choiceLocation)
                    }
                }
            }
        })
        loadingAnimation.visibility = View.VISIBLE
        viewModel.callData(choiceLocation)
        ivArrowBack.setOnClickListener { finish() }
        locationText(choiceLocation)
    }

    private fun locationText(selectedChipName: String) =with(binding){
        viewList.add(allView)
        viewList.add(sudoView)
        viewList.add(chungcheongView)
        viewList.add(jeollaView)
        viewList.add(gyeongsangView)
        viewList.add(gangwonView)
        viewList.forEach { it.visibility = View.INVISIBLE }
        when(selectedChipName){
            "수도권" -> sudoView.visibility = View.VISIBLE
            "충청도" -> chungcheongView.visibility = View.VISIBLE
            "강원도" -> gangwonView.visibility = View.VISIBLE
            "경상도" -> gyeongsangView.visibility = View.VISIBLE
            "전라도" -> jeollaView.visibility = View.VISIBLE
            else -> allView.visibility = View.VISIBLE
        }
        tvAll.setOnClickListener {
            showLocation(allView)
            choiceLocation = "전체"
            viewModel.callData(choiceLocation)
        }
        tvSudo.setOnClickListener {
            showLocation(sudoView)
            choiceLocation = "수도권"
            viewModel.callData(choiceLocation)
        }
        tvGangwon.setOnClickListener {
            showLocation(gangwonView)
            choiceLocation = "강원도"
            viewModel.callData(choiceLocation)
        }
        tvChungcheong.setOnClickListener {
            showLocation(chungcheongView)
            choiceLocation = "충청도"
            viewModel.callData(choiceLocation)
        }
        tvJeolla.setOnClickListener {
            showLocation(jeollaView)
            choiceLocation = "전라도"
            viewModel.callData(choiceLocation)
        }
        tvGyeongsang.setOnClickListener {
            showLocation(gyeongsangView)
            choiceLocation = "경상도"
            viewModel.callData(choiceLocation)
        }
    }

    private fun showLocation(view: View) {
        binding.loadingAnimation.visibility = View.VISIBLE
        val viewList = listOf(
            binding.allView,
            binding.sudoView,
            binding.chungcheongView,
            binding.jeollaView,
            binding.gyeongsangView,
            binding.gangwonView
        )
        viewList.forEach { it.visibility = View.INVISIBLE }
        view.visibility = View.VISIBLE
    }
}