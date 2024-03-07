package com.brandon.campingmate.presentation.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brandon.campingmate.domain.model.CampEntity
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import timber.log.Timber

class ProfileViewModel : ViewModel() {
    private val _bookmarkedList: MutableLiveData<List<CampEntity>> = MutableLiveData()
    val bookmarkedList: LiveData<List<CampEntity>> get() = _bookmarkedList
    private val bookmarkCamp: MutableList<CampEntity> = mutableListOf()

    fun getBookmark(userID: String) {
        Timber.tag("겟북마크검사").d("작동중")
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document("Kakao${userID}")
        val contentIds = mutableListOf<String>()
        docRef.get().addOnSuccessListener {
            if (it.exists()) {
                val bookmarkData = it.get("bookmarked") as? List<*>
                Timber.tag("겟북마크목록검사").d(bookmarkData.toString())
                if (bookmarkData != null) {
                    for (item in bookmarkData) {
                        contentIds.add(item.toString())
                    }
                    Timber.tag("북마크목록검사").d("$contentIds")
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
            Timber.tag("콜북마크검사").d(bookmarkCamp.size.toString())
        }
    }

}