package com.brandon.campingmate.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.brandon.campingmate.R
import com.brandon.campingmate.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private lateinit var imageLauncher: ActivityResultLauncher<Intent>
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//자동로그인 유지 세션 확인해서 적용
//        if(uuid != null){
//            initLogin()
//        }

        clickWritingTab()
        clickBookmarkedTab()

        clickLogin()
        
        clickEditListener()
        clickEditProfile()
        clickEditImg()


        clickLogout()

    }

    private fun initLogin() {
        with(binding) {

            //todo.사진설정해주기 (if문 작성으로 저장값이 있으면 불러오기)

            tvProfileName.textSize = 24f
            tvProfileName.text = "김수미"//DB UserName

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

    private fun clickEditName() {
        with(binding) {
            btnEditName.setOnClickListener {
                tvProfileName.text = etProfileName.text.toString()
            }
        }
    }

    private fun clickEditImg() {
        with(binding) {
            btnEditImg.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                imageLauncher.launch(intent)
            }
        }

        imageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val img_URI = result.data?.data
                binding.ivProfileImg.scaleType = ImageView.ScaleType.CENTER_CROP
                binding.ivProfileImg.setImageURI(img_URI)
            }
        }
    }

    private fun clickEditListener() {
        with(binding) {
            btnEditConfirm.setOnClickListener { handleClickEdit(true) }
            btnEditCancel.setOnClickListener { handleClickEdit(false) }
        }
    }

    private fun handleClickEdit(confirm: Boolean) {
        with(binding) {
            if (confirm) {
                //Todo. 이름, 사진은 변경값을 다시 데이터베이스에 넘겨서 저장
                Toast.makeText(requireContext(), "데이터베이스로 저장!", Toast.LENGTH_SHORT).show()
            }

            initLogin()
            btnEditName.visibility = View.GONE
            btnEditImg.visibility = View.GONE
            llEditConfirm.visibility = View.INVISIBLE
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
        with(binding) {
            btnLogout.setOnClickListener {

                val logoutDialog = layoutInflater.inflate(R.layout.dialog_logout, null)

                val dialog = AlertDialog.Builder(requireContext())
                    .setView(logoutDialog)
                    .create()

                //다이얼로그 영역(기본값 화이트) 투명화로 둥근 테두리가 묻히지 않고 보이도록 설정
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                dialog.show()

                dialog.findViewById<TextView>(R.id.btn_logout_cancel)?.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.findViewById<TextView>(R.id.btn_logout_comfirm)?.setOnClickListener {
                    //todo. 실제 로그아웃 절차 수행 <- 수행시 토스트 삭제!
                    Toast.makeText(requireContext(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()

                    //화면 상에서 비로그인 화면으로 되돌리기
                    ivProfileImg.setImageResource(R.drawable.ic_camp)
                    tvProfileName.textSize = 20f
                    tvProfileName.text = getString(R.string.profile_login_text)
                    tvProfileEmail.visibility = View.GONE
                    btnLogout.visibility = View.INVISIBLE
                    btnGoLogin.visibility = View.VISIBLE
                    btnProfileEdit.visibility = View.GONE


                    dialog.dismiss()
                }
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}