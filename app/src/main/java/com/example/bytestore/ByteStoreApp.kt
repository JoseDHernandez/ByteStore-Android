package com.example.bytestore

import android.app.Application
import com.example.bytestore.core.ApiClient
import com.example.bytestore.utils.SessionManager

class ByteStoreApp: Application() {

    override fun onCreate() {
        super.onCreate()
        ApiClient.init(this)
    }
}