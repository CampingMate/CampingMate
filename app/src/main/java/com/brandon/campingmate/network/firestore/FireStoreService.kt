package com.brandon.campingmate.network.firestore

import com.google.firebase.firestore.FirebaseFirestore

object FireStoreService {
    val fireStoreDB: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
}