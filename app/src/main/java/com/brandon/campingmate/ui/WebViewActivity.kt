package com.brandon.campingmate.ui

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.brandon.campingmate.R

class WebViewActivity : AppCompatActivity() {

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        var webview = findViewById<WebView>(R.id.wv_webview)
        val url = "https://www.naver.com/";
        val webSettings : WebSettings = webview.settings

        webSettings.javaScriptEnabled = true
        webSettings.loadWithOverviewMode = true
        webview.webViewClient = WebViewClient()
        webview.loadUrl(url)

    }
}