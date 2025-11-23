package com.example.bytestore.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import com.example.bytestore.databinding.ViewTopBarBinding

class TopBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val binding = ViewTopBarBinding.inflate(LayoutInflater.from(context), this, true)
    private var customBackAction: (() -> Unit)? = null
    private var navController: NavController? = null

    init {
        binding.buttonBack.setOnClickListener {
            customBackAction?.invoke() ?: runDefaultBackAction()
        }
    }

    //Establer titulo
    fun setTitle(title: String) {
        binding.viewTitle.text = title
    }

    //ocultar boton de login
    fun hideLoginButton() {
        binding.buttonAccount.visibility = View.GONE
    }

    //callback de login
    fun showLoginButton(onClick: () -> Unit) {
        binding.buttonAccount.visibility = View.VISIBLE
        binding.buttonAccount.setOnClickListener { onClick() }
    }

    //Boton de regreso
    fun setOnBackClickListener(action: (() -> Unit)?) {
        customBackAction = action
    }

    fun setNavController(controller: NavController) {
        navController = controller
    }

    private fun runDefaultBackAction() {
        val nav = navController

        if (nav != null) {
            // Intentar volver un nivel en Navigation
            if (!nav.popBackStack()) {
                // Si no hay más backstack, cerrar
                (context as? AppCompatActivity)
                    ?.onBackPressedDispatcher
                    ?.onBackPressed()
            }
        } else {
            (context as? AppCompatActivity)
                ?.onBackPressedDispatcher
                ?.onBackPressed()
        }
    }

}