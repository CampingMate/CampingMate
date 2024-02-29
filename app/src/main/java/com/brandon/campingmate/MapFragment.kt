package com.brandon.campingmate

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.MarkerIcons

class MapFragment : Fragment(),OnMapReadyCallback {
    private var _binding : FragmentMapBinding? = null
    private val binding  get() = _binding!!
    private var mapView: MapView? = null
    private var naverMap: NaverMap? = null
    private var maptype : Boolean = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        binding.btnRoad.setOnClickListener {

        }
    }

    override fun onMapReady(p0: NaverMap) {
        naverMap = p0
        val cameraPosition = CameraPosition(LatLng(37.413294,127.269311),10.0)
        naverMap?.cameraPosition = cameraPosition

        val firebaseDatabase = FirebaseDatabase.getInstance()
        val db = Firebase.firestore

        val campsRef = db.collection("camps")

//            campsRef.whereEqualTo("induty", "글램핑")
        campsRef
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // 각 문서에 대한 작업 수행
                    val marker = Marker()
                    marker.icon = MarkerIcons.GREEN
                    marker.captionText = ""+document.data["facltNm"]
                    marker.captionRequestedWidth = 200
                    marker.setCaptionAligns(Align.Top)
                    marker.captionOffset = 10
                    marker.captionTextSize = 18f
                    marker.position = LatLng(document.data["mapY"].toString().toDouble() , document.data["mapX"].toString().toDouble())
                    marker.setOnClickListener {overlay ->

                        true
                    }
                    marker.map = naverMap
                }
            }
            .addOnFailureListener { exception ->
                Log.e("test", "Error getting documents: ", exception)
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
}