package com.brandon.campingmate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.brandon.campingmate.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clickWritingTab()
        clickBookmarkedTab()

        clickLogin()
        clickEditProfile()
        clickEditCancel()

//자동로그인 유지 세션 확인해서 적용
//        if(uuid != null){
//            initLogin()
//        }

    }

    private fun initLogin() {
        with(binding) {
            tvProfileName.textSize = 24f
            tvProfileName.text = "김수미"

            tvProfileEmail.visibility = View.VISIBLE
            btnLogout.visibility = View.VISIBLE

            btnGoLogin.visibility = View.GONE
            btnProfileEdit.visibility = View.VISIBLE

            //Todo. 북마크사이즈 부여 및 리사이클러뷰아이템가져오기 + 작성글사이즈 부여 및 리사이클러뷰아이템가져오기

        }
    }

    private fun clickLogin() {
        binding.btnGoLogin.setOnClickListener {
            //todo.로그인 페이지로 이동
            initLogin()
        }
    }


    private fun clickEditProfile() {
        with(binding) {
            btnProfileEdit.setOnClickListener {
                llEditConfirm.visibility = View.VISIBLE
                btnEditName.visibility = View.VISIBLE //todo.이름 수정 제한 10자 넘어가는 프래그먼트 생성 (생성 시에 기존 유저네임이 기본값)
                btnEditImg.visibility = View.VISIBLE //todo.사진고르는 인텐트로 넘어가기, 사진 찍을건지?인텐트.. 연결..?
                btnLogout.visibility = View.GONE
                btnProfileEdit.visibility = View.INVISIBLE
            }
        }
    }

    private fun clickEditComfirm() {
        //Todo. 이름 사진은 변경값을 다시 데이터베이스에 저장도 해줘야함.
        with(binding){
            btnEditConfirm.setOnClickListener {
                tvProfileName.text = etProfileName.text.toString()

            }
        }
    }

    private fun clickEditCancel() {
        with(binding) {
            btnEditCancel.setOnClickListener {
                btnEditName.visibility = View.GONE
                btnEditImg.visibility = View.GONE
                btnLogout.visibility = View.VISIBLE
                btnProfileEdit.visibility = View.VISIBLE
                llEditConfirm.visibility = View.INVISIBLE
            }
        }
    }



    private fun clickBookmarkedTab() {
        binding.tabBookmarked.setOnClickListener {
            binding.lineBookmarked.visibility = View.VISIBLE
            binding.lineWriting.visibility = View.INVISIBLE

            //todo.if(가져올데이터가있으면) size.setText + 리사이클러뷰 어댑터 수행 + 스와이프 아이템 삭제(스낵바undo)
            //로그인 후 사용해주세요 텍스트 어떻게 되는지 확인하고 아마 View.Gone해줘야할듯
        }
    }

    private fun clickWritingTab() {
        binding.tabWriting.setOnClickListener {
            binding.lineBookmarked.visibility = View.INVISIBLE
            binding.lineWriting.visibility = View.VISIBLE

            //todo.if(가져올데이터가있으면) size.setText + 리사이클러뷰 어댑터 수행 + 스와이프 아이템 삭제(스낵바undo)
            //로그인 후 사용해주세요 텍스트 어떻게 되는지 확인하고 아마 View.Gone해줘야할듯
        }
    }


    private fun clickLogout() {
        binding.btnLogout.setOnClickListener {
            //todo. 로그아웃 다이얼로그 연결
            //취소-뒤로가기 로그아웃-로그아웃절차수행
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}