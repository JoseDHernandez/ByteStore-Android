package com.example.bytestore.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.navigation.findNavController
import com.example.bytestore.R
import com.example.bytestore.databinding.ViewTopBarBinding
import com.example.bytestore.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TopBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding = ViewTopBarBinding.inflate(LayoutInflater.from(context), this, true)
    private val sessionManager = SessionManager(context)

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.TopBarView)
        binding.viewTitle.text = attributes.getString(R.styleable.TopBarView_titleText) ?: "Volver"
        attributes.recycle()
        checkLoginStatus()
    }

    //Establer titulo
    fun setTitle(title: String) {
        binding.viewTitle.text = title
    }

    //ocultar iniciar sesión
    private fun checkLoginStatus() {
        CoroutineScope(Dispatchers.Main).launch {
            val loggedIn = sessionManager.isLoggedIn()
            if (loggedIn) {
                binding.buttonAccount.visibility = View.GONE
            } else {
                binding.buttonAccount.visibility = View.VISIBLE
                binding.buttonAccount.setOnClickListener {
                    findNavController().navigate(R.id.action_global_loginFragment)
                }
            }
        }
    }

    //Boton de regreso
    fun setOnBackClickListener(action: () -> Unit) {
        binding.buttonBack.setOnClickListener { action() }
    }

}