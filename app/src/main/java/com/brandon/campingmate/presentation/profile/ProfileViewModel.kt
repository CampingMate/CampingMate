package com.brandon.campingmate.presentation.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brandon.campingmate.data.remote.dto.PostDTO
import com.brandon.campingmate.data.remote.dto.UserDTO
import com.brandon.campingmate.domain.model.CampEntity
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
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
    private var removeBookmarkIndex : Int? = null
    private var removePostItem: PostDTO? = null
    private var removePostIndex: Int? = null

    fun getUserData(userID: String) {
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(userID)
        docRef.get().addOnSuccessListener {
            val item = it.toObject(UserDTO::class.java)
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
            callPosts(posts.sortedByDescending { it.timestamp }.toMutableList())
        }
            .addOnFailureListener {
                Timber.tag("getPostsFail").d(it.toString())
            }
    }

    private fun callPosts(posts: MutableList<PostDTO>) {
        posts.forEach { writingPost.add(it) }
        _postList.value = writingPost
    }

    fun removeBookmarkAdapter(contentID: String) {
        _bookmarkedList.value = _bookmarkedList.value?.toMutableList()?.apply {
            removeBookmarkItem = find { it.contentId == contentID }
            removeBookmarkIndex = indexOf(removeBookmarkItem)
            remove(removeBookmarkItem)
        } ?: mutableListOf()
    }

    fun removeBookmarkDB(userID: String) {
        val db = FirebaseFirestore.getInstance()
        val docRef =  db.collection("users").document(userID)
        docRef.update("bookmarked",FieldValue.arrayRemove(removeBookmarkItem?.contentId))
    }

    fun undoBookmarkCamp() {
        _bookmarkedList.value = _bookmarkedList.value?.toMutableList()?.apply {
            removeBookmarkItem?.let {
                if (removeBookmarkIndex != null && removeBookmarkIndex!! in 0 until size) {
                    add(removeBookmarkIndex!!, it)
                } else {
                    add(it)
                }
            }
        } ?: mutableListOf()
    }

    fun removePostAdapter(postID: String) {
        _postList.value = _postList.value?.toMutableList()?.apply {
            removePostItem = find { it.postId == postID }
            removePostIndex = indexOf(removePostItem)
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
            removePostItem?.let {
                if (removePostIndex != null && removePostIndex!! in 0 until size) {
                    add(removePostIndex!!, it)
                } else {
                    add(it)
                }
            }
        } ?: mutableListOf()
    }

    fun initAdapterList() {
        _bookmarkedList.value = emptyList()
        _postList.value = emptyList()
    }
}