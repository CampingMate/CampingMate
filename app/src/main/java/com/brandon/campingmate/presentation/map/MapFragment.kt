package com.brandon.campingmate.presentation.map

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isGone
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.brandon.campingmate.presentation.map.adapter.DialogImgAdapter
import com.brandon.campingmate.domain.model.LocationBasedListItem
import com.brandon.campingmate.databinding.FragmentMapBinding
import com.brandon.campingmate.domain.model.CampEntity
import com.brandon.campingmate.domain.model.NaverItem
import com.brandon.campingmate.presentation.board.BoardEvent
import com.brandon.campingmate.presentation.campdetail.CampDetailActivity
import com.brandon.campingmate.presentation.common.SnackbarUtil
import com.brandon.campingmate.presentation.profile.ProfileViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.sdk.user.UserApiClient
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraAnimation
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import ted.gun0912.clustering.naver.TedNaverClustering
import timber.log.Timber

class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private var mapView: MapView? = null
    private var naverMap: NaverMap? = null
    private var maptype: Int = 1
    private var context: Context? = null
    private val imgAdapter = DialogImgAdapter()
    private var tedNaverClustering: TedNaverClustering<LocationBasedListItem>? = null
    private var campDataList = mutableListOf<LocationBasedListItem>()
    private var imageList = mutableListOf<String>()
    private var bookMarkedList = mutableListOf<LocationBasedListItem>()
    private val markers = mutableListOf<Marker>()
    private var bookmarkMarkers = mutableListOf<Marker>()
    private val viewModel by lazy {
        ViewModelProvider(this)[MapViewModel::class.java]
    }
    private lateinit var fusedLocationSource: FusedLocationSource
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        context = container?.context
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        mapView = binding.mvMap
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
        fusedLocationSource = FusedLocationSource(this, 1005)
        //Timber.tag("mapfragment").d("mapview getMapAsync()")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initView()
    }

    private fun initView() = with(binding) {
        val map = viewModel.getBlParamHashmap()
        viewModel.getAllCampList(map)

        btnSattel.setOnClickListener {
            when (maptype) {
                1 -> {
                    naverMap?.mapType = NaverMap.MapType.Satellite
                    maptype += 1
                    btnSattel.text = "위성도"
                }

                2 -> {
                    naverMap?.mapType = NaverMap.MapType.Terrain
                    maptype += 1
                    btnSattel.text = "지형도"
                }

                3 -> {
                    naverMap?.mapType = NaverMap.MapType.Basic
                    maptype = 1
                    btnSattel.text = "기본"
                }
            }
        }

        ivDialogclose.setOnClickListener {
            clMapBottomDialog.isGone = true
        }

//        button.setOnClickListener {
//            val intent = Intent(context, WebViewActivity::class.java)
//            startActivity(intent)
//        }
        rvCampImg.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvCampImg.adapter = imgAdapter


    }

    private fun initViewModel() = with(viewModel) {

        campList.observe(viewLifecycleOwner) {
            if (campList.value?.isNotEmpty() == true) {
                campDataList = campList.value!!
                campDataList.sortWith(compareBy<LocationBasedListItem> { it.mapX }.thenBy { it.mapY })
                val temp = mutableListOf<Marker>()
                for (camp in campDataList) {
                    val marker = Marker()
                    if (camp.mapX.isNullOrEmpty() || camp.mapY.isNullOrEmpty()) {
                        continue
                    }
                    marker.captionText = camp.facltNm.toString()
                    marker.captionRequestedWidth = 400
                    marker.setCaptionAligns(Align.Top)
                    marker.tag = camp.contentId
                    marker.captionOffset = 5
                    marker.captionTextSize = 16f
                    marker.position = LatLng(camp.mapY.toDouble(), camp.mapX.toDouble())
                    marker.setOnClickListener {
                        val tag = camp.induty
                        val loc = camp.lctCl
                        imgAdapter.clear()
                        binding.clMapBottomDialog.setOnClickListener(null)
                        binding.tvDialogtag.text = "$tag · $loc"
                        binding.tvDialogcampname.text = camp.facltNm
                        binding.tvDialoglocation.text = camp.addr1
                        binding.clMapBottomDialog.isGone = false
                        binding.clMapBottomDialog.setOnClickListener { view ->
                            val intent = Intent(requireContext(), CampDetailActivity::class.java)
                            var data = camp.contentId
                            intent.putExtra("campData", data)
                            startActivity(intent)
                        }
                        val param = viewModel.getImgParamHashmap(camp.contentId.toString())
                        viewModel.getImgList(param)
                        true
                    }
                    temp.add(marker)

                }
                //북마크 로드
                markers.addAll(temp)
                getBookmarkedList(campDataList)
            }
        }

        imageRes.observe(viewLifecycleOwner) {
            if (imageRes.value?.isNotEmpty() == true) {
                imageList = viewModel.imageRes.value!!
                imgAdapter.submitList(imageList)
            }
        }

        bookmarkedList.observe(viewLifecycleOwner) {
            Timber.tag("북마크 갱신 확인").d("mapview onStart()")
            bookMarkedList = bookmarkedList.value!!
            val temp = mutableListOf<Marker>()
            bookmarkMarkers.forEach {
                hideMarker(it)
            }
            markers.removeAll(bookmarkMarkers)

            for (camp in bookMarkedList) {
                val marker = Marker()
                if (camp.mapX.isNullOrEmpty() || camp.mapY.isNullOrEmpty()) {
                    continue
                }
                marker.captionText = camp.facltNm.toString()
                marker.captionRequestedWidth = 400
                marker.setCaptionAligns(Align.Top)
                marker.icon = MarkerIcons.RED
                marker.captionOffset = 5
                marker.captionTextSize = 16f
                marker.position = LatLng(camp.mapY.toDouble(), camp.mapX.toDouble())
                marker.setOnClickListener {
                    val tag = camp.induty
                    val loc = camp.lctCl
                    imgAdapter.clear()
                    binding.clMapBottomDialog.setOnClickListener(null)
                    binding.tvDialogtag.text = "$tag · $loc"
                    binding.tvDialogcampname.text = camp.facltNm
                    binding.tvDialoglocation.text = camp.addr1
                    binding.clMapBottomDialog.isGone = false
                    binding.clMapBottomDialog.setOnClickListener { view ->
                        val intent = Intent(requireContext(), CampDetailActivity::class.java)
                        var data = camp.contentId
                        intent.putExtra("campData", data)
                        startActivity(intent)
                    }
                    val param = viewModel.getImgParamHashmap(camp.contentId.toString())
                    viewModel.getImgList(param)
                    true
                }
                temp.add(marker)
            }
            bookmarkMarkers = temp
            markers.addAll(temp)
            if(naverMap != null){
                showCampSite(naverMap?.cameraPosition?.zoom!!, markers, naverMap!!, campDataList)
            }

        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != 1004) {
            return
        }
        if (fusedLocationSource.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (!fusedLocationSource.isActivated) { // 권한 거부됨
                naverMap?.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }

    }


    override fun onMapReady(p0: NaverMap) {
        naverMap = p0

        //한번도 카메라 영역 제한
        naverMap?.minZoom = 6.0
        naverMap?.maxZoom = 18.0
        naverMap?.extent =
            LatLngBounds(LatLng(32.973077, 124.270981), LatLng(38.856197, 130.051725))

        val uiSetting = naverMap?.uiSettings
        uiSetting?.isLocationButtonEnabled = true

        val cameraPosition = CameraPosition(LatLng(37.5664056, 126.9778222), 16.0)
        naverMap?.cameraPosition = cameraPosition


        naverMap?.locationSource = fusedLocationSource
        naverMap?.locationTrackingMode = LocationTrackingMode.NoFollow

        var isFirst = false
        naverMap?.addOnLocationChangeListener { location ->
            if (!isFirst) {
                val currentPosition = LatLng(location.latitude, location.longitude)
                val cameraUpdate = CameraUpdate.scrollTo(currentPosition)
                naverMap?.moveCamera(cameraUpdate)
                isFirst = true
            }
        }
        var bookmark = false
        naverMap?.addOnCameraIdleListener {
            //Timber.tag("test").d(naverMap?.cameraPosition?.zoom.toString())
            if (bookmark) {
                showCampSite(
                    naverMap?.cameraPosition?.zoom!!,
                    bookmarkMarkers,
                    naverMap!!,
                    bookMarkedList
                )
            } else {
                showCampSite(naverMap?.cameraPosition?.zoom!!, markers, naverMap!!, campDataList)
            }

        }

        binding.btnBookmark.setOnClickListener {
            if (bookmark) {
                binding.btnBookmark.text = "전체"
                bookmarkMarkers.forEach {
                    hideMarker(it)
                }
                showCampSite(naverMap?.cameraPosition?.zoom!!, markers, naverMap!!, campDataList)
                bookmark = false
            } else {
                UserApiClient.instance.me { user, _ ->
                    if (user?.id == null) {
                        SnackbarUtil.showSnackBar(it)
                        return@me
                    } else {
                        binding.btnBookmark.text = "북마크"
                        markers.forEach {
                            hideMarker(it)
                        }
                        showCampSite(
                            naverMap?.cameraPosition?.zoom!!,
                            bookmarkMarkers,
                            naverMap!!,
                            bookMarkedList
                        )
                        bookmark = true
                    }
                }

            }
        }

        tedNaverClustering =
            TedNaverClustering.with<LocationBasedListItem>(requireContext(), naverMap!!)
                .customMarker {
                    Marker().apply {
                        val zoom = naverMap?.cameraPosition?.zoom!!
                        if (zoom.toInt() > 13) {
                            captionText = it.facltNm.toString()
                        }
                        captionRequestedWidth = 200
                        setCaptionAligns(Align.Top)
                        captionOffset = 5
                        captionTextSize = 16f
                        //markers.add(this)
                    }
                }
                .markerClickListener {
                    val tag = it.induty
                    val loc = it.lctCl
                    imgAdapter.clear()
                    binding.clMapBottomDialog.setOnClickListener(null)
                    binding.tvDialogtag.text = "$tag · $loc"
                    binding.tvDialogcampname.text = it.facltNm
                    binding.tvDialoglocation.text = it.addr1
                    binding.clMapBottomDialog.isGone = false
                    binding.clMapBottomDialog.setOnClickListener { view ->
                        val intent = Intent(requireContext(), CampDetailActivity::class.java)
                        var data = CampEntity(
                            contentId = it.contentId,
                        )
                        intent.putExtra("campData", data)
                        startActivity(intent)
                    }
                    val param = viewModel.getImgParamHashmap(it.contentId.toString())
                    viewModel.getImgList(param)
                }
                .minClusterSize(0)
                .clusterBuckets(intArrayOf(5000, 100))
                .items(mutableListOf())
                .make()
    }


    override fun onStart() {
        super.onStart()
        mapView?.onStart()
        getBookmarkedList(campDataList)
        Timber.tag("mapfragment").d("mapview onStart()")
    }

    override fun onResume() {
        super.onResume()
        with(viewModel){

        }
        mapView?.onResume()
        Timber.tag("mapfragment").d("mapview onResume()")
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()

        Timber.tag("mapfragment").d("mapview onPause()")
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
        Timber.tag("mapfragment").d("mapview onStop()")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView?.onDestroy()
        _binding = null
        Timber.tag("mapfragment").d("mapview onDestroyView()")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
        Timber.tag("mapfragment").d("mapview onSaveInstanceState()")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
        Timber.tag("mapfragment").d("mapview onLowMemory()")
    }

    private fun showMarker(marker: Marker, naverMap: NaverMap) {
        marker.map = naverMap
    }

    private fun hideMarker(marker: Marker) {
        marker.map = null
    }


    //fun updateMarkers(naverMap: NaverMap, tMarkers: MutableList<Marker>){
    private fun updateCluster(
        naverMap: NaverMap,
        campData: MutableList<LocationBasedListItem>,
        mMarkers: MutableList<Marker>
    ) {

        mMarkers.forEach {
            hideMarker(it)
        }

        CoroutineScope(Dispatchers.Default).launch {

            tedNaverClustering?.clearItems()
            val middleIdx = campData.size / 4

            val job1 = async {
                showCampData(campData, naverMap, 0, middleIdx)
            }
            val job2 = async {
                showCampData(campData, naverMap, middleIdx, middleIdx * 2)
            }
            val job3 = async {
                showCampData(campData, naverMap, middleIdx * 2, middleIdx * 3)
            }
            val job4 = async {
                showCampData(campData, naverMap, middleIdx * 3, campData.size)
            }
            val list = listOf(job1, job2, job3, job4)
            list.awaitAll()
        }
    }

    private fun showCampData(
        campData: MutableList<LocationBasedListItem>,
        naverMap: NaverMap,
        startIdx: Int,
        endIdx: Int
    ) {
        val mapBounds = naverMap.contentBounds
        val clustering = mutableListOf<LocationBasedListItem>()
        for (i in startIdx until endIdx) {
            val lat = campData[i].mapY
            val lon = campData[i].mapX
            val position = LatLng(lat!!.toDouble(), lon!!.toDouble())
            //Log.d("test","포지션= ${position}")
            if (mapBounds.contains(position)) {
                clustering.add(campData[i])
            }
        }
        tedNaverClustering?.addItems(clustering)
    }

    private fun updateMarkers(cMarkers: MutableList<Marker>, naverMap: NaverMap) {
        val mapBounds = naverMap.contentBounds
        tedNaverClustering?.clearItems()

        for (marker in cMarkers) {
            val position = marker.position
            //Log.d("test","포지션= ${position}")
            if (mapBounds.contains(position)) {
                marker.captionOffset = 5
                showMarker(marker, naverMap)
                //Log.d("test","마커 드러남")
            } else {
                hideMarker(marker)
            }
        }
    }

    private fun updateNoCaptionMarkers(cMarkers: MutableList<Marker>, naverMap: NaverMap) {
        val mapBounds = naverMap.contentBounds
        tedNaverClustering?.clearItems()

        for (marker in cMarkers) {
            val position = marker.position
            //Log.d("test","포지션= ${position}")
            if (mapBounds.contains(position)) {
                marker.captionOffset = 9999
                showMarker(marker, naverMap)
                //Log.d("test","마커 드러남")
            } else {
                hideMarker(marker)
            }
        }
    }

    private fun showCampSite(
        zoom: Double,
        mMarkers: MutableList<Marker>,
        map: NaverMap,
        campData: MutableList<LocationBasedListItem>
    ) {
        when (zoom.toInt()) {
            in 13..18 -> updateMarkers(mMarkers, map)
            in 11..12 -> updateNoCaptionMarkers(mMarkers, map)
            in 6..10 -> updateCluster(map, campData, mMarkers)
        }
    }

    private fun getBookmarkedList(campData: MutableList<LocationBasedListItem>) {
        UserApiClient.instance.me { user, error ->
            if (user?.id != null) {
                val docRef = db.collection("users").document("Kakao${user?.id}")
                docRef.get().addOnSuccessListener {
                    viewModel.getBookmarkedCamp(it.id, campData)
                    Log.d("로그인 정보","북마크 리스트 = ${it.id}")
                }
            }
        }

    }
}
