package com.brandon.campingmate.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

class PickMultipleImages : ActivityResultContract<Void?, List<Uri>?>() {
    override fun createIntent(context: Context, input: Void?): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri>? {
        if (resultCode != Activity.RESULT_OK || intent == null) {
            return null
        }
        val uris = mutableListOf<Uri>()

        val data = intent.clipData
        if (data != null) {
            for (i in 0 until data.itemCount) {
                val uri = data.getItemAt(i).uri
                uris.add(uri)
            }
        } else if (intent.data != null) {
            uris.add(intent.data!!)
        }
        return uris
    }

}