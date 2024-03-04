package com.brandon.campingmate.song.network

import com.google.firebase.firestore.FirebaseFirestore

object FireStoreService {
    val fireStoreDB: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
}