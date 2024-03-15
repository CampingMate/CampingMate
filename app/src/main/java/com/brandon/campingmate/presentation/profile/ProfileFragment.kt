package com.brandon.campingmate.presentation.profile

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.brandon.campingmate.R
import com.brandon.campingmate.databinding.FragmentProfileBinding
import com.brandon.campingmate.presentation.login.LoginActivity
import com.brandon.campingmate.presentation.profile.adapter.ProfileBookmarkAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.sdk.user.UserApiClient

class ProfileFragment : Fragment() {

    private lateinit var imageLauncher: ActivityResultLauncher<Intent>
    private var _binding: FragmentProfileBinding? = null
    private var img_URI: Uri? = null
    private val binding get() = _binding!!
    private val adapter: ProfileBookmarkAdapter by lazy { ProfileBookmarkAdapter() }
    private val viewModel by lazy { ViewModelProvider(this)[ProfileViewModel::class.java] }
    val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

//    override fun onStart() {
//        super.onStart()
//        checkLogin()
//    }
    override fun onResume() {
        super.onResume()
        checkLogin()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //checkLogin()

        clickWritingTab()
        clickBookmarkedTab()

        clickLogin()

        clickEditListener()
        clickEditProfile()

        clickLogout()

    }

    fun checkLogin() {
        UserApiClient.instance.me { user, error ->
            if (user?.id != null) {
                initLogin()
                setBookmarkedAdapter(user.id.toString())
            } else initLogout()
        }

    }

    private fun setBookmarkedAdapter(userId : String) = with(binding) {
        rvBookmarked.adapter = adapter
        rvBookmarked.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        viewModel.getBookmark(userId)
        viewModel.bookmarkedList.observe(viewLifecycleOwner){
            adapter.submitList(it)
            if(it.isNotEmpty()) {
                tvBookmarkedSize.visibility =View.VISIBLE
                tvTabBookmarked.visibility =View.GONE
                rvBookmarked.visibility = View.VISIBLE
                tvBookmarkedSize.text = it.size.toString()
            } else{
                tvTabBookmarked.visibility =View.VISIBLE
                rvBookmarked.visibility = View.GONE
                tvBookmarkedSize.text = it.size.toString()
            }
        }
    }


    private fun initLogout() {
        with(binding) {
            //화면 상에서 비로그인 화면으로 되돌리기
            ivProfileImg.setImageResource(R.drawable.ic_camp)
            ivProfileImg.visibility = View.VISIBLE
            tvProfileName.textSize = 20f
            tvProfileName.text = getString(R.string.profile_login_text)
            tvProfileName.visibility = View.VISIBLE
            tvProfileEmail.visibility = View.GONE
            btnLogout.visibility = View.INVISIBLE
            btnGoLogin.visibility = View.VISIBLE
            btnProfileEdit.visibility = View.GONE
            tvTabLoginText.visibility = View.VISIBLE
            lineBookmarked.visibility = View.VISIBLE
            tvTabBookmarked.visibility = View.GONE
            tvTabWriting.visibility = View.GONE
            tvBookmarkedSize.visibility = View.GONE
            tvBookmarkedSize.text = "0"
            rvBookmarked.visibility = View.GONE
            tvWritingSize.text="0"

        }
    }

    fun initLogin() {
        with(binding) {
            UserApiClient.instance.me { user, error ->
                val docRef = db.collection("users").document("Kakao${user?.id}")
                docRef.get().addOnSuccessListener {
                    if (!it.exists()) {
                        ivProfileImg.setImageURI(Uri.parse(user?.kakaoAccount?.profile?.profileImageUrl))
                        tvProfileName.textSize = 24f
                        tvProfileName.text = user?.kakaoAccount?.profile?.nickname
                        tvProfileEmail.text = user?.kakaoAccount?.email
                    } else {
                        if (it.getString("profileImage") != null) {
                            //resolveUri failed on bad bitmap uri:
                            ivProfileImg.setImageURI(Uri.parse(it.getString("profileImage")))
                        }
                        tvProfileName.textSize = 24f
                        tvProfileName.text = it.getString("nickName").toString()
                        tvProfileEmail.text = it.getString("userEmail").toString()

                    }
                }
            }

            tvProfileName.visibility = View.VISIBLE
            tvProfileEmail.visibility = View.VISIBLE

            btnLogout.visibility = View.VISIBLE

            btnGoLogin.visibility = View.GONE
            btnProfileEdit.visibility = View.VISIBLE

            tvTabLoginText.visibility = View.GONE
            tvTabBookmarked.visibility = View.VISIBLE
            lineBookmarked.visibility = View.VISIBLE
            lineWriting.visibility = View.INVISIBLE

            //Todo. 북마크사이즈 부여 및 리사이클러뷰아이템가져오기 + 작성글사이즈 부여 및 리사이클러뷰아이템가져오기

        }
    }

    private fun clickLogin() {
        binding.btnGoLogin.setOnClickListener {
            //로그인 페이지로 이동
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        }
    }


    private fun clickEditProfile() {
        with(binding) {
            btnProfileEdit.setOnClickListener {
                llEditConfirm.visibility = View.VISIBLE
                btnEditName.visibility = View.VISIBLE
                btnEditImg.visibility = View.VISIBLE
                btnLogout.visibility = View.GONE
                btnProfileEdit.visibility = View.GONE
            }

            clickEditName()
            //clickEditImg()
        }
    }

    private fun clickEditName() {
        with(binding) {
            btnEditName.setOnClickListener {

                val builder = AlertDialog.Builder(requireContext())
                val editUserNameDialog = layoutInflater.inflate(R.layout.dialog_edit_user_name, null)
                builder.setView(editUserNameDialog)
                val dialog = builder.create()

                //다이얼로그 영역(기본값 화이트) 투명화로 둥근 테두리가 묻히지 않고 보이도록 설정
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                dialog.show()

                val layoutParams = WindowManager.LayoutParams().apply {
                    // 다이얼로그의 크기를 화면에 꽉 차게 조절
                    copyFrom(dialog.window?.attributes)
                    width = WindowManager.LayoutParams.MATCH_PARENT
                    height = WindowManager.LayoutParams.MATCH_PARENT
                    //화면 투명도 설정 (투명0~선명1)
                    dimAmount = 0.9f
                }
                dialog.window?.attributes = layoutParams

                val etEditUserName = dialog.findViewById<EditText>(R.id.et_edit_user_name)
                etEditUserName.setText(tvProfileName.text.toString())
                dialog.findViewById<TextView>(R.id.tv_current_length).text = etEditUserName.length().toString()

                //키보드 자동활성화
                etEditUserName.requestFocus()
                val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

                etEditUserName.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        //입력 전 호출 메서드 (입력 하여 변화가 생기기 전)
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        //입력 중 호출 메서드 (변화와 동시에 처리)
                        dialog.findViewById<TextView>(R.id.tv_current_length).text = etEditUserName.length().toString()
                    }

                    override fun afterTextChanged(p0: Editable?) {
                        //입력 후 호출 메서드 (입력이 끝났을 때 처리)
                    }

                })

                with(dialog) {
                    findViewById<ImageView>(R.id.btn_clear_name).setOnClickListener {
                        etEditUserName.text.clear()
                    }

                    findViewById<TextView>(R.id.btn_edit_name_cancel).setOnClickListener {
                        //화면 취소하면 키보드 없애주기
                        inputMethodManager.hideSoftInputFromWindow(dialog.window?.decorView?.windowToken, 0)
                        dialog.dismiss()
                    }

                    findViewById<TextView>(R.id.btn_edit_name_confirm).setOnClickListener {
                        tvProfileName.text = etEditUserName.text.toString().replace("\n", "")
                        inputMethodManager.hideSoftInputFromWindow(dialog.window?.decorView?.windowToken, 0)
                        dialog.dismiss()
                    }
                }
            }
        }
    }

//    private fun clickEditImg() {
//        with(binding) {
//            btnEditImg.setOnClickListener {
//                val intent = Intent(Intent.ACTION_PICK)
//                intent.type = "image/*"
//                imageLauncher.launch(intent)
//            }
//        }
//
//        imageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                img_URI = result.data?.data
//                binding.ivProfileImg.scaleType = ImageView.ScaleType.CENTER_CROP
//                binding.ivProfileImg.setImageURI(img_URI)
//            }
//        }
//    }

    private fun clickEditListener() {
        with(binding) {
            btnEditConfirm.setOnClickListener { handleClickEdit(true) }
            btnEditCancel.setOnClickListener { handleClickEdit(false) }
        }
    }

    private fun handleClickEdit(confirm: Boolean) {
        with(binding) {
            if (confirm) {
                //Todo. 사진에 대한 변경값을 다시 스토리지를 통헤 데이터베이스에 넘겨서 저장
                UserApiClient.instance.me { user, error ->
                    val documentRef = db.collection("users").document("Kakao${user?.id}")
                    val updateNickname = hashMapOf<String, Any>("nickName" to "${tvProfileName.text}")
                    //val updateProfileImg = hashMapOf<String,Any>("profileImage" to img_URI.toString())
                    documentRef.get().addOnSuccessListener {
                        documentRef.update(updateNickname)
                        //documentRef.update(updateProfileImg)
                    }
                }
                tvProfileName.text = tvProfileName.text
                //ivProfileImg.setImageURI(img_URI)
            } else {
                tvProfileName.text = ""
                initLogin()
            }

            btnEditName.visibility = View.GONE
            btnEditImg.visibility = View.GONE
            btnLogout.visibility = View.VISIBLE
            btnProfileEdit.visibility = View.VISIBLE
            llEditConfirm.visibility = View.INVISIBLE
            tvProfileName.visibility = View.VISIBLE
            if(rvBookmarked.visibility == View.VISIBLE){
                tvTabBookmarked.visibility = View.GONE
            }
        }

    }

    private fun clickBookmarkedTab() {
        with(binding) {
            tabBookmarked.setOnClickListener {
                lineBookmarked.visibility = View.VISIBLE
                lineWriting.visibility = View.INVISIBLE

                //if 가져올 데이터가 없으면
                if(tvBookmarkedSize.text.toString().toInt() >0) {
                    rvBookmarked.visibility = View.VISIBLE
                    tvTabLoginText.visibility = View.GONE
                    tvTabWriting.visibility = View.GONE
                } else {
                    tvTabLoginText.visibility = View.GONE
                    tvTabBookmarked.visibility = View.VISIBLE
                    tvTabWriting.visibility = View.GONE
                }
            }
        }
    }

    private fun clickWritingTab() {
        with(binding) {
            tabWriting.setOnClickListener {
                lineBookmarked.visibility = View.INVISIBLE
                lineWriting.visibility = View.VISIBLE

                //if 가져올 데이터가 없으면
                tvTabLoginText.visibility = View.GONE
                tvTabBookmarked.visibility = View.GONE
                tvTabWriting.visibility = View.VISIBLE
                rvBookmarked.visibility = View.GONE
                //todo.if(가져올데이터가있으면) size.setText + 리사이클러뷰 어댑터 수행 + 스와이프 아이템 삭제(스낵바undo)
                //로그인 후 사용해주세요 텍스트 어떻게 되는지 확인하고 아마 View.Gone해줘야할듯
            }
        }

    }


    private fun clickLogout() {
        with(binding) {
            btnLogout.setOnClickListener {
                val builder = AlertDialog.Builder(requireContext())
                val logoutDialog = layoutInflater.inflate(R.layout.dialog_logout, null)

                builder.setView(logoutDialog)
                val dialog = builder.create()

                //다이얼로그 영역(기본값 화이트) 투명화로 둥근 테두리가 묻히지 않고 보이도록 설정
                dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                dialog.show()

                dialog.findViewById<TextView>(R.id.btn_logout_cancel)?.setOnClickListener {
                    dialog.dismiss()
                }

                dialog.findViewById<TextView>(R.id.btn_logout_comfirm)?.setOnClickListener {
                    Toast.makeText(requireContext(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
                    //By.sounghyun
                    UserApiClient.instance.logout { error ->
                        if (error != null) {
                            Toast.makeText(requireContext(), "로그아웃 실패 $error", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "로그아웃 성공", Toast.LENGTH_SHORT).show()
                        }
                    }
//                    UserApiClient.instance.unlink { error ->
//                        if (error != null) {
//                            Toast.makeText(requireContext(), "회원 탈퇴 실패 $error", Toast.LENGTH_SHORT).show()
//                        }else {
//                            Toast.makeText(requireContext(), "회원 탈퇴 성공", Toast.LENGTH_SHORT).show()
//                        }
//                    }

                    //화면 상에서 비로그인 화면으로 되돌리기
                    initLogout()
                    dialog.dismiss()
                }
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}