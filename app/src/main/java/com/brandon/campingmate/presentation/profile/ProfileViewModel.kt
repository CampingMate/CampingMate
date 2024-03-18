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

class ProfileViewModel : ViewModel() {
    private val _bookmarkedList: MutableLiveData<List<CampEntity>> = MutableLiveData()
    val bookmarkedList: LiveData<List<CampEntity>> get() = _bookmarkedList
    private val bookmarkCamp: MutableList<CampEntity> = mutableListOf()

    private val _postList: MutableLiveData<List<Post>> = MutableLiveData()
    val postList: LiveData<List<Post>> get() = _postList
    private val writingPost: MutableList<Post> = mutableListOf()
    private var removeBookmarkItem : CampEntity? = null
    private var removePostItem : Post? = null


    fun getBookmark(userID: String) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(userID)
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
        val result = baseQuery.whereIn("authorId", listOf(userID))
        writingPost.clear()
        result.get().addOnSuccessListener {
            for (doc in it) {
                val post = doc.toObject(Post::class.java)
                writingPost.add(post)
            }
            _postList.value = writingPost
        }
    }

    fun removeBookmarkCamp(userID: String, contentID: String) {
        _bookmarkedList.value = _bookmarkedList.value?.toMutableList()?.apply {
            removeBookmarkItem = find { it.contentId == contentID }
            remove(removeBookmarkItem)
        } ?: mutableListOf()

        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(userID)
        val updateBookmarkList = mutableListOf<String>()
        _bookmarkedList.value?.forEach {
            updateBookmarkList.add(it.contentId.toString())
        }
        docRef.update("bookmarked", updateBookmarkList)
    }

    fun undoBookmarkCamp(userID: String) {
        _bookmarkedList.value = _bookmarkedList.value?.toMutableList()?.apply {
            removeBookmarkItem?.let { add(it) }
        } ?: mutableListOf()
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(userID)
        val updateBookmarkList = mutableListOf<String>()
        _bookmarkedList.value?.forEach {
            updateBookmarkList.add(it.contentId.toString())
        }
        docRef.update("bookmarked", updateBookmarkList)
    }

    fun removePost(postID: String) {
        _postList.value = _postList.value?.toMutableList()?.apply {
            removePostItem = find { it.postId == postID }
            remove(removePostItem)
        } ?: mutableListOf()

        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("posts")
        docRef.whereEqualTo("postId",removePostItem?.postId).get().addOnSuccessListener {
            for(doc in it) {
                doc.reference.delete()
            }
        }
    }

//    fun undoPost() {
//        _postList.value = _postList.value?.toMutableList()?.apply {
//            removePostItem?.let { add(it) }
//        } ?: mutableListOf()
//
//        val db = Firebase.firestore
//        val docRef = db.collection("posts").document()
//        val postItem = hashMapOf(
//            "authorId" to removePostItem?.authorId,
//            "authorName" to removePostItem?.authorName,
//            "authorProfileImageUrl" to removePostItem?.authorProfileImageUrl,
//            "content" to removePostItem?.content,
//            "imageUrls" to removePostItem?.imageUrls,
//            "postId" to removePostItem?.postId,
//            "timestamp" to removePostItem?.timestamp,
//            "title" to removePostItem?.title
//        )
//        docRef.set(postItem)
//    }
}