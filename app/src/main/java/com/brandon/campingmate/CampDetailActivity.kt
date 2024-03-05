package com.brandon.campingmate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.brandon.campingmate.databinding.ActivityCampDetailBinding
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class CampDetailActivity : AppCompatActivity() {

    private val binding by lazy { ActivityCampDetailBinding.inflate(layoutInflater) }
    private val viewModel by lazy {
        ViewModelProvider(this)[CampDetailViewModel::class.java]
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
        initViewModel()
    }

    private fun initViewModel() =with(viewModel) {
        imageParam.observe(this@CampDetailActivity){
            viewModel.communicateNetWork(it)
        }
        imageResult.observe(this@CampDetailActivity){
            if(it.isNotEmpty()){
                val firstImageUrl = it[0]
                Glide.with(applicationContext).load(firstImageUrl).into(binding.ivDetail)
            }
        }
    }

    private fun initView() =with(binding) {
        val myData = intent.getParcelableExtra("campData") as? CampModel
        if(myData != null){
            val contentId = myData.contentId.toString()
            tvCampName.text = myData.facltNm
            tvAddr.text = myData.addr1
            tvCall.text = myData.tel
            tvHomepage.text = "홈페이지 - ${myData.homepage}"
            tvSize.text = "면적 - ${myData.allar}㎡"
            tvRestTime.text = "휴장기간 ${myData.hvofBgnde} ~ ${myData.hvofEnddle} "
            tvPlayTime.text = "운영기간 - ${myData.operPdCl}"
            var bottom = ""
            if(myData.siteBottomCl1 != "0") bottom += "잔디, "
            if(myData.siteBottomCl2 != "0") bottom += "파쇄석, "
            if(myData.siteBottomCl3 != "0") bottom += "테크, "
            if(myData.siteBottomCl4 != "0") bottom += "자갈, "
            if(myData.siteBottomCl5 != "0") bottom += "맨흙, "
            tvBottom.text = "바닥재질 - ${bottom}"
            tvIntroduceComment.text = myData.intro
            tvConvenienceComment.text = "편의시설 - 화장실: ${myData.toiletCo} 샤워실: ${myData.swrmCo} 개수대: ${myData.wtrplCo} 화로대-${myData.brazierCl}"
            tvConvenienceComment2.text = "부대시설 - ${myData.sbrsCl}"
            tvConvenienceThema.text = "테마 - ${myData.themaEnvrnCl}"
            tvConvenienceNear.text = "주변이용가능시설 - ${myData.posblFcltyCl}"
            tvConvenienceFeature.text = "특징 - ${myData.featureNm}"
            viewModel.setUpParkParameter(contentId)
        }
        ivArrowBack.setOnClickListener {
            finish()
        }
        scrollTab()
    }

    private fun scrollTab() =with(binding){
        tvInformationTab.setOnClickListener {
            scrollToView(tvInformation)
        }
        tvIntroduceTab.setOnClickListener {
            scrollToView(tvIntroduce)
        }
        tvConvenienceTab.setOnClickListener {
            scrollToView(tvConvenience)
        }
        tvMapTab.setOnClickListener {
            scrollToView(tvMap)
        }
        tvCommentTab.setOnClickListener {
            scrollToView(tvComment)
        }
    }
    private fun scrollToView(view: View) {
        binding.scrollView.post {
            binding.scrollView.smoothScrollTo(0, view.top)
        }
    }
}