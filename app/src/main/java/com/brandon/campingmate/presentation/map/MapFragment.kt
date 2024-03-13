package com.brandon.campingmate.presentation.map

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isGone
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.brandon.campingmate.presentation.map.adapter.DialogImgAdapter
import com.brandon.campingmate.domain.model.LocationBasedListItem
import com.brandon.campingmate.databinding.FragmentMapBinding
import com.brandon.campingmate.domain.model.CampEntity
import com.brandon.campingmate.domain.model.NaverItem
import com.brandon.campingmate.presentation.campdetail.CampDetailActivity
import com.brandon.campingmate.presentation.profile.ProfileViewModel
import com.kakao.sdk.user.UserApiClient
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraPosition
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
import kotlinx.coroutines.launch
import ted.gun0912.clustering.naver.TedNaverClustering
import timber.log.Timber

class MapFragment : Fragment(),OnMapReadyCallback {
    private var _binding : FragmentMapBinding? = null
    private val binding  get() = _binding!!
    private var mapView: MapView? = null
    private var naverMap: NaverMap? = null
    private var maptype : Int = 1
    private var context : Context? = null
    private val imgAdapter = DialogImgAdapter()
    private var tedNaverClustering: TedNaverClustering<LocationBasedListItem>?= null
    private var campDataList = mutableListOf<LocationBasedListItem>()
    private var imageList = mutableListOf<String>()
    private var bookMarkedList = mutableListOf<LocationBasedListItem>()
    private val markers = mutableListOf<Marker>()
    private val bookmarkMarkers = mutableListOf<Marker>()
    private val userId = "3378474735"
    private val viewModel by lazy {
        ViewModelProvider(this)[MapViewModel::class.java]
    }
    private lateinit var fusedLocationSource: FusedLocationSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        context = container?.context
        _binding = FragmentMapBinding.inflate(inflater,container,false)
        mapView = binding.mvMap
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
        initView()
        initViewModel()
        Timber.tag("mapfragment").d("mapview getMapAsync()")
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun initView() = with(binding){
        val map = viewModel.getBlParamHashmap()
        viewModel.getAllCampList(map)

        btnSattel.setOnClickListener {
            when(maptype){
                1 -> {
                    naverMap?.mapType = NaverMap.MapType.Satellite
                    maptype+=1
                    btnSattel.text = "위성도"
                }
                2 -> {
                    naverMap?.mapType = NaverMap.MapType.Terrain
                    maptype+=1
                    btnSattel.text = "지형도"
                }
                3 -> {
                    naverMap?.mapType = NaverMap.MapType.Basic
                    maptype=1
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
        rvCampImg.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
        rvCampImg.adapter = imgAdapter


    }

    private fun initViewModel() = with(viewModel){

        paramHashmap?.observe(viewLifecycleOwner){
            getCampList(it)
        }
        campList.observe(viewLifecycleOwner){
            if (campList.value?.isNotEmpty() == true) {
                campDataList = campList.value!!
//                for(camp in campDataList){
//                    val marker = Marker()
//                    marker.captionText = camp.facltNm.toString()
//                    marker.captionRequestedWidth = 200
//                    marker.setCaptionAligns(Align.Top)
//                    marker.captionOffset = 10
//                    if(camp.mapX.isNullOrEmpty() || camp.mapY.isNullOrEmpty()) {
//                        continue
//                    }
//                    marker.position = LatLng(camp.mapY.toDouble(),camp.mapX.toDouble())
//                    markers.add(marker)
//                }
                //Log.d("test","campdatalist개수 = ${campDataList.size}")

            }
        }

        imageRes.observe(viewLifecycleOwner){
            if (imageRes.value?.isNotEmpty() == true) {
                imageList = viewModel.imageRes.value!!
                imgAdapter.submitList(imageList )
            }
        }

        bookmarkedList.observe(viewLifecycleOwner){
            if (bookmarkedList.value?.isNotEmpty() == true) {
                bookMarkedList = bookmarkedList.value!!
                if(markers.isNotEmpty()) {
                    //hideMarker(markers)
                }
                bookmarkMarkers.clear()
                tedNaverClustering = TedNaverClustering.with<LocationBasedListItem>(requireContext(), naverMap!!)
                    .customMarker {
                        Marker().apply {
                            icon = MarkerIcons.RED
                            captionText = it.facltNm.toString()
                            captionRequestedWidth = 200
                            setCaptionAligns(Align.Top)
                            captionOffset = 10
                            captionTextSize = 18f
                            bookmarkMarkers .add(this)
                        }
                    }
                    .markerClickListener {
                        val tag = it.induty
                        val loc = it.lctCl
                        imgAdapter.clear()
                        binding.tvDialogcampname.setOnClickListener(null)
                        binding.tvDialogtag.text = "$tag · $loc"
                        binding.tvDialogcampname.text = it.facltNm
                        binding.tvDialoglocation.text = it.addr1
                        binding.clMapBottomDialog.isGone=false
                        binding.tvDialogcampname.setOnClickListener { view ->
                            val intent = Intent(requireContext(),CampDetailActivity::class.java)
                            var data = CampEntity(
                                contentId = it.contentId
                            )
                            intent.putExtra("campData",data)
                            startActivity(intent)
                        }
                        val param = getImgParamHashmap(it.contentId.toString())
                        viewModel.getImgList(param)
                    }
                    .minClusterSize(10)
                    .clusterBuckets(intArrayOf(50,50))
                    .items(bookMarkedList)
                    .make()
            }
        }

    }
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if(requestCode != LOCATION_PERMISSION_REQUEST_CODE){
            return
        }
        if (fusedLocationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (!fusedLocationSource.isActivated) { // 권한 거부됨
                naverMap?.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }

    }
    
    override fun onMapReady(p0: NaverMap) {
        naverMap = p0

        val uiSetting = naverMap?.uiSettings
        uiSetting?.isLocationButtonEnabled=true

        fusedLocationSource = FusedLocationSource(this,LOCATION_PERMISSION_REQUEST_CODE)
        naverMap?.locationSource = fusedLocationSource
        naverMap?.addOnCameraIdleListener {
            updateMarkers(naverMap!!,campDataList)
            //updateMarkers(naverMap!!,markers)
        }

        val cameraPosition = CameraPosition(LatLng(37.5440, 127.1265), 16.0)
        naverMap?.cameraPosition = cameraPosition
        //한번도 카메라 영역 제한
        naverMap?.minZoom = 6.0
        naverMap?.maxZoom = 18.0
        naverMap?.extent = LatLngBounds(LatLng(32.973077, 124.270981), LatLng(38.856197,130.051725 ))

        var bookmark = false
        binding.btnBookmark.setOnClickListener {
            if(bookmark){
                binding.btnBookmark.text = "전체"
                //hideMarker(bookmarkMarkers)
                //showMarker(markers)
                bookmark = false
                //viewModel.getBookmarkedCamp(userId,campDataList)
            }else{
                binding.btnBookmark.text = "북마크"
                //hideMarker(markers)
               // showMarker(bookmarkMarkers)
                bookmark = true
//                val map = viewModel.getBlParamHashmap()
//                viewModel.getAllCampList(map)
            }
        }

        tedNaverClustering = TedNaverClustering.with<LocationBasedListItem>(requireContext(), naverMap!!)
            .customMarker {
                Marker().apply {
                    captionText = it.facltNm.toString()
                    captionRequestedWidth = 200
                    setCaptionAligns(Align.Top)
                    captionOffset = 10
                    captionTextSize = 18f
                    markers.add(this)
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
                binding.clMapBottomDialog.isGone=false
                binding.clMapBottomDialog.setOnClickListener { view ->
                    val intent = Intent(requireContext(),CampDetailActivity::class.java)
                    var data = CampEntity(
                        contentId = it.contentId,
                    )
                    intent.putExtra("campData",data)
                    startActivity(intent)
                }
//                val param = viewModel.getImgParamHashmap(it.contentId.toString())
//                viewModel.getImgList(param)
            }
            .minClusterSize(10)
            .clusterBuckets(intArrayOf(200,20))
            .items(mutableListOf())
            .make()
    }


    override fun onStart() {
        super.onStart()
        mapView?.onStart()
        Timber.tag("mapfragment").d("mapview onStart()")
    }

    override fun onResume() {
        super.onResume()
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

    fun showMarker(marker: Marker, naverMap : NaverMap) {
        marker.map = naverMap
    }

    fun hideMarker(marker: Marker) {
        marker.map = null
    }


    //fun updateMarkers(naverMap: NaverMap, tMarkers: MutableList<Marker>){
    private fun updateMarkers(naverMap: NaverMap, campData: MutableList<LocationBasedListItem>){
        val mapBounds =  naverMap.contentBounds
        val clustering = mutableListOf<LocationBasedListItem>()

//        for(marker in tMarkers){
//            val position = marker.position
//            val lat = camp.mapY
//            val lon = camp.mapX
//            val position = LatLng(lat!!.toDouble(),lon!!.toDouble())
//            //Log.d("test","포지션= ${position}")
//            if(mapBounds.contains(position)){
//                showMarker(marker,naverMap)
//                //Log.d("test","마커 드러남")
//            }else {
//                hideMarker(marker)
//            }
//        }


            for(camp in campData){
                val lat = camp.mapY
                val lon = camp.mapX
                val position = LatLng(lat!!.toDouble(),lon!!.toDouble())
                //Log.d("test","포지션= ${position}")
                if(mapBounds.contains(position)){
                    clustering.add(camp)
                    tedNaverClustering?.addItem(camp)
                    //Log.d("test","마커 드러남")
                }
                else{
                    tedNaverClustering?.removeItem(camp)
                }
            }



        Log.d("test","화면 속 마커 갯수= ${clustering.size}")



    }
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }


}
