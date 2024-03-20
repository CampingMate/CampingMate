package com.brandon.campingmate.presentation.profile

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brandon.campingmate.R
import com.brandon.campingmate.data.local.preferences.EncryptedPrefs
import com.brandon.campingmate.databinding.FragmentProfileBinding
import com.brandon.campingmate.domain.model.CampEntity
import com.brandon.campingmate.presentation.login.LoginActivity
import com.brandon.campingmate.presentation.profile.adapter.ProfileBookmarkAdapter
import com.brandon.campingmate.presentation.profile.adapter.ProfilePostAdapter
import com.brandon.campingmate.utils.profileImgUpload
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage
import com.kakao.sdk.user.UserApiClient
import timber.log.Timber

class ProfileFragment : Fragment() {

    private lateinit var imageLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private var _binding: FragmentProfileBinding? = null
    private var profileImgUri: Uri? = null
    private val binding get() = _binding!!
    private val viewModel by lazy { ViewModelProvider(this)[ProfileViewModel::class.java] }
    private val bookmarkAdapter: ProfileBookmarkAdapter by lazy { ProfileBookmarkAdapter() }
    private val postAdapter: ProfilePostAdapter by lazy { ProfilePostAdapter() }
    private val db = FirebaseFirestore.getInstance()
    var userId: String? = EncryptedPrefs.getMyId()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        userId = EncryptedPrefs.getMyId()
        checkLogin()
    }
//    override fun onResume() {
//        super.onResume()
//        checkLogin()
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //checkLogin()

        clickLogin()

        clickWritingTab()
        clickBookmarkedTab()

        clickEditListener()
        clickEditProfile()
        initActivityResultContracts()

        swipeRecyclerView(binding.rvBookmarked)
        swipeRecyclerView(binding.rvWriting)

        clickLogout()

    }

    private fun checkLogin() {
        if (userId != null) {
            initLogin()
            setBookmarkedAdapter(userId!!)
            setPostAdapter(userId!!)
        } else initLogout()
    }

    private fun setBookmarkedAdapter(userId: String) = with(binding) {
        rvBookmarked.adapter = bookmarkAdapter
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        rvBookmarked.layoutManager = layoutManager
        viewModel.getBookmark(userId)
        viewModel.bookmarkedList.observe(viewLifecycleOwner) {
            val newList = mutableListOf<CampEntity>()
            newList.addAll(it)
            bookmarkAdapter.submitList(newList)
            if (it.isNotEmpty()) {
                tvBookmarkedSize.text = it.size.toString()
                tvBookmarkedSize.visibility = View.VISIBLE
                tvTabBookmarked.visibility = View.GONE
                rvBookmarked.visibility = View.VISIBLE
            } else {
                tvBookmarkedSize.text = it.size.toString()
                tvTabBookmarked.visibility = View.VISIBLE
                rvBookmarked.visibility = View.GONE
            }
            layoutManager.scrollToPosition(it.size - 1)
        }
    }

    private fun setPostAdapter(userId: String) = with(binding) {
        rvWriting.adapter = postAdapter
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        rvWriting.layoutManager = layoutManager
        viewModel.getPosts(userId)
        viewModel.postList.observe(viewLifecycleOwner) {
            postAdapter.submitList(it.toList())
            if (it.isNotEmpty()) {
                tvWritingSize.text = it.size.toString()
                tvWritingSize.visibility = View.VISIBLE
            } else {
                tvWritingSize.text = it.size.toString()
                if (lineWriting.visibility == View.VISIBLE) {
                    tvTabWriting.visibility = View.VISIBLE
                }
            }
            layoutManager.scrollToPosition(it.size - 1)
        }
    }

    private fun initLogin() {
        with(binding) {
            val docRef = db.collection("users").document(userId.toString())
            docRef.get().addOnSuccessListener {
                if (!it.exists()) {
                    UserApiClient.instance.me { user, error ->
                        ivProfileImg.setImageURI(Uri.parse(user?.kakaoAccount?.profile?.profileImageUrl))
                        tvProfileName.textSize = 24f
                        tvProfileName.text = user?.kakaoAccount?.profile?.nickname
                        tvProfileEmail.text = user?.kakaoAccount?.email
                    }
                } else {
                    if (profileImgUri == null) {
                        ivProfileImg.scaleType = ImageView.ScaleType.CENTER_CROP
                        Glide.with(requireContext()).load(it.getString("profileImage")).into(ivProfileImg)
                        ivProfileImg.visibility = View.VISIBLE
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
            tvTabWriting.visibility = View.GONE
            rvWriting.visibility = View.GONE
        }
    }

    private fun initLogout() {
        with(binding) {
            //화면 상에서 비로그인 화면으로 되돌리기
            ivProfileImg.scaleType = ImageView.ScaleType.CENTER_INSIDE
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
            tvWritingSize.visibility = View.GONE
            tvWritingSize.text = "0"
            rvWriting.visibility = View.GONE

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
            clickEditImg()
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

    private fun initActivityResultContracts() {
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getImg()
            } else {
                val builder = AlertDialog.Builder(requireContext())
                builder.setMessage("프로필 이미지 수정을 하시려면\n파일 및 미디어 권한을 허용해주세요")
                    .setPositiveButton("확인", DialogInterface.OnClickListener { _, _ ->
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + requireActivity().packageName))
                        startActivity(intent)
                    })
                    .setNegativeButton("취소", null)
                builder.show()
            }
        }

        imageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                profileImgUri = result.data?.data
                binding.ivProfileImg.scaleType = ImageView.ScaleType.CENTER_CROP
                Glide.with(requireContext()).load(profileImgUri).into(binding.ivProfileImg)
            }
        }
    }

    private fun checkPermissionVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermission(android.Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            requestPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun requestPermission(permission: String) {
        if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(permission)
        } else {
            getImg()
        }
    }

    private fun clickEditImg() {
        with(binding) {
            btnEditImg.setOnClickListener {
                checkPermissionVersion()
            }
        }
    }

    private fun getImg() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imageLauncher.launch(intent)
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
                val documentRef = db.collection("users").document(userId.toString())
                val updateNickname = hashMapOf<String, Any>("nickName" to "${tvProfileName.text}")
                if (profileImgUri != null) {
                    profileImgUpload(profileImgUri!!, userId.toString())
                    Firebase.storage.getReference("profileImage").child(userId.toString()).downloadUrl.addOnCompleteListener {
                        if (it.isSuccessful) {
                            val profileImgURI = hashMapOf<String, Any>("profileImage" to it.result.toString())
                            documentRef.update(profileImgURI)
                        }
                    }
                    //profileImgUri = null
                }
                documentRef.get().addOnSuccessListener {
                    documentRef.update(updateNickname)
                }
                tvProfileName.text = tvProfileName.text
            } else {
                profileImgUri = null
                tvProfileName.text = ""
                ivProfileImg.setImageURI(profileImgUri)
                initLogin()
            }

            btnEditName.visibility = View.GONE
            btnEditImg.visibility = View.GONE
            btnLogout.visibility = View.VISIBLE
            btnProfileEdit.visibility = View.VISIBLE
            llEditConfirm.visibility = View.INVISIBLE
            tvProfileName.visibility = View.VISIBLE
            if (rvBookmarked.visibility == View.VISIBLE) {
                tvTabBookmarked.visibility = View.GONE
            }
        }

    }

    private fun clickBookmarkedTab() {
        with(binding) {
            tabBookmarked.setOnClickListener {
                lineBookmarked.visibility = View.VISIBLE
                lineWriting.visibility = View.INVISIBLE

                if (tvBookmarkedSize.text.toString().toInt() > 0) {
                    rvWriting.visibility = View.GONE
                    rvBookmarked.visibility = View.VISIBLE
                    tvTabLoginText.visibility = View.GONE
                    tvTabBookmarked.visibility = View.GONE
                    tvTabWriting.visibility = View.GONE
                } else {
                    tvTabLoginText.visibility = View.GONE
                    tvTabBookmarked.visibility = View.VISIBLE
                    rvWriting.visibility = View.GONE
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

                if (postAdapter.itemCount > 0) {
                    rvBookmarked.visibility = View.GONE
                    rvWriting.visibility = View.VISIBLE
                    tvTabLoginText.visibility = View.GONE
                    tvTabBookmarked.visibility = View.GONE
                    tvTabWriting.visibility = View.GONE
                } else {
                    rvBookmarked.visibility = View.GONE
                    tvTabLoginText.visibility = View.GONE
                    tvTabBookmarked.visibility = View.GONE
                    tvTabWriting.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun swipeRecyclerView(recyclerView: RecyclerView) {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                when (recyclerView) {
                    binding.rvBookmarked -> {
                        val bookmarkID = bookmarkAdapter.currentList[position]
                        viewModel.removeBookmarkCamp(userId.toString(), bookmarkID.contentId.toString())
                        val undoSnackbar = Snackbar.make(binding.root, "해당 북마크를 삭제했습니다.", 5000)
                        undoSnackbar.setAction("되돌리기") {
                            viewModel.undoBookmarkCamp(userId.toString())
                        }
                        undoSnackbar.show()
                    }

                    binding.rvWriting -> {
                        val postID = postAdapter.currentList[position]
                        viewModel.removePostAdapter(postID.postId.toString())
                        val undoSnackbar = Snackbar.make(binding.root, "해당 작성 글을 삭제했습니다.", 5000)
                        undoSnackbar.setAction("되돌리기") {
                            viewModel.undoPost()
                        }
                        undoSnackbar.show()
                        val snackbarCallBack = object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                super.onDismissed(transientBottomBar, event)
                                if (event != DISMISS_EVENT_ACTION) {
                                    viewModel.removePostDB()
                                }
                            }
                        }
                        undoSnackbar.addCallback(snackbarCallBack)
                    }
                }

            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                val icon: Bitmap
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = viewHolder.itemView
                    val height = (itemView.bottom - itemView.top).toFloat()
                    val width = height / 4
                    val paint = Paint()
                    if (dX < 0) {
                        paint.color = Color.WHITE
                        val background = RectF(itemView.right.toFloat() + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                        c.drawRect(background, paint)

                        icon = BitmapFactory.decodeResource(resources, R.drawable.ic_delete)
                        val iconTop = itemView.top.toFloat() + (height - width) / 2
                        val iconRight = itemView.right.toFloat() - width + dX
                        val iconDst = RectF(iconRight, iconTop, iconRight + width, iconTop + width)
                        c.drawBitmap(icon, null, iconDst, null)
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
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
                    EncryptedPrefs.deleteMyId()
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