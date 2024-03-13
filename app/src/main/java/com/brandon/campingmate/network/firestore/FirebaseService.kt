package com.brandon.campingmate.network.firestore

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object FirebaseService {
    val fireStoreDB: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    val firebaseStorage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }
}