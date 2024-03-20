package com.brandon.campingmate.presentation.campdetail

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.animation.AlphaAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.brandon.campingmate.R
import com.brandon.campingmate.databinding.ActivityCampDetailBinding
import com.brandon.campingmate.domain.model.CampCommentEntity
import com.brandon.campingmate.domain.model.CampEntity
import com.brandon.campingmate.presentation.campdetail.adapter.CommentListAdapter
import com.brandon.campingmate.presentation.campdetail.adapter.ViewPagerAdapter
import com.brandon.campingmate.presentation.common.SnackbarUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.sdk.user.UserApiClient
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.Marker
import org.checkerframework.common.subtyping.qual.Bottom
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CampDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private val binding by lazy { ActivityCampDetailBinding.inflate(layoutInflater) }
    private val viewModel by lazy {
        ViewModelProvider(this)[CampDetailViewModel::class.java]
    }
    private val listAdapter: CommentListAdapter by lazy { CommentListAdapter() }

    private val db = FirebaseFirestore.getInstance()
    private val imageUrls = mutableListOf<String>()
    private var mapView: MapView? = null
    private var naverMap: NaverMap? = null
    private var maptype: Int = 1
    private var myId: String? = null
    private var mapX: String? = null
    private var mapY: String? = null
    private var campName: String? = null
    private var myImage: String = ""
    var isTop = true
    lateinit var behavior: BottomSheetBehavior<ConstraintLayout>

    companion object {
        private const val REQUEST_CODE_IMAGE_PICK = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        Log.d("Detail", "onCreate")
        initView()
        initViewModel()
        initBottomSheet()
        checkBookmarked()
        clickBookmarked()
        mapView = binding.fcMap
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
    }

    private fun initViewPager() {
        binding.viewPager.adapter = ViewPagerAdapter(imageUrls)
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.springDotsIndicator.attachTo(binding.viewPager)
    }

    private fun initViewModel() = with(viewModel) {
        imageResult.observe(this@CampDetailActivity) {
            if (it.isNotEmpty()) {
                imageUrls.addAll(it)
                initViewPager()
            }
        }
        campEntity.observe(this@CampDetailActivity) {
            if (it != null) {
                initSetting(it)
                makeMarker()
            }
        }
        campComment.observe(this@CampDetailActivity) {
            if (it != null) {
                listAdapter.submitList(it)
            }
        }
        checkLastComment.observe(this@CampDetailActivity){
            if(it != null){
                binding.commentContent.text = it
            }
        }
        commentCount.observe(this@CampDetailActivity){
            binding.commentCount.text = it
        }
    }

    private fun initSetting(it: CampEntity) = with(binding) {
        mapX = it.mapX
        mapY = it.mapY
        campName = it.facltNm
        Log.d("Detail", "${mapX}, ${mapY}, ${campName}")
        tvCampName.text = it.facltNm
        tvAddr.text = it.addr1 ?: "등록된 주소가 없습니다."
        tvCall.text = it.tel ?: "등록된 번호가 없습니다."
        if (it.homepage.isNullOrBlank()) {
            tvHomepage.text = "등록된 홈페이지가 없습니다."
        } else {
            tvHomepage.text = "홈페이지 - ${it.homepage}"
        }
        if (it.allar.isNullOrBlank()) {
            tvSize.text = "등록된 면적 정보가 없습니다."
        } else {
            tvSize.text = "면적 - ${it.allar}㎡"
        }
        if (it.hvofBgnde.isNullOrBlank() && it.hvofEnddle.isNullOrBlank()) {
            tvRestTime.text = "등록된 휴장기간 정보가 없습니다."
        } else {
            tvRestTime.text = "휴장기간 ${it.hvofBgnde} ~ ${it.hvofEnddle} "
        }
        if (it.operPdCl.isNullOrBlank()) {
            tvPlayTime.text = "등록된 운영기간 정보가 없습니다."
        } else {
            tvPlayTime.text = "운영기간 - ${it.operPdCl}"
        }
        var bottom = ""
        if (it.siteBottomCl1 != "0") bottom += "잔디, "
        if (it.siteBottomCl2 != "0") bottom += "파쇄석, "
        if (it.siteBottomCl3 != "0") bottom += "테크, "
        if (it.siteBottomCl4 != "0") bottom += "자갈, "
        if (it.siteBottomCl5 != "0") bottom += "맨흙"
        tvBottom.text = "바닥재질 - ${bottom}"
        if (it.intro.isNullOrBlank()) {
            tvIntroduceComment.text = "등록된 내용이 없습니다."
        } else {
            tvIntroduceComment.text = it.intro
        }
        tvConvenienceComment.text =
            "편의시설 - 화장실: ${it.toiletCo} 샤워실: ${it.swrmCo} 개수대: ${it.wtrplCo} 화로대-${it.brazierCl}"
        if (it.sbrsCl.isNullOrEmpty()) {
            tvConvenienceComment2.text = "등록된 부대시설이 없습니다."
        } else {
            tvConvenienceComment2.text = "부대시설 - ${it.sbrsCl}"
        }
        if (it.themaEnvrnCl.isNullOrEmpty()) {
            tvConvenienceThema.text = "등록된 테마가 없습니다."
        } else {
            tvConvenienceThema.text = "테마 - ${it.themaEnvrnCl}"
        }
        if (it.posblFcltyCl.isNullOrEmpty()) {
            tvConvenienceNear.text = "등록된 내용이 없습니다."
        } else {
            tvConvenienceNear.text = "주변이용가능시설 - ${it.posblFcltyCl}"
        }
        if (it.featureNm.isNullOrBlank()) {
            tvConvenienceFeature.text = "등록된 특징이 없습니다."
        } else {
            tvConvenienceFeature.text = "특징 - ${it.featureNm}"
        }

        ivArrowBack.setOnClickListener {
            finish()
        }
        val tell = it.tel
        ivCallCamping.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${tell}"))
            startActivity(intent)
        }
        val reserveUrl = it.resveUrl
        btnReserve.setOnClickListener {
            if (!reserveUrl.isNullOrBlank()) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(reserveUrl))
                startActivity(intent)
            } else {
                Toast.makeText(this@CampDetailActivity, "등록된 홈페이지가 없습니다.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun initView() = with(binding) {
        myId = intent.getStringExtra("campData")
        myId?.let { viewModel.setUpParkParameter(it) }
        viewModel.callIdData(myId!!)
        viewModel.registerRealtimeUpdates(myId!!)
        viewModel.checkComment(myId!!) //댓글 미리보기
        //리사이클러뷰 연결
        recyclerComment.adapter = listAdapter
        recyclerComment.layoutManager =
            LinearLayoutManager(this@CampDetailActivity, LinearLayoutManager.VERTICAL, false)
        scrollTab()
        comment()
        scrollListener()

        btnDetailsattel.setOnClickListener {
            when (maptype) {
                1 -> {
                    naverMap?.mapType = NaverMap.MapType.Satellite
                    maptype += 1
                    btnDetailsattel.text = "위성도"
                }

                2 -> {
                    naverMap?.mapType = NaverMap.MapType.Terrain
                    maptype += 1
                    btnDetailsattel.text = "지형도"
                }

                3 -> {
                    naverMap?.mapType = NaverMap.MapType.Basic
                    maptype = 1
                    btnDetailsattel.text = "기본"
                }
            }
        }

        fcMap.setOnTouchListener(object : OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_MOVE -> {
                        Log.d("test", "mapview: actionmove")
                        scrollView.setScrollingEnabled(false)
                        //scrollView.requestDisallowInterceptTouchEvent(true)
                    }

                    MotionEvent.ACTION_UP -> {
                        scrollView.setScrollingEnabled(true)
                    }

                    MotionEvent.ACTION_CANCEL -> {
                        scrollView.setScrollingEnabled(true)
                    }
                }
                return onTouchEvent(event)
            }
        })
        commentBottomSheet.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        }
        bottomSheetCancle.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun initBottomSheet() = with(binding) {
        behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.isHideable = true
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private fun scrollListener() = with(binding) {
        val fadeIn = AlphaAnimation(0f, 1f).apply { duration = 1000 }
        val fadeOut = AlphaAnimation(1f, 0f).apply { duration = 1000 }
        scrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY == 0) { //최상단일경우
                if (!isTop) {
                    floating.startAnimation(fadeOut)
                    floating.visibility = View.GONE
                    isTop = true
                }
            } else { //최상단이 아닐경우
                if (isTop) {
                    floating.startAnimation(fadeIn)
                    floating.visibility = View.VISIBLE
                    isTop = false
                }
            }
        }
        floating.setOnClickListener {
            scrollView.smoothScrollTo(0, 0)
        }
    }

    /**
     * 댓글
     */
    private fun comment() = with(binding) {
        commentPlusImage.setOnClickListener {
            openGalleryForImage()
        }
        commentSend.setOnClickListener {
            commentSend.hideKeyboardInput()
            UserApiClient.instance.me { user, error ->
                if (user?.id != null) {
                    val userDocRef = db.collection("users").document("Kakao${user.id}")
                    userDocRef
                        .get()
                        .addOnSuccessListener {
                            val userId = "Kakao${user.id}"
                            val userName = it.get("nickName")
                            val content = commentEdit.text.toString()
                            val date =
                                SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(
                                    Date()
                                )
                            val myImage = if (selectedImage.visibility == View.VISIBLE) {
                                myImage
                            } else {
                                ""
                            }
                            if (myImage.isNotBlank()) {
                                val myImageUri = Uri.parse(myImage)
                                viewModel.uploadImage(myImageUri) { imageUrl ->
                                    val myComment = myId?.let { it1 ->
                                        CampCommentEntity(
                                            userId,
                                            userName,
                                            content,
                                            date,
                                            Uri.parse(imageUrl),
                                            it1
                                        )
                                    }
                                    if (myComment != null) {
                                        viewModel.uploadComment(myId!!, myComment)
                                    }
                                    commentEdit.text.clear()
                                    selectedImage.setImageURI(null)
                                    selectedImage.visibility = View.GONE
                                    selectedImageDelete.visibility = View.GONE
                                }
                            } else {
                                val myComment =
                                    myId?.let { it1 ->
                                        CampCommentEntity(
                                            userId, userName, content, date, Uri.EMPTY,
                                            it1
                                        )
                                    }
                                if (myComment != null) {
                                    viewModel.uploadComment(myId!!, myComment)
                                }
                                commentEdit.text.clear()
                            }
                        }
                } else {
                    SnackbarUtil.showSnackBar(it)
                }
            }
        }
        selectedImageDelete.setOnClickListener {
            binding.selectedImage.setImageURI(null)
            binding.selectedImage.visibility = View.GONE
            binding.selectedImageDelete.visibility = View.GONE
            myImage = ""
        }
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            selectedImageUri?.let {
                binding.selectedImage.visibility = View.VISIBLE
                binding.selectedImageDelete.visibility = View.VISIBLE
                binding.selectedImage.setImageURI(it)
                myImage = it.toString()
            }
        }
    }

    private fun scrollTab() = with(binding) {
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
                    if (bookmarkedList != null && bookmarkedList.contains(myId)) {
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
                        if (bookmarkedList?.contains(myId) == true) {
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

    private fun modifyBookmarked(
        userDocRef: DocumentReference,
        bookmarkedList: List<*>?,
        deleteFromList: Boolean
    ) = with(binding) {
        Timber.tag("모디파이검사").d(bookmarkedList.toString())
        val updatedList = bookmarkedList?.toMutableList() ?: mutableListOf()
        if (deleteFromList) {
            updatedList.remove(myId)
            userDocRef.update("bookmarked", updatedList)
                .addOnSuccessListener {
                    Toast.makeText(binding.root.context, "북마크가 해제되었습니다.", Toast.LENGTH_SHORT).show()
                    ivArrowBookmark.setImageResource(R.drawable.ic_bookmark)
                }
                .addOnFailureListener {
                    Toast.makeText(binding.root.context, "북마크 해제에 실패했습니다.", Toast.LENGTH_SHORT)
                        .show()
                }
        } else {
            updatedList.add(myId)
            userDocRef.update("bookmarked", updatedList)
                .addOnSuccessListener {
                    Toast.makeText(binding.root.context, "북마크가 추가되었습니다.", Toast.LENGTH_SHORT).show()
                    ivArrowBookmark.setImageResource(R.drawable.ic_bookmark_checked)
                }
                .addOnFailureListener {
                    Toast.makeText(binding.root.context, "북마크 추가에 실패했습니다.", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

    override fun onMapReady(p0: NaverMap) {
        Log.d("Detail", "onMapReady")
        naverMap = p0
        //한번도 카메라 영역 제한
        naverMap?.minZoom = 6.0
        naverMap?.maxZoom = 18.0
        naverMap?.extent =
            LatLngBounds(LatLng(32.973077, 124.270981), LatLng(38.856197, 130.051725))
    }

    private fun View.hideKeyboardInput() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView = null
        Log.d("test", "맵뷰 파괴됨")
    }

    private fun makeMarker() {
        if (mapX != null && mapY != null) {
            val mapY = if (mapY.isNullOrEmpty()) 45.0 else mapY!!.toDouble()
            val mapX = if (mapX.isNullOrEmpty()) 130.0 else mapX!!.toDouble()
            val cameraPosition = CameraPosition(LatLng(mapY, mapX), 10.0)
            val marker = Marker()
            Timber.tag("test").d(naverMap.toString())
            marker.position = LatLng(mapY, mapX)
            marker.captionText = campName.toString()
            marker.captionRequestedWidth = 200
            marker.setCaptionAligns(Align.Top)
            marker.captionOffset = 10
            marker.captionTextSize = 18f
            marker.map = naverMap
            naverMap?.cameraPosition = cameraPosition
        }
    }

}
