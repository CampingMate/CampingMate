package com.brandon.campingmate.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.brandon.campingmate.NewActivity
import com.brandon.campingmate.R
import com.brandon.campingmate.data.PostCommentModel
import com.brandon.campingmate.data.PostModel
import com.brandon.campingmate.databinding.FragmentBoardBinding
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import timber.log.Timber

class BoardFragment : Fragment() {

    private var _binding: FragmentBoardBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()

    var num = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBoardBinding.inflate(inflater, container, false)




        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private fun initView() = with(binding) {
        btnUpload.setOnClickListener {
            uploadPostData()
        }

        btnDownload.setOnClickListener {
            downloadPostData()
        }
        btnUploadComment.setOnClickListener {
            val newComment = PostCommentModel("김철수$num", "정말 유익한 댓글")
            uploadComment(postId = "bVcbnmOWZfMPp9yggbIO", comment = newComment)
        }
        btnDownloadComment.setOnClickListener {
            getCommentData()
        }
        btnAutoDownloadComment.setOnClickListener {
            autoGetCommentData()
        }

        btnActivity.setOnClickListener {
            Intent(requireContext(), NewActivity::class.java).also {
                startActivity(it)
            }
            activity?.overridePendingTransition(R.anim.slide_in, R.anim.anim_none)
        }


    }

    private fun autoGetCommentData() {
        db.collection("posts").document("bVcbnmOWZfMPp9yggbIO").collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Timber.e("Listen failed: $e")
                    return@addSnapshotListener
                }

                for (docChange in snapshots!!.documentChanges) {
                    when (docChange.type) {
                        DocumentChange.Type.ADDED -> {
                            Timber.d(
                                "New comment: ${docChange.document.data}"
                            )
                            val origin = binding.tvContent.text.toString()
                            binding.tvContent.text = origin + "${docChange.document.data}\n"
                        }

                        DocumentChange.Type.MODIFIED -> Timber.d("Modified comment: ${docChange.document.data}")
                        DocumentChange.Type.REMOVED -> Timber.d("Removed comment: ${docChange.document.data}")
                    }
                }
            }
    }

    private fun getCommentData() {
        db.collection("posts").document("bVcbnmOWZfMPp9yggbIO")
            .collection("comments").orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Timber.d("Comment: ${document.data}")
                    val origin = binding.tvContent.text.toString()
                    binding.tvContent.text = origin + "${document.data}\n"
                }
            }
            .addOnFailureListener { e ->
                Timber.d("Error fetching comments: $e")
            }
    }

    private fun uploadComment(postId: String, comment: PostCommentModel) {
        db.collection("posts").document(postId).collection("comments")
            .add(comment)
            .addOnSuccessListener { documentRef ->
                Timber.d("Comment added with ID: ${documentRef.id}")
            }
            .addOnFailureListener { e ->
                Timber.d("Error adding comment: $e")
            }
    }

    private fun downloadPostData() {
        Timber.d("데이터 다운로드 시작!")
        db.collection("posts")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Timber.d("Document ID: ${document.id}, Data: ${document.data}")
                }
            }
            .addOnFailureListener { e ->
                Timber.e("Error getting documents: $e")
            }
        Timber.d("데이터 다운로드 종료!")
    }

    private fun uploadPostData() {
        Timber.d("데이터 업로드 시작!")
        val postRef = db.collection("posts")
        val postModel = PostModel(
            author = "송근영$num",
            authorId = "1234",
            authorProfileImageUrl = "이미지 주소입니다",
            title = "첫 게시물 업로드",
            content = "첫 게시물 내용입니다",
            imageUrlList = listOf("이미지 주소1", "이미지 주소2"),
        )
        num++

        postRef.add(postModel)
            .addOnSuccessListener { documentRef ->
                Timber.d("DocumentSnapshot added with ID: ${documentRef.id}")
                // 업로드 document 에는 id 없음
                // 해당 id를 유저의 글쓴 목록에 업데이트 함
                // 해당 문서를 이후 다운 받을 시 id를 postModel 에 저장함
            }
            .addOnFailureListener { e ->
                Timber.e("Error adding document", e)
            }
        Timber.d("데이터 업로드 종료!")
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = BoardFragment()
    }
}