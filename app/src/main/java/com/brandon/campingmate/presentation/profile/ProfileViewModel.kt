package com.brandon.campingmate.presentation.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brandon.campingmate.domain.model.CampEntity
import com.brandon.campingmate.domain.model.Post
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import timber.log.Timber

class ProfileViewModel : ViewModel() {
    private val _bookmarkedList: MutableLiveData<List<CampEntity>> = MutableLiveData()
    val bookmarkedList: LiveData<List<CampEntity>> get() = _bookmarkedList
    private val bookmarkCamp: MutableList<CampEntity> = mutableListOf()

    private val _postList : MutableLiveData<List<Post>> = MutableLiveData()
    val postList : LiveData<List<Post>> get() = _postList
    private val writingPost : MutableList<Post> = mutableListOf()

    fun getBookmark(userID: String) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document("Kakao${userID}")
        val contentIds = mutableListOf<String>()
        docRef.get().addOnSuccessListener {
            if (it.exists()) {
                val bookmarkData = it.get("bookmarked") as? List<*>
                if (bookmarkData != null) {
                    for (item in bookmarkData) {
                        contentIds.add(item.toString())
                    }
                }
                bookmarkCamp.clear()
                if (contentIds.isNotEmpty()) {
                    callBookmarkCamp(contentIds)
                }
            }
        }
    }

    private fun callBookmarkCamp(bookmarkedItemList: MutableList<String>) {
        val db = Firebase.firestore
        val baseQuery: Query = db.collection("camps")
        val result = baseQuery.whereIn("contentId", bookmarkedItemList)
        result.get().addOnSuccessListener {
            for (doc in it) {
                val camp = doc.toObject(CampEntity::class.java)
                bookmarkCamp.add(camp)
            }
            _bookmarkedList.value = bookmarkCamp
        }
    }
    fun getPosts(userID: String) {
        val db = Firebase.firestore
        val baseQuery: Query = db.collection("posts")
        val result = baseQuery.whereIn("authorId", listOf("Kakao${userID}"))
        writingPost.clear()
        result.get().addOnSuccessListener {
            for (doc in it) {
                val post = doc.toObject(Post::class.java)
                writingPost.add(post)
            }
            _postList.value = writingPost
        }
    }
}