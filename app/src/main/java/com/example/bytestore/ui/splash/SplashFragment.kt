package com.example.bytestore.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.bytestore.R
import com.example.bytestore.databinding.FragmentSplashBinding
import com.example.bytestore.utils.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull


class SplashFragment : Fragment() {
    private var _binding: FragmentSplashBinding? = null
    private lateinit var sessionManager: SessionManager
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            checkSession()
        }
    }

    //validar cuenta local
    private suspend fun checkSession() {
        val navController = findNavController()
        val loggedIn = sessionManager.isLoggedInFlow.first()
        if (loggedIn) {
            //verficiar token (maximo 10s)
            val authResult = withTimeoutOrNull(10_000) {
                sessionManager.authToken()
            }
            //cerrar sesion y enviar al login
            if (authResult == null || !authResult) {
                sessionManager.logout()
                if (isAdded) navController.navigate(R.id.action_splashFragment_to_loginFragment)
                return
            }
            if (isAdded) navController.navigate(R.id.action_splashFragment_to_productsFragment)
        } else {
            //retardo normal de 3s
            delay(3000)
            if (isAdded) navController.navigate(R.id.action_splashFragment_to_mainFragment)
        }

    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}