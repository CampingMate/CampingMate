package com.brandon.campingmate.presentation.map

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.brandon.campingmate.presentation.map.adapter.DialogImgAdapter
import com.brandon.campingmate.domain.model.LocationBasedListItem
import com.brandon.campingmate.databinding.FragmentMapBinding
import com.brandon.campingmate.domain.model.CampEntity
import com.brandon.campingmate.domain.model.NaverItem
import com.brandon.campingmate.presentation.campdetail.CampDetailActivity
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Align
import com.naver.maps.map.overlay.Marker
import kotlinx.coroutines.launch
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
    private lateinit var tedNaverClustering: TedNaverClustering<LocationBasedListItem>
    private var campDataList = mutableListOf<LocationBasedListItem>()
    private var naverItems = mutableListOf<NaverItem>()

    private val viewModel by lazy {
        ViewModelProvider(this)[MapViewModel::class.java]
    }

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
        btnSattel.setOnClickListener {
            if(maptype){
                naverMap?.mapType = NaverMap.MapType.Satellite
                maptype=false
                btnSattel.text = "지형도"
            }else{
                naverMap?.mapType = NaverMap.MapType.Terrain
                maptype=true
                btnSattel.text = "위성"
            }
        }

        ivDialogclose.setOnClickListener {
            clMapBottomDialog.isGone = true
        }

        tvDialogcampname.setOnClickListener {
            //캠프디테일로 이동
        }

        button.setOnClickListener {
            val intent = Intent(context, WebViewActivity::class.java)
            startActivity(intent)
        }
        //makeAllMarker()
        rvCampImg.adapter = imgAdapter

    }

    private fun initViewModel() = with(viewModel){
        paramHashmap.observe(viewLifecycleOwner){
            //getCampList(it)
        }
        campList.observe(viewLifecycleOwner){
            if (campList.value?.isNotEmpty() == true) {
                campDataList = viewModel.campList.value!!

                tedNaverClustering = TedNaverClustering.with<LocationBasedListItem>(requireContext(), naverMap!!)
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
                        imgAdapter.clear()
                        binding.tvDialogcampname.setOnClickListener(null)
                        binding.tvDialogtag.text = "$tag · $loc"
                        binding.tvDialogcampname.text = it.facltNm
                        binding.tvDialoglocation.text = it.addr1
                        binding.clMapBottomDialog.isGone=false
                        binding.tvDialogcampname.setOnClickListener { view ->
                            val intent = Intent(requireContext(),CampDetailActivity::class.java)
                            var data = CampEntity(
                                firstImageUrl = it.firstImageUrl,
                                siteMg3Vrticl = it.siteMg3Vrticl,
                                siteMg2Vrticl = it.siteMg2Vrticl,
                                siteMg1Co = it.siteMg1Co,
                                siteMg2Co = it.siteMg2Co,
                                siteMg3Co = it.siteMg3Co,
                                siteBottomCl1 = it.siteBottomCl1,
                                siteBottomCl2 = it.siteBottomCl2,
                                siteBottomCl3 = it.siteBottomCl3,
                                siteBottomCl4 = it.siteBottomCl4,
                                fireSensorCo = it.fireSensorCo,
                                themaEnvrnCl = it.themaEnvrnCl?.split(","),
                                eqpmnLendCl = it.eqpmnLendCl?.split(","),
                                animalCmgCl = it.animalCmgCl,
                                tooltip = it.tooltip,
                                glampInnerFclty = it.glampInnerFclty?.split(","),
                                caravInnerFclty = it.caravInnerFclty?.split(","),
                                prmisnDe = it.prmisnDe,
                                operPdCl = it.operPdCl,
                                operDeCl = it.operDeCl,
                                trlerAcmpnyAt = it.trlerAcmpnyAt,
                                caravAcmpnyAt = it.caravAcmpnyAt,
                                toiletCo = it.toiletCo,
                                frprvtWrppCo = it.frprvtWrppCo,
                                frprvtSandCo = it.frprvtSandCo,
                                induty = it.induty?.split(","),
                                siteMg1Vrticl = it.siteMg1Vrticl,
                                posblFcltyEtc = it.posblFcltyEtc,
                                clturEventAt = it.clturEventAt,
                                clturEvent = it.clturEvent,
                                exprnProgrmAt = it.exprnProgrmAt,
                                exprnProgrm = it.exprnProgrm,
                                extshrCo = it.extshrCo,
                                manageSttus = it.manageSttus,
                                hvofBgnde = it.hvofBgnde,
                                hvofEnddle = it.hvofEnddle,
                                trsagntNo = it.trsagntNo,
                                bizrno = it.bizrno,
                                facltDivNm = it.facltDivNm,
                                mangeDivNm = it.mangeDivNm,
                                mgcDiv = it.mgcDiv,
                                tourEraCl = it.tourEraCl,
                                lctCl = it.lctCl?.split(","),
                                doNm = it.doNm,
                                sigunguNm = it.sigunguNm,
                                zipcode = it.zipcode,
                                addr1 = it.addr1,
                                addr2 = it.addr2,
                                mapX = it.mapX,
                                mapY = it.mapY,
                                direction = it.direction,
                                tel = it.tel,
                                homepage = it.homepage,
                                contentId = it.contentId,
                                swrmCo = it.swrmCo,
                                wtrplCo = it.wtrplCo,
                                brazierCl = it.brazierCl,
                                sbrsCl = it.sbrsCl?.split(","),
                                sbrsEtc = it.sbrsEtc,
                                modifiedtime = it.modifiedtime,
                                facltNm = it.facltNm,
                                lineIntro = it.lineIntro,
                                intro = it.intro,
                                allar = it.allar,
                                insrncAt = it.insrncAt,
                                resveUrl = it.resveUrl,
                                resveCl = it.resveCl,
                                manageNmpr = it.manageNmpr,
                                gnrlSiteCo = it.gnrlSiteCo,
                                autoSiteCo = it.autoSiteCo,
                                glampSiteCo = it.glampSiteCo,
                                caravSiteCo = it.caravSiteCo,
                                indvdlCaravSiteCo = it.indvdlCaravSiteCo,
                                sitedStnc = it.sitedStnc,
                                siteMg1Width = it.siteMg1Width,
                                siteMg2Width = it.siteMg2Width,
                                siteMg3Width = it.siteMg3Width,
                                createdtime = it.createdtime,
                                posblFcltyCl = it.posblFcltyCl?.split(","),
                                featureNm = it.featureNm,
                                siteBottomCl5 = it.siteBottomCl5
                            )
                            intent.putExtra("campData",data)
                            startActivity(intent)
                        }

                        val list = listOf(it.firstImageUrl)
                        imgAdapter.submitList(list)
                    }
                    .minClusterSize(10)
                    .clusterBuckets(intArrayOf(50,50))
                    .items(campDataList)
                    .make()
            }
        }
    }

    
    override fun onMapReady(p0: NaverMap) {
        naverMap = p0
        val cameraPosition = CameraPosition(LatLng(36.60545, 127.9792), 6.0)
        naverMap?.cameraPosition = cameraPosition

        val map = viewModel.getBlParamHashmap()
        viewModel.getAllCampList(map)

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

}
