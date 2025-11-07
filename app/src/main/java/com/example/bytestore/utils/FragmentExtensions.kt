package com.example.bytestore.utils

import androidx.fragment.app.Fragment
import com.example.bytestore.ui.MainActivity

fun Fragment.topBar() = (requireActivity() as MainActivity).topBar
fun Fragment.sessionManager(): SessionManager =
    (requireActivity() as MainActivity).sessionManager
