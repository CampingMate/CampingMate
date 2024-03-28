package com.brandon.campingmate.presentation.map

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.brandon.campingmate.data.local.preferences.EncryptedPrefs
import com.brandon.campingmate.databinding.FragmentMapBinding
import com.brandon.campingmate.domain.model.CampEntity
import com.brandon.campingmate.domain.model.LocationBasedListItem
import com.brandon.campingmate.presentation.campdetail.CampDetailActivity
import com.brandon.campingmate.presentation.common.SnackbarUtil
import com.brandon.campingmate.presentation.map.adapter.DialogImgAdapter
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import ted.gun0912.clustering.naver.TedNaverClustering

class MapFragment : Fragment(), OnMapReadyCallback {
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private var mapView: MapView? = null
    private var naverMap: NaverMap? = null
    private val imgAdapter = DialogImgAdapter()
    private var tedNaverClustering: TedNaverClustering<LocationBasedListItem>? = null
    private var campDataList = mutableListOf<LocationBasedListItem>()
    private var mapFragmentBookMarkedList = mutableListOf<LocationBasedListItem>()
    private var mapFragmentMarkers = mutableListOf<Marker>()
    private var bookmarkMarkers = mutableListOf<Marker>()
    private val viewModel by lazy {
        ViewModelProvider(this)[MapViewModel::class.java]
    }
    private lateinit var fusedLocationSource: FusedLocationSource
    var userId: String? = EncryptedPrefs.getMyId()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        binding.spinnerSattel.lifecycleOwner = this
    }

    private fun initView() = with(binding) {

        spinnerSattel.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newItem ->
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

        ivDialogclose.setOnClickListener {
            clMapBottomDialog.isGone = true
        }

        rvCampImg.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvCampImg.adapter = imgAdapter


    }

    private fun initViewModel() = with(viewModel) {
        val map = getBlParamHashmap()
        getAllCampList(map)
        campList.observe(viewLifecycleOwner) {
            val campdatalists = it
            campDataList = campdatalists
            val tempList = mutableListOf<Marker>()
            for (camp in campdatalists) {
                val marker = Marker()
                if (camp.mapX.isNullOrEmpty() || camp.mapY.isNullOrEmpty()) {
                    continue
                }
                marker.captionText = camp.facltNm.toString()
                marker.captionRequestedWidth = 400
                marker.setCaptionAligns(Align.Top)
                marker.tag = camp
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
                    //뷰모델에 이미지 불러오기 실행
                    val param = viewModel.getImgParamHashmap(camp.contentId.toString())
                    viewModel.getImgList(param)
                    true
                }
                tempList.add(marker)
            }
            mapFragmentMarkers = tempList
            if(userId != null){
                getBookmarkedList(campdatalists,userId!!)
            }
        }

        imageRes.observe(viewLifecycleOwner) {
            val imgList = it
            if (imgList.isNotEmpty()) {
                imgAdapter.submitList(imgList)
            }
        }

        bookmarkedList.observe(viewLifecycleOwner) {
            val bookmarkedCampLists = it
            mapFragmentBookMarkedList = bookmarkedCampLists
            bookmarkMarkers.forEach {
                hideMarker(it)
            }
            mapFragmentMarkers.removeAll(bookmarkMarkers)
            //마커에 클릭리스너 추가
            val bookmarkMarker = mutableListOf<Marker>()

            for (camp in bookmarkedCampLists) {
                val marker = Marker()
                if (camp.mapX.isNullOrEmpty() || camp.mapY.isNullOrEmpty()) {
                    continue
                }
                marker.captionText = camp.facltNm.toString()
                marker.captionRequestedWidth = 400
                marker.setCaptionAligns(Align.Top)
                marker.icon = MarkerIcons.RED
                marker.tag = camp
                marker.captionOffset = 5
                marker.captionTextSize = 16f
                marker.position = LatLng(camp.mapY.toDouble(), camp.mapX.toDouble())
                marker.setOnClickListener {
                    val tag = camp.induty
                    val loc =camp.lctCl
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
                    //뷰모델에 이미지 불러오기 실행
                    val param = viewModel.getImgParamHashmap(camp.contentId.toString())
                    viewModel.getImgList(param)
                    true
                }
                bookmarkMarker.add(marker)
            }
            bookmarkMarkers = bookmarkMarker
            mapFragmentMarkers.addAll(bookmarkMarker)
            if(naverMap != null){
                showCampSite(naverMap?.cameraPosition?.zoom!!,mapFragmentMarkers, naverMap!!, campDataList)
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
                    naverMap?.cameraPosition?.zoom,
                    bookmarkMarkers,
                    naverMap,
                    mapFragmentBookMarkedList
                )
            } else {
                showCampSite(naverMap?.cameraPosition?.zoom, mapFragmentMarkers, naverMap, campDataList)
            }

        }

        binding.switchBookmark.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (userId == null) {
                    SnackbarUtil.showSnackBar(buttonView)
                    buttonView.isChecked = false
                } else {
                    mapFragmentMarkers.forEach {
                        hideMarker(it)
                    }
                    showCampSite(
                        naverMap?.cameraPosition?.zoom,
                        bookmarkMarkers,
                        naverMap,
                        mapFragmentBookMarkedList
                    )
                    bookmark = true
                }
            } else {
                bookmarkMarkers.forEach {
                    hideMarker(it)
                }
                showCampSite(naverMap?.cameraPosition?.zoom, mapFragmentMarkers, naverMap, campDataList)
                bookmark = false
            }
        }


        naverMap?.let {
            tedNaverClustering =
                TedNaverClustering.with<LocationBasedListItem>(requireContext(),it)
                    .customMarker {
                        Marker().apply {
                            val zoom = naverMap?.cameraPosition?.zoom
                            if(zoom != null){
                                if (zoom.toInt() > 13) {
                                    captionText = it.facltNm.toString()
                                }
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
                    .clusterClickListener {

                    }
                    .minClusterSize(0)
                    .clusterBuckets(intArrayOf(5000, 100))
                    .items(mutableListOf())
                    .make()

        }

    }


    override fun onStart() {
        super.onStart()
        mapView?.onStart()
        userId = EncryptedPrefs.getMyId()
        if(userId != null){
            viewModel.getBookmarkedList(campDataList,userId!!)
        }
    }

    override fun onResume() {
        mapView?.onResume()
        super.onResume()

    }

    override fun onPause() {
        binding.spinnerSattel.dismiss()
        mapView?.onPause()
        super.onPause()

    }

    override fun onStop() {
        mapView?.onStop()
        super.onStop()

    }

    override fun onDestroyView() {
        mapView?.onDestroy()
        _binding = null
        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    private fun showMarker(marker: Marker, naverMap: NaverMap?) {
        if(naverMap != null){
            marker.map = naverMap
        }

    }

    private fun hideMarker(marker: Marker) {
        marker.map = null
    }

    private fun updateCluster(
        naverMap: NaverMap?,
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
                addCampData(campData, naverMap, 0, middleIdx)
            }
            val job2 = async {
                addCampData(campData, naverMap, middleIdx, middleIdx * 2)
            }
            val job3 = async {
                addCampData(campData, naverMap, middleIdx * 2, middleIdx * 3)
            }
            val job4 = async {
                addCampData(campData, naverMap, middleIdx * 3, campData.size)
            }
            val list = listOf(job1, job2, job3, job4)
            list.awaitAll()
        }
    }

    private fun addCampData(
        campData: MutableList<LocationBasedListItem>,
        naverMap: NaverMap?,
        startIdx: Int,
        endIdx: Int
    ) {
        val mapBounds = naverMap?.contentBounds
        val clustering = mutableListOf<LocationBasedListItem>()
        for (i in startIdx until endIdx) {
            val lat = campData[i].mapY
            val lon = campData[i].mapX
            val position = LatLng(lat!!.toDouble(), lon!!.toDouble())
            //Log.d("test","포지션= ${position}")
            if (mapBounds?.contains(position) == true) {
                clustering.add(campData[i])
            }
        }
        tedNaverClustering?.addItems(clustering)
    }

    private fun updateMarkers(cMarkers: MutableList<Marker>, naverMap: NaverMap?) {
        val mapBounds = naverMap?.contentBounds
        tedNaverClustering?.clearItems()

        for (marker in cMarkers) {
            val position = marker.position
            //Log.d("test","포지션= ${position}")
            if (mapBounds?.contains(position) == true) {
                marker.captionOffset = 5
                showMarker(marker, naverMap)
                //Log.d("test","마커 드러남")
            } else {
                hideMarker(marker)
            }
        }
    }

    private fun updateNoCaptionMarkers(cMarkers: MutableList<Marker>, naverMap: NaverMap?) {
        val mapBounds = naverMap?.contentBounds
        tedNaverClustering?.clearItems()

        for (marker in cMarkers) {
            val position = marker.position
            //Log.d("test","포지션= ${position}")
            if (mapBounds?.contains(position) == true) {
                marker.captionOffset = 9999
                showMarker(marker, naverMap)
                //Log.d("test","마커 드러남")
            } else {
                hideMarker(marker)
            }
        }
    }

    private fun showCampSite(
        zoom: Double?,
        mMarkers: MutableList<Marker>,
        map: NaverMap?,
        campData: MutableList<LocationBasedListItem>
    ) {
        if(map != null) {
            when (zoom?.toInt()) {
                in 13..18 -> updateMarkers(mMarkers, map)
                in 11..12 -> updateNoCaptionMarkers(mMarkers, map)
                in 6..10 -> updateCluster(map, campData, mMarkers)
            }
        }
    }
}