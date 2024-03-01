package com.brandon.campingmate.ui
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import com.brandon.campingmate.CampingModel
import com.brandon.campingmate.R
import com.brandon.campingmate.databinding.FragmentMapBinding
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.firestore
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.MarkerIcons
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ted.gun0912.clustering.clustering.TedClusterItem
import ted.gun0912.clustering.geometry.TedLatLng
import timber.log.Timber
import java.util.concurrent.Executors

class MapFragment : Fragment(),OnMapReadyCallback {
    private var _binding : FragmentMapBinding? = null
    private val binding  get() = _binding!!
    private var mapView: MapView? = null
    private var naverMap: NaverMap? = null
    private var maptype : Boolean = true
    private var context : Context? = null

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
    }

    override fun onMapReady(p0: NaverMap) {
        naverMap = p0
        val cameraPosition = CameraPosition(LatLng(37.413294,127.269311),10.0)
        naverMap?.cameraPosition = cameraPosition


        //백그라운드에서 불러온 마커가 저장되는 리스트
        var markers = mutableListOf<Marker>()

        val firebaseDatabase = FirebaseDatabase.getInstance()
        val db = Firebase.firestore

        val campsRef = db.collection("camps")

        campsRef
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // 각 문서에 대한 작업 수행
                    Timber.tag("test").d(document.data["facltNm"].toString())
                    val marker = Marker()
                    marker.icon = MarkerIcons.GREEN
                    marker.captionText = ""+document.data["facltNm"]
                    marker.captionRequestedWidth = 200
                    marker.setCaptionAligns(Align.Top)
                    marker.captionOffset = 10
                    marker.captionTextSize = 18f
                    marker.position = LatLng(document.data["mapY"].toString().toDouble() , document.data["mapX"].toString().toDouble())
                    marker.setOnClickListener {overlay ->
                        val tag = document.data["induty"] as List<*>
                        val loc = document.data["lctCl"] as List<*>
                        val str = tag.joinToString(", ")+" · "+loc.joinToString(" / ")
                        binding.tvDialogtag.text = str
                        binding.tvDialogcampname.text = document.data["facltNm"].toString()
                        binding.tvDialoglocation.text = document.data["addr1"].toString()
                        //binding.rvCampimg.adapter
                        binding.clMapBottomDialog.isGone=false

                        true
                    }
                    marker.map = naverMap
                }
            }
            .addOnFailureListener { exception ->
                Timber.tag("test").e(exception, "Error getting documents: ")
            }



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



    //백그라운드에서 불러온 마커를 저장하고 메인스레드에서 뿌려주는 메소드 . 작동하지 않아서 수정 필요
    private fun makeAllMarker() {

        CoroutineScope(Dispatchers.IO).launch {


            //백그라운드에서 불러온 마커가 저장되는 리스트
            var markers = mutableListOf<Marker>()

            val firebaseDatabase = FirebaseDatabase.getInstance()
            val db = Firebase.firestore

            val campsRef = db.collection("camps")

            campsRef
                .limit(10)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        // 각 문서에 대한 작업 수행
                        Timber.tag("test").d(document.data["facltNm"].toString())
                        val marker = Marker()
                        marker.icon = MarkerIcons.GREEN
                        marker.captionText = ""+document.data["facltNm"]
                        marker.captionRequestedWidth = 200
                        marker.setCaptionAligns(Align.Top)
                        marker.captionOffset = 10
                        marker.captionTextSize = 18f
                        marker.position = LatLng(document.data["mapY"].toString().toDouble() , document.data["mapX"].toString().toDouble())
                        marker.setOnClickListener {overlay ->
                            val tag = document.data["induty"] as List<*>
                            val loc = document.data["lctCl"] as List<*>
                            val str = tag.joinToString(", ")+" · "+loc.joinToString(" / ")
                            binding.tvDialogtag.text = str
                            binding.tvDialogcampname.text = document.data["facltNm"].toString()
                            binding.tvDialoglocation.text = document.data["addr1"].toString()
                            //binding.rvCampimg.adapter
                            binding.clMapBottomDialog.isGone=false
                            true
                        }
                        markers?.add(marker)
                    }
                }
                .addOnFailureListener { exception ->
                    Timber.tag("test").e(exception, "Error getting documents: ")
                }
            withContext(Dispatchers.Main) {
                // 메인 스레드
                    markers.forEach { marker ->
                        marker.map = naverMap
                    }
                }

        }
    }

}


data class NaverItem(var position: LatLng) : TedClusterItem {
      override fun getTedLatLng(): TedLatLng {
            return TedLatLng(position.latitude, position.longitude)
          }
     
      var title: String? = null
      constructor(lat: Double, lng: Double) : this(LatLng(lat, lng)) {
            title = null
          }
     
      constructor(lat: Double, lng: Double, title: String?) : this(
        LatLng(lat, lng)
      ) {
            this.title = title
          }
}








