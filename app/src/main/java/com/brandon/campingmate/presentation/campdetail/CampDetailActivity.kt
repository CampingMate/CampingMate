package com.brandon.campingmate.presentation.campdetail

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.brandon.campingmate.R
import com.brandon.campingmate.data.local.preferences.EncryptedPrefs
import com.brandon.campingmate.databinding.ActivityCampDetailBinding
import com.brandon.campingmate.databinding.BottomSheetPostdetailCommnetSideMenuBinding
import com.brandon.campingmate.domain.model.CampCommentEntity
import com.brandon.campingmate.domain.model.CampEntity
import com.brandon.campingmate.presentation.campdetail.adapter.CommentListAdapter
import com.brandon.campingmate.presentation.campdetail.adapter.ViewPagerAdapter
import com.brandon.campingmate.presentation.common.SnackbarUtil
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.Marker
import timber.log.Timber

class CampDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private val binding by lazy { ActivityCampDetailBinding.inflate(layoutInflater) }
    private val viewModel by lazy {
        ViewModelProvider(this)[CampDetailViewModel::class.java]
    }
    private val listAdapter: CommentListAdapter by lazy { CommentListAdapter({ commentImage ->
        commentImageClick(commentImage)
    }) }

    private fun commentImageClick(commentImage: String) {
//        Toast.makeText(this, "이미지클릭", Toast.LENGTH_SHORT).show()
        val dialog = ImageDialog(this, commentImage)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog.show()
    }

    var userId: String? = EncryptedPrefs.getMyId()
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
    private var sendLoading: Boolean = false

    companion object {
        private const val REQUEST_CODE_IMAGE_PICK = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
        initViewModel()
        initBottomSheet()
        clickBookmarked()
        binding.spinnerDetailsattel.lifecycleOwner = this
        mapView = binding.fcMap
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
    }

    override fun onResume() {
        super.onResume()
        userId = EncryptedPrefs.getMyId()
        if(userId != null){
            binding.commentEdit.isFocusableInTouchMode = true
        }
        checkBookmarked()
    }

    private fun initViewPager() {
        preloadImages(imageUrls)
        binding.viewPager.adapter = ViewPagerAdapter(imageUrls)
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.springDotsIndicator.attachTo(binding.viewPager)
    }

    private fun preloadImages(urls: List<String>) {
        for (image in urls) {
            Glide.with(this).load(image).preload()
        }
    }

    private fun initViewModel() = with(viewModel) {
        imageResult.observe(this@CampDetailActivity) {
            if (it.isNotEmpty()) {
                imageUrls.addAll(it)
                Log.d("imageUrls", "이미지 들어오는속도 확인 : ${imageUrls}")
                initViewPager()
            }
        }
        campEntity.observe(this@CampDetailActivity) {
            if (it != null) {
                initSetting(it)
            }
        }
        campComment.observe(this@CampDetailActivity) {
            if (it != null) {
                listAdapter.submitList(it)
                with(binding) {
                    if (it.isEmpty()) {
                        recyclerComment.visibility = View.INVISIBLE
                        tvNoComment.visibility = View.VISIBLE
                    } else {
                        recyclerComment.visibility = View.VISIBLE
                        tvNoComment.visibility = View.INVISIBLE
                    }
                    loadingAnimation.visibility = View.INVISIBLE
                    commentEdit.text.clear()
                    selectedImage.setImageURI(null)
                    selectedImage.visibility = View.GONE
                    selectedImageDelete.visibility = View.GONE
                    sendLoading = false
                    commentEdit.clearFocus()
                }
            }
        }
        checkLastComment.observe(this@CampDetailActivity) {
            if (it != null) {
                binding.commentContent.text = it
            }
        }
        commentCount.observe(this@CampDetailActivity) {
            binding.commentCount.text = it
        }

    }

    private fun initSetting(it: CampEntity) = with(binding) {
        mapX = it.mapX
        mapY = it.mapY
        campName = it.facltNm

        tvCampName.text = it.facltNm
        if(it.addr1.isNullOrBlank()){
            tvAddr.text = "등록된 주소가 없습니다."
        } else{
            tvAddr.text = it.addr1
        }
        if(it.tel.isNullOrBlank()){
            tvCall.text = "등록된 번호가 없습니다."
        } else{
            tvCall.text = it.tel
        }
        if (it.homepage.isNullOrBlank()) {
//            tvHomepage.text = "등록된 홈페이지가 없습니다."
            tvHomepage.visibility = View.GONE
        } else {
            tvHomepage.text = "홈페이지 - ${it.homepage}"
        }
        if (it.allar == "0") {
//            tvSize.text = "등록된 면적 정보가 없습니다."
            tvSize.visibility = View.GONE
        } else {
            tvSize.text = "면적 - ${it.allar}㎡"
        }
        if (it.hvofBgnde.isNullOrBlank() && it.hvofEnddle.isNullOrBlank()) {
//            tvRestTime.text = "등록된 휴장기간 정보가 없습니다."
            tvRestTime.visibility = View.GONE
        } else {
            tvRestTime.text = "휴장기간 ${it.hvofBgnde} ~ ${it.hvofEnddle} "
        }
        if (it.operPdCl.isNullOrBlank()) {
//            tvPlayTime.text = "등록된 운영기간 정보가 없습니다."
            tvPlayTime.visibility = View.GONE
        } else {
            tvPlayTime.text = "운영기간 - ${it.operPdCl}"
        }
//        var bottom = ""
//        if (it.siteBottomCl1 != "0") bottom += "잔디, "
//        if (it.siteBottomCl2 != "0") bottom += "파쇄석, "
//        if (it.siteBottomCl3 != "0") bottom += "테크, "
//        if (it.siteBottomCl4 != "0") bottom += "자갈, "
//        if (it.siteBottomCl5 != "0") bottom += "맨흙"
//        tvBottom.text = "바닥재질 - ${bottom}"
//        tvBottom.text = ""
//        tvBottom.visibility = View.GONE
        if (it.intro.isNullOrBlank()) {
            tvIntroduceComment.text = "등록된 내용이 없습니다."
        } else {
            tvIntroduceComment.text = it.intro
        }
        tvConvenienceComment.text =
            "편의시설 - 화장실: ${it.toiletCo} 샤워실: ${it.swrmCo} 개수대: ${it.wtrplCo} 화로대-${it.brazierCl}"
        if (it.sbrsCl.isNullOrEmpty()) {
//            tvConvenienceComment2.text = "등록된 부대시설이 없습니다."
            tvConvenienceComment2.visibility = View.GONE
        } else {
            tvConvenienceComment2.text = "부대시설 - ${it.sbrsCl}"
        }
        if (it.themaEnvrnCl.isNullOrEmpty()) {
//            tvConvenienceThema.text = "등록된 테마가 없습니다."
            tvConvenienceThema.visibility = View.GONE
        } else {
            tvConvenienceThema.text = "테마 - ${it.themaEnvrnCl}"
        }
        if (it.posblFcltyCl.isNullOrEmpty()) {
//            tvConvenienceNear.text = "등록된 내용이 없습니다."
            tvConvenienceNear.visibility = View.GONE
        } else {
            tvConvenienceNear.text = "주변이용가능시설 - ${it.posblFcltyCl}"
        }
        if (it.featureNm.isNullOrBlank()) {
//            tvConvenienceFeature.text = "등록된 특징이 없습니다."
            tvConvenienceFeature.visibility = View.GONE
        } else {
            tvConvenienceFeature.text = "특징 - ${it.featureNm}"
        }

        ivArrowBack.setOnClickListener {
            finish()
        }
        callLayout.setOnClickListener {
            if (tvCall.text == "등록된 번호가 없습니다.") {
                return@setOnClickListener
            }
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${tvCall.text}"))
            startActivity(intent)
        }
        val reserveUrl = it.resveUrl
        btnReserve.setOnClickListener {
            if (!reserveUrl.isNullOrBlank() && reserveUrl.startsWith("http")) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(reserveUrl))
                startActivity(intent)
            } else {
                Toast.makeText(this@CampDetailActivity, "등록된 홈페이지가 없습니다.", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.btnDetailroute.setOnClickListener { view ->
            openMap(it.mapY!!.toDouble(), it.mapX!!.toDouble(), it.facltNm)
        }
        viewModel.callMart(mapY!!.toDouble(),mapX!!.toDouble() )

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

        spinnerDetailsattel.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->
            when (newIndex) {
                0 -> {
                    naverMap?.mapType = NaverMap.MapType.Basic
                }
                1 -> {
                    naverMap?.mapType = NaverMap.MapType.Satellite
                }
                2 -> {
                    naverMap?.mapType = NaverMap.MapType.Terrain
                }
                else-> naverMap?.mapType = NaverMap.MapType.Basic
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
            bottomSheetOverlay.visibility = View.VISIBLE
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            val screenHeight = resources.displayMetrics.heightPixels // 화면의 높이를 가져옴
            val peekHeightRatio = 0.7 // 바텀시트가 화면의 70%까지 보이도록 설정
            behavior.peekHeight = (screenHeight * peekHeightRatio).toInt()
            spinnerDetailsattel.dismiss()
        }
        bottomSheetCancle.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    private fun initBottomSheet() = with(binding) {
        behavior = BottomSheetBehavior.from(bottomSheet)
        behavior.isHideable = true
        behavior.state = BottomSheetBehavior.STATE_HIDDEN

        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    viewModel.checkComment(myId!!)
                    bottomSheetOverlay.visibility = View.GONE
                    commentEdit.clearFocus()
                    binding.root.hideKeyboard()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })
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
        linearComment.setOnClickListener {
            if (userId == null) {
                SnackbarUtil.showSnackBar(it)
                binding.root.hideKeyboard()
                behavior.state = BottomSheetBehavior.STATE_HIDDEN
                return@setOnClickListener
            }
        }
        commentLayout.setOnClickListener {
            if (userId == null) {
                SnackbarUtil.showSnackBar(it)
                binding.root.hideKeyboard()
                behavior.state = BottomSheetBehavior.STATE_HIDDEN
                return@setOnClickListener
            }
        }
        commentPlusImage.setOnClickListener {
            if (userId == null) {
                SnackbarUtil.showSnackBar(it)
                binding.root.hideKeyboard()
                behavior.state = BottomSheetBehavior.STATE_HIDDEN
                return@setOnClickListener
            } else{
                openGalleryForImage()
            }
        }
        commentEdit.setOnClickListener {
            if(userId == null){
                SnackbarUtil.showSnackBar(it)
                binding.root.hideKeyboard()
                behavior.state = BottomSheetBehavior.STATE_HIDDEN
                return@setOnClickListener
            }
        }
        commentSend.setOnClickListener {
            val content = commentEdit.text.toString()
            if (content.isBlank()) {
                sendLoading = false
                return@setOnClickListener
            }
            if (userId != null) {
                if (!sendLoading) {
                    sendLoading = true
                    loadingAnimation.visibility = View.VISIBLE
                    commentSend.hideKeyboardInput()

                    val myImage = if (selectedImage.visibility == View.VISIBLE) {
                        myImage
                    } else {
                        ""
                    }
                    val campId = myId
                    viewModel.bringUserData(userId!!, content, myImage, campId!!)
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
        if (userId != null) {
            val userDocRef = db.collection("users").document(userId!!)
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

    private fun clickBookmarked() = with(binding) {
        ivArrowBookmark.setOnClickListener {
            if (userId == null) {
                SnackbarUtil.showSnackBar(it)
            } else {
                val userDocRef = db.collection("users").document(userId!!)
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
        naverMap = p0
        //한번도 카메라 영역 제한
        naverMap?.minZoom = 6.0
        naverMap?.maxZoom = 18.0
        naverMap?.extent =
            LatLngBounds(LatLng(32.973077, 124.270981), LatLng(38.856197, 130.051725))
        //Log.d("check", "onMapReady = ${mapX}, ${mapY}, ${campName},${naverMap}")
        val cameraPosition = CameraPosition(LatLng(37.5664056, 126.9778222), 16.0)
        naverMap?.cameraPosition = cameraPosition



        viewModel.campEntity.observe(this@CampDetailActivity){
            Log.d("Detail", " initsettiong = ${mapX}, ${mapY}, ${campName}, ${naverMap}")
            if (naverMap != null) {
                Log.d("Detail", "initsettiong 안에 마커만들기 실행됨")
                if (it != null) {
                    makeMarker(it.mapX, it.mapY, it.facltNm, naverMap)
                }
            } else {
                Toast.makeText(this@CampDetailActivity,"위치 정보가 없어 지도에 표시할 수 없습니다.",Toast.LENGTH_SHORT).show()
            }
            binding.btnRelocation.setOnClickListener{view->
                val cameraPosition = CameraPosition(LatLng(it?.mapY?.toDouble()!!, it.mapX?.toDouble()!!), 16.0)
                naverMap?.cameraPosition = cameraPosition
            }
        }

        viewModel.martMarker.observe(this@CampDetailActivity){
            val markers = it
            for(martMarker in markers){
                if(naverMap != null){
                    martMarker.map = naverMap
                }

            }
        }

    }

    private fun View.hideKeyboardInput() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView = null
    }

    private fun makeMarker(mapX: String?, mapY: String?, campName: String?, map: NaverMap?) {
        if (mapX != null && mapY != null) {
            val mapY = if (mapY.isNullOrEmpty()) 45.0 else mapY.toDouble()
            val mapX = if (mapX.isNullOrEmpty()) 130.0 else mapX.toDouble()
            val cameraPosition = CameraPosition(LatLng(mapY, mapX), 11.0)
            val marker = Marker()
            marker.position = LatLng(mapY, mapX)
            marker.captionText = campName.toString()
            marker.captionRequestedWidth = 200
            marker.setCaptionAligns(Align.Top)
            marker.captionOffset = 10
            marker.captionTextSize = 16f
            marker.map = map
            map?.cameraPosition = cameraPosition
        }
    }


    private fun openMap(
        endLat: Double, endlon: Double, name: String?
    ) {
        val url =
            "nmap://route/car?dlat=${endLat}&dlng=${endlon}&dname=${name}&appname=com.brandon.campingmate"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        var installCheck: PackageInfo? = null
        try {
            installCheck = packageManager.getPackageInfo("com.nhn.android.nmap", 0)

        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
        if (installCheck?.packageName.isNullOrEmpty()) {
            val uri =
                "http://m.map.naver.com/route.nhn?menu=route&ename=${name}&ex=${endlon}&ey=${endLat}&pathType=0&showMap=true"
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(uri)
                )
            )
        } else {
            startActivity(intent)
        }

    }

    fun showBottomSheetCommentMenu(
        comment: CampCommentEntity,
    ) {
        val user = comment.userId
        val admin = arrayOf(
            "Kakao3378858947",
            "Kakao3378858360",
            "Kakao3378474735",
            "GooglefzN0fi888dOR7eBQlAwRqClg3Me2",
            "Google2Gy9bYVkj0NdlzC8MYTEEqaW57s1",)
        val isOwner = user == userId || admin.contains(userId)
        val campId = comment.campId

        showBottomSheetCommentMenu(isOwner, campId, comment)
    }

    private fun showBottomSheetCommentMenu(
        isOwner: Boolean, campId: String?, comment: CampCommentEntity
    ) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetBinding = BottomSheetPostdetailCommnetSideMenuBinding.inflate(layoutInflater)

        if (isOwner) {
            bottomSheetBinding.btnMenuOwner.isVisible = true
            bottomSheetBinding.btnMenuNotOwner.isVisible = false
        } else {
            bottomSheetBinding.btnMenuOwner.isVisible = false
            bottomSheetBinding.btnMenuNotOwner.isVisible = true
        }

        bottomSheetDialog.setContentView(bottomSheetBinding.root)

        bottomSheetBinding.btnMenuOwner.setOnClickListener {
            Timber.tag("DELETE").d("삭제 이벤트 발생")
            // 삭제 동작 처리
            viewModel.deleteComment(campId!!, comment)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }


}
