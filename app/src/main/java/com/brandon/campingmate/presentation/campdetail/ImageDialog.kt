package com.brandon.campingmate.presentation.campdetail

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.ImageView
import com.brandon.campingmate.R
import com.bumptech.glide.Glide

class ImageDialog(context: Context, private val imageUrl: String) : Dialog(context) {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_image)

        val imageView = findViewById<ImageView>(R.id.imageView)
        Glide.with(context)
            .load(imageUrl)
            .into(imageView)

        // 이미지 클릭 시 다이얼로그 종료
        imageView.setOnClickListener {
            dismiss()
        }
    }
}