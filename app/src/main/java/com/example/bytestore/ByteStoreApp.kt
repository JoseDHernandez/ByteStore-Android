package com.example.bytestore

import android.app.Application
import com.example.bytestore.core.ApiClient

class ByteStoreApp : Application() {

    override fun onCreate() {
        super.onCreate()
        ApiClient.init(this)
    }
}