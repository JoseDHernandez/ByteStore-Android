package com.example.bytestore.ui.components

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.navigation.NavController
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
        navController?.let {
            if (!it.navigateUp()) {
                (context as? Activity)?.onBackPressed()
            }
        } ?: run {
            (context as? Activity)?.onBackPressed()
        }
    }

}