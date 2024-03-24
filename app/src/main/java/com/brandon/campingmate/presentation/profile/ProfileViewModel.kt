package com.brandon.campingmate.presentation.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brandon.campingmate.data.remote.dto.PostDTO
import com.brandon.campingmate.data.remote.dto.UserDTO
import com.brandon.campingmate.domain.model.CampEntity
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import timber.log.Timber

class ProfileViewModel : ViewModel() {
    private val _userData: MutableLiveData<UserDTO?> = MutableLiveData()
    val userData: LiveData<UserDTO?> get() = _userData
    private val _bookmarkedList: MutableLiveData<List<CampEntity>> = MutableLiveData()
    val bookmarkedList: LiveData<List<CampEntity>> get() = _bookmarkedList
    private val bookmarkCamp: MutableList<CampEntity> = mutableListOf()

    private val _postList: MutableLiveData<List<PostDTO>> = MutableLiveData()
    val postList: LiveData<List<PostDTO>> get() = _postList
    private val writingPost: MutableList<PostDTO> = mutableListOf()
    private var removeBookmarkItem: CampEntity? = null
    private var removePostItem: PostDTO? = null

    fun getUserData(userID: String) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(userID)
        docRef.get().addOnSuccessListener {
            val item = it.toObject(UserDTO::class.java)
            Timber.tag("getUser검사").d(item.toString())
            _userData.value = item
        }
            .addOnFailureListener {
                Timber.tag("LoadUserDataFail").d(it.toString())
            }
    }

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
                if (contentIds.isNotEmpty()) {
                    bookmarkCamp.clear()
                    callBookmarkCamp(contentIds)
                } else {
                    contentIds.add("0")
                    bookmarkCamp.clear()
                    callBookmarkCamp(contentIds)
                }
            }
        }
            .addOnFailureListener {
                Timber.tag("getBookmarkFail").d(it.toString())
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
            .addOnFailureListener {
                Timber.tag("callBookmarkCampFail").d(it.toString())
            }
    }

    fun getPosts(userID: String) {
        val db = Firebase.firestore
        val baseQuery: Query = db.collection("posts")
        val result = baseQuery.whereIn("authorId", listOf(userID))
        val posts = mutableListOf<PostDTO>()
        result.get().addOnSuccessListener {
            for (doc in it) {
                val post = doc.toObject(PostDTO::class.java)
                posts.add(post)
            }
            writingPost.clear()
            callPosts(posts)
        }
            .addOnFailureListener {
                Timber.tag("getPostsFail").d(it.toString())
            }
    }

    private fun callPosts(posts: MutableList<PostDTO>) {
        posts.forEach { writingPost.add(it) }
        _postList.value = writingPost
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

    fun removePostAdapter(postID: String) {
        _postList.value = _postList.value?.toMutableList()?.apply {
            removePostItem = find { it.postId == postID }
            remove(removePostItem)
        } ?: mutableListOf()
    }

    fun removePostDB() {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("posts")
        docRef.whereEqualTo("postId", removePostItem?.postId).get().addOnSuccessListener {
            for (doc in it) {
                doc.reference.delete()
            }
        }
    }

    fun undoPost() {
        _postList.value = _postList.value?.toMutableList()?.apply {
            removePostItem?.let { add(it) }
        } ?: mutableListOf()
    }

    fun clearBookmarkedList() {
        _bookmarkedList.value = emptyList()
    }
}