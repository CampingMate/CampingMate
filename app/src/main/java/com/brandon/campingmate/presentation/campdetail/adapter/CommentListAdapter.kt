package com.brandon.campingmate.presentation.campdetail.adapter

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brandon.campingmate.R
import com.brandon.campingmate.databinding.ItemDetailCommentBinding
import com.brandon.campingmate.domain.model.CampCommentEntity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.kakao.sdk.user.UserApiClient

class CommentListAdapter(
) : ListAdapter<CampCommentEntity, CommentListAdapter.CampCommentViewHolder>(
    object : DiffUtil.ItemCallback<CampCommentEntity>(){
        override fun areItemsTheSame(
            oldItem: CampCommentEntity,
            newItem: CampCommentEntity
        ): Boolean = oldItem.userId == newItem.userId

        override fun areContentsTheSame(
            oldItem: CampCommentEntity,
            newItem: CampCommentEntity
        ): Boolean = oldItem == newItem
    }
){
    abstract class CampCommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun onBind(item: CampCommentEntity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CampCommentViewHolder =
        CampCommentItemViewHolder(
            ItemDetailCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )

    override fun onBindViewHolder(holder: CampCommentViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

    class CampCommentItemViewHolder(
        private val binding: ItemDetailCommentBinding,
    ) : CampCommentViewHolder(binding.root){
        override fun onBind(item: CampCommentEntity) = with(binding){
            if(item !is CampCommentEntity) {
                return@with
            }
            tvCommentContent.text = item.content
            tvCommentTime.text = item.date
            if(item.imageUrl.toString().isBlank()){
                ivCommentImg.visibility = View.GONE
            }
            Glide.with(binding.root)
                .load(item.imageUrl)
                .into(ivCommentImg)
            ivCommentImg.clipToOutline = true
            tvCommentUsername.text = item.userName.toString()
            Glide.with(binding.root)
                .load(item.userProfile)
                .into(ivUserProfile)

            binding.root.setOnLongClickListener {
                UserApiClient.instance.me { user, error ->
                    if (user?.id != null) {
                        val db = FirebaseFirestore.getInstance()
                        val commentId = item.userId
                        val myIdRef = db.collection("users").document("Kakao${user.id}")
                        myIdRef.get()
                            .addOnSuccessListener { documentSnapshot ->
                                if(documentSnapshot.exists()){
                                    val myUserId = documentSnapshot.getString("userId")
                                    Log.d("checkId", "아이템 아이디: ${commentId} \n 내아이디 : ${myUserId}")
                                    val campId = item.campId
                                    if(commentId == myUserId){
                                        showDeleteDialog(binding.root.context, item, campId)
                                    }
                                }
                            }
                    }
                }
                true
            }
        }

        private fun showDeleteDialog(context: Context, item: CampCommentEntity, campId: String) {
            val builder = AlertDialog.Builder(context)
            val logoutDialog = LayoutInflater.from(context).inflate(R.layout.dialog_comment_delete, null)

            builder.setView(logoutDialog)
            val dialog = builder.create()

            //다이얼로그 영역(기본값 화이트) 투명화로 둥근 테두리가 묻히지 않고 보이도록 설정
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()

            dialog.findViewById<TextView>(R.id.btn_logout_cancel)?.setOnClickListener {
                dialog.dismiss()
            }

            dialog.findViewById<TextView>(R.id.btn_logout_comfirm)?.setOnClickListener {
                //Todo: 댓글 삭제 및 파이어스토어 업데이트
                deleteComment(item, campId)
                dialog.dismiss()
            }
        }
        private fun deleteComment(item: CampCommentEntity, campId: String) {
            val db = FirebaseFirestore.getInstance()
            val campRef = db.collection("camps").whereEqualTo("contentId", campId)
            campRef.get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        return@addOnSuccessListener
                    }
                    // 해당 캠핑장의 댓글 리스트를 가져와서 삭제할 댓글을 찾음
                    val campDoc = documents.documents[0]
                    val commentList = campDoc.get("commentList") as? MutableList<Map<String, Any?>> ?: mutableListOf()
                    val iterator = commentList.iterator()

                    // 삭제할 댓글을 찾아서 리스트에서 제거
                    while (iterator.hasNext()) {
                        val comment = iterator.next()
                        val userId = comment["userId"] as String
                        val userName = comment["userName"] as String
                        val content = comment["content"] as String
                        val date = comment["date"] as String
                        val imageUrl = comment["img"] as String

                        if (userId == item.userId &&
                            userName == item.userName &&
                            content == item.content &&
                            date == item.date &&
                            imageUrl == item.imageUrl.toString()
                        ) {
                            iterator.remove()
                            break
                        }
                    }
                    // 업데이트된 댓글 리스트를 Firestore에 반영
                    campDoc.reference.update("commentList", commentList)
                        .addOnSuccessListener {
                            Log.d("CampDetailActivity", "댓글 삭제 성공")
                            // RecyclerView에 반영하기 위해 ViewModel을 통해 새로운 댓글 리스트 업데이트
//                            viewModel.updateCommentList(commentList)
                        }
                        .addOnFailureListener { e ->
                            Log.e("CampDetailActivity", "댓글 삭제 실패: $e")
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("CampDetailActivity", "캠핑장 쿼리 실패: $e")
                }
        }

    }
}