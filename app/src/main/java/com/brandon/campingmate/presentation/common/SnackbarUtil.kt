package com.brandon.campingmate.presentation.common

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.brandon.campingmate.R
import com.brandon.campingmate.presentation.login.LoginActivity
import com.google.android.material.snackbar.Snackbar

object SnackbarUtil {

    fun showSnackBar(view: View) {
        val snackbar = Snackbar.make(view, "", 2000)
        val inflater = LayoutInflater.from(view.context)
        val customLayout = inflater.inflate(R.layout.dialog_snackbar, null)

        val snackbarView = snackbar.view
        val snackbarLayout = snackbarView as Snackbar.SnackbarLayout
        snackbarLayout.addView(customLayout, 0)

        snackbarView.setBackgroundColor(Color.TRANSPARENT)

        snackbar.show()

        val textViewGoLogin = snackbarLayout.findViewById<TextView>(R.id.tv_go_login)
        textViewGoLogin?.setOnClickListener {
            val intent = Intent(view.context, LoginActivity::class.java)
            view.context.startActivity(intent)
        }
    }

}