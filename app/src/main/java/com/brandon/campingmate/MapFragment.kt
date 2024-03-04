package com.brandon.campingmate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import com.brandon.campingmate.databinding.FragmentMapBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.MarkerIcons
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ted.gun0912.clustering.clustering.TedClusterItem
import ted.gun0912.clustering.geometry.TedLatLng
import ted.gun0912.clustering.naver.TedNaverClustering
import timber.log.Timber

class MapFragment : Fragment(),OnMapReadyCallback {
    private var _binding : FragmentMapBinding? = null
    private val binding  get() = _binding!!
    private var mapView: MapView? = null
    private var naverMap: NaverMap? = null
    private var maptype : Boolean = true
    private var context : Context? = null
    private val imgAdapter = DialogImgAdapter()
    private lateinit var tedNaverClustering: TedNaverClustering<MapModel>
    private var campDataList = mutableListOf<MapModel>()
    private var naverItems = mutableListOf<NaverItem>()
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
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSattel.setOnClickListener {
            if(maptype){
                naverMap?.mapType = NaverMap.MapType.Satellite
                maptype=false
                binding.btnSattel.text = "지형도"
            }else{
                naverMap?.mapType = NaverMap.MapType.Terrain
                maptype=true
                binding.btnSattel.text = "위성"
            }
        }

        binding.ivDialogclose.setOnClickListener {
            binding.clMapBottomDialog.isGone = true
        }

        binding.tvDialogcampname.setOnClickListener {
            //캠프디테일로 이동
        }

        binding.button.setOnClickListener {
            val intent = Intent(context,WebViewActivity::class.java)
            startActivity(intent)
        }
        //makeAllMarker()
        binding.rvCampImg.adapter = imgAdapter
    }


    
    override fun onMapReady(p0: NaverMap) {
        naverMap = p0
        val cameraPosition = CameraPosition(LatLng(36.60545, 127.9792), 6.0)
        naverMap?.cameraPosition = cameraPosition

        //백그라운드에서 불러온 마커가 저장되는 리스트
        var markers = mutableListOf<Marker>()

        val firebaseDatabase = FirebaseDatabase.getInstance()
        val db = Firebase.firestore

        val campsRef = db.collection("camps")

        campsRef
            .limit(100)
            .get()
            .addOnSuccessListener { documents ->

                for (document in documents) {
                    val indutyList = document.data["induty"].toString().split(",")
                    val lctClList = document.data["lctCl"].toString().split(",")
                    val addr1 = if(!document.data["addr1"].toString().isNullOrEmpty()) document.data["addr1"].toString() else "주소없음"
                    val doNm = if(!document.data["doNm"].toString().isNullOrEmpty()) document.data["doNm"].toString() else "미분류"
                    val facltNm = if(!document.data["facltNm"].toString().isNullOrEmpty()) document.data["facltNm"].toString() else "이름없음"
                    val firstImageUrl = if(!document.data["firstImageUrl"].toString().isNullOrEmpty()) document.data["firstImageUrl"].toString() else "https://pbs.twimg.com/media/EgkUVPaUwAAr6K6.jpg"
                    val induty = if(! indutyList.isNullOrEmpty()) indutyList else listOf("뷴류없음")
                    val lctCl = if(!lctClList.toString().isNullOrEmpty()) lctClList else listOf("뷴류없음")
                    val mapX = if(!document.data["mapX"].toString().isNullOrEmpty()) document.data["mapX"].toString() else "129.08832"
                    val mapY = if(!document.data["mapY"].toString().isNullOrEmpty()) document.data["mapY"].toString() else "35.67312"

                    campDataList.add(
                        MapModel(
                            addr1 = addr1,
                            doNm = doNm ,
                            facltNm = facltNm,
                            firstImageUrl = firstImageUrl,
                            induty =  induty,
                            lctCl = lctCl,
                            mapX = mapX,
                            mapY = mapY
                        )
                    )
                }

                Timber.tag("test").d("이걸 확인해야함"+campDataList.toString())
                tedNaverClustering = TedNaverClustering.with<MapModel>(requireContext(), naverMap!!)
                    .customMarker {
                        Marker().apply {
                            captionText = it.facltNm.toString()
                            captionRequestedWidth = 200
                            setCaptionAligns(Align.Top)
                            captionOffset = 10
                            captionTextSize = 18f
                        }
                    }
                    .markerClickListener {
                        val tag = it.induty
                        val loc = it.lctCl
                        val str = tag.joinToString(", ")+" · "+loc?.joinToString(" / ")
                        imgAdapter.clear()
                        binding.tvDialogtag.text = str
                        binding.tvDialogcampname.text = it.facltNm
                        binding.tvDialoglocation.text = it.addr1
                        binding.clMapBottomDialog.isGone=false

                        val list = listOf(it.firstImageUrl)
                        imgAdapter.submitList(list)
                    }
                    .clusterAddedListener { cluster, tedNaverMarker ->
                        tedNaverMarker.marker.tag
                    }
                    .minClusterSize(5)
                    .clusterBuckets(intArrayOf(50,50))
                    .items(campDataList)
                    .make()

            }
            .addOnFailureListener { exception ->
                Timber.tag("test").e(exception, "Error getting documents: ")
            }

    }






    fun onClickClusterMarker(donm: String?) {
        // 호출해야 할 데이터가 다른 ArrayList 에서 code 값이 서로 일치하는 데이터만 추출해야 한다.
        val itemData: MapModel = campDataList.filter { it.doNm.equals(donm) }.single()

    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView?.onDestroy()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

}
data class NaverItem(var position: LatLng) : TedClusterItem {
      override fun getTedLatLng(): TedLatLng {
            return TedLatLng(position.latitude, position.longitude)
          }

      var donm : String? = null
      constructor(lat: Double, lng: Double) : this(LatLng(lat, lng)) {
            donm = null
          }

      constructor(lat: Double, lng: Double, donm : String?) : this(
        LatLng(lat, lng)
      ) {
            this.donm = donm
          }
}