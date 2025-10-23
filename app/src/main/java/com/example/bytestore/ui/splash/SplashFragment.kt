package com.example.bytestore.ui.splash

import android.os.Bundle
import android.os.Handler
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
import kotlinx.coroutines.launch


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
            delay(3000)
            checkSession()
        }
    }

    //validar cuenta local
    private suspend fun checkSession() {
        val isLoggedIt = sessionManager.isLoggedIn()
        val navController = findNavController()
        //si exite el splash
        if (!isAdded || _binding == null) return
        if (isLoggedIt) {
            navController.navigate(R.id.action_splashFragment_to_productsFragment)
        } else {
            navController.navigate(R.id.action_splashFragment_to_mainFragment)

        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}