package com.brandon.campingmate.presentation.campdetail

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.brandon.campingmate.R
import com.brandon.campingmate.databinding.ActivityCampDetailBinding
import com.brandon.campingmate.domain.model.CampEntity
import com.brandon.campingmate.presentation.campdetail.adapter.ViewPagerAdapter
import com.brandon.campingmate.presentation.common.SnackbarUtil
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.sdk.user.UserApiClient
import timber.log.Timber

class CampDetailActivity : AppCompatActivity() {

    private val binding by lazy { ActivityCampDetailBinding.inflate(layoutInflater) }
    private val viewModel by lazy {
        ViewModelProvider(this)[CampDetailViewModel::class.java]
    }
    private val imageUrls = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
        initViewModel()
        checkBookmarked()
        clickBookmarked()

    }

    private fun initViewPager() {
        binding.viewPager.adapter = ViewPagerAdapter(imageUrls)
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.springDotsIndicator.attachTo(binding.viewPager)
    }
    private fun getImageList(): ArrayList<Int> {
        return arrayListOf(R.drawable.ic_arrow_back, R.drawable.ic_arrow_forward, R.drawable.ic_arrow_upward)
    }

    private fun initViewPager() {
        binding.viewPager.adapter = ViewPagerAdapter(imageUrls)
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.springDotsIndicator.attachTo(binding.viewPager)
    }
    private fun getImageList(): ArrayList<Int> {
        return arrayListOf(R.drawable.ic_arrow_back, R.drawable.ic_arrow_forward, R.drawable.ic_arrow_upward)
    }

    private fun initViewModel() =with(viewModel) {
        imageResult.observe(this@CampDetailActivity){
            if(it.isNotEmpty()){
                imageUrls.addAll(it)
                initViewPager()
            }
        }
    }

    private fun initView() = with(binding) {
        myData = intent.getParcelableExtra("campData") as? CampEntity
        if (myData != null) {
            val contentId = myData?.contentId.toString()
            tvCampName.text = myData?.facltNm
            tvAddr.text = myData?.addr1
            tvCall.text = myData?.tel
            tvHomepage.text = "홈페이지 - ${myData?.homepage}"
            tvSize.text = "면적 - ${myData?.allar}㎡"
            tvRestTime.text = "휴장기간 ${myData?.hvofBgnde} ~ ${myData?.hvofEnddle} "
            tvPlayTime.text = "운영기간 - ${myData?.operPdCl}"
            var bottom = ""
            if (myData?.siteBottomCl1 != "0") bottom += "잔디, "
            if (myData?.siteBottomCl2 != "0") bottom += "파쇄석, "
            if (myData?.siteBottomCl3 != "0") bottom += "테크, "
            if (myData?.siteBottomCl4 != "0") bottom += "자갈, "
            if (myData?.siteBottomCl5 != "0") bottom += "맨흙, "
            tvBottom.text = "바닥재질 - ${bottom}"
            tvIntroduceComment.text = myData?.intro
            tvConvenienceComment.text =
                "편의시설 - 화장실: ${myData?.toiletCo} 샤워실: ${myData?.swrmCo} 개수대: ${myData?.wtrplCo} 화로대-${myData?.brazierCl}"
            tvConvenienceComment2.text = "부대시설 - ${myData?.sbrsCl}"
            tvConvenienceThema.text = "테마 - ${myData?.themaEnvrnCl}"
            tvConvenienceNear.text = "주변이용가능시설 - ${myData?.posblFcltyCl}"
            tvConvenienceFeature.text = "특징 - ${myData?.featureNm}"
            viewModel.setUpParkParameter(contentId)
        }
        ivArrowBack.setOnClickListener {
            finish()
        }
        ivCallCamping.setOnClickListener {
            val callNum = myData?.tel
            // 전화를 걸기 위한 Intent 생성
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$callNum"))
            startActivity(intent)
        }
        btnReserve.setOnClickListener{
            val reserveUrl = myData?.resveUrl
            Log.d("asdf", "$reserveUrl")
            if(!reserveUrl.isNullOrBlank()){
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(reserveUrl))
                startActivity(intent)
            } else{
                Toast.makeText(this@CampDetailActivity, "등록된 홈페이지가 없습니다.", Toast.LENGTH_SHORT).show()
            }
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

    private fun checkBookmarked() = with(binding) {
        UserApiClient.instance.me { user, error ->
            if (user?.id != null) {
                val userDocRef = db.collection("users").document("Kakao${user?.id}")
                userDocRef.get().addOnSuccessListener {
                    val bookmarkedList = it.get("bookmarked") as? List<*>
                    if (bookmarkedList != null && bookmarkedList.contains(myData?.contentId)) {
                        ivArrowBookmark.setImageResource(R.drawable.ic_bookmark_checked)
                    } else {
                        ivArrowBookmark.setImageResource(R.drawable.ic_bookmark)
                    }
                }
            } else {
                ivArrowBookmark.setImageResource(R.drawable.ic_bookmark)
            }
        }
    }
    private fun clickBookmarked() = with(binding) {
        ivArrowBookmark.setOnClickListener {
            UserApiClient.instance.me { user, error ->
                if (user?.id == null) {
                    SnackbarUtil.showSnackBar(it)
                } else {
                    val userDocRef = db.collection("users").document("Kakao${user?.id}")
                    userDocRef.get().addOnSuccessListener { document ->
                        val bookmarkedList = document.get("bookmarked") as? List<*>
                        Timber.tag("북마크리스트검사").d(bookmarkedList.toString())
                        if (bookmarkedList?.contains(myData?.contentId) == true) {
                            modifyBookmarked(userDocRef, bookmarkedList, deleteFromList = true)
                            Timber.tag("북마크삭제검사").d(bookmarkedList.toString())
                        } else {
                            modifyBookmarked(userDocRef, bookmarkedList, deleteFromList = false)
                            Timber.tag("북마크추가검사").d(bookmarkedList.toString())
                        }
                    }

                }
            }
        }
    }
    private fun modifyBookmarked(userDocRef: DocumentReference, bookmarkedList: List<*>?, deleteFromList: Boolean) = with(binding) {
        Timber.tag("모디파이검사").d(bookmarkedList.toString())
        val updatedList = bookmarkedList?.toMutableList()?: mutableListOf()
        if (deleteFromList) {
            updatedList.remove(myData?.contentId)
            userDocRef.update("bookmarked", updatedList)
                .addOnSuccessListener {
                    Toast.makeText(binding.root.context, "북마크가 해제되었습니다.", Toast.LENGTH_SHORT).show()
                    ivArrowBookmark.setImageResource(R.drawable.ic_bookmark)
                }
                .addOnFailureListener {
                    Toast.makeText(binding.root.context, "북마크 해제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
        } else {
            updatedList.add(myData?.contentId)
            userDocRef.update("bookmarked", updatedList)
                .addOnSuccessListener {
                    Toast.makeText(binding.root.context, "북마크가 추가되었습니다.", Toast.LENGTH_SHORT).show()
                    ivArrowBookmark.setImageResource(R.drawable.ic_bookmark_checked)
                }
                .addOnFailureListener {
                    Toast.makeText(binding.root.context, "북마크 추가에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}