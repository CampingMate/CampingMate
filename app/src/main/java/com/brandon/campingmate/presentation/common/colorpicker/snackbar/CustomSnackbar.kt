package com.brandon.campingmate.presentation.common.colorpicker.snackbar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.brandon.campingmate.databinding.PickerSnackbarLayoutBinding
import com.google.android.material.snackbar.Snackbar

class CustomSnackbar(context: Context, view: View, private val layoutInflater: LayoutInflater) {
    private var snackbar: Snackbar? = null
    private val binding = PickerSnackbarLayoutBinding.inflate(layoutInflater)

    init {
        createSnackbar(context, view)
    }

    private fun createSnackbar(context: Context, view: View) {
        snackbar = Snackbar.make(view, "", Snackbar.LENGTH_INDEFINITE)
    }

    fun showSnackbar(itemCount: Int) {
        if (itemCount > 0) {
            binding.btnAdd.text = "ADD ($itemCount)"
            snackbar?.show()
        } else {
            snackbar?.dismiss()
        }
    }
}