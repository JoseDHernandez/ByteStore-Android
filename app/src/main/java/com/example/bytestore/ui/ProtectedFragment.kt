package com.example.bytestore.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.bytestore.R
import com.example.bytestore.utils.sessionManager
import kotlinx.coroutines.launch

abstract class ProtectedFragment : Fragment() {
    open val requiredRole: String? =
        null // agregar, si se necita que sea administrador:   override val requiredRole = "ADMINISTRADOR"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sessionManager = sessionManager()
        viewLifecycleOwner.lifecycleScope.launch {
            val hasAccess = sessionManager.checkAccess(requiredRole)
            if (!hasAccess) {
                val navController = findNavController()
                if (requiredRole != null) {
                    navController.navigate(R.id.action_global_loginFragment)
                } else {
                    //TODO: Cambiar redirecion a un fragmente de acceso no permito o el productos
                    navController.navigate(R.id.action_global_loginFragment)
                }
            }
        }
    }
}
