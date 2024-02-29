package com.brandon.campingmate

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.brandon.campingmate.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.firestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var databaseReference: DatabaseReference
    private val TAG = "Main"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.mapview, MapFragment())
            commit()
        }

        // Firebase 데이터베이스 인스턴스 가져오기
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val db = Firebase.firestore


        // "camps" 경로의 DatabaseReference 가져오기
//        databaseReference = firebaseDatabase.getReference("camps")

        // 버튼 클릭 이벤트 처리
        binding.button.setOnClickListener {
            Log.e("CampInfo", "데이터 요청!!")

            val campsRef = db.collection("camps")

//            campsRef.whereEqualTo("induty", "글램핑")
            campsRef.whereArrayContainsAny("induty", listOf("카라반"))
                .limit(20)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        // 각 문서에 대한 작업 수행
                        Log.e(TAG, "${document.id} => ${document.data}\n")
                    }
                    Log.e(TAG, "결과 값: ${documents.toString()}")
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error getting documents: ", exception)
                }
        }
    }
}
