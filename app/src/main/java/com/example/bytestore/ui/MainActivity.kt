package com.example.bytestore.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.example.bytestore.R
import com.example.bytestore.databinding.ActivityMainBinding
import com.example.bytestore.ui.viewmodel.AppViewModelFactory
import com.example.bytestore.ui.viewmodel.userViewModels.AccountViewModel
import com.example.bytestore.utils.Resource
import com.example.bytestore.utils.SessionManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val accountViewModel: AccountViewModel by viewModels {
        AppViewModelFactory(this)
    }

    lateinit var sessionManager: SessionManager

    val topBar get() = binding.topBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //sessionManager
        sessionManager = SessionManager(this)
        //Navegación
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController
        //pasar navcontroler al topBar
        binding.topBar.setNavController(navController)
        binding.topBar.setOnBackClickListener {
            //cerrar app si no hay fragmentos en el stack
            if (!navController.navigateUp()) {
                finishAffinity()
            }
        }
        navController.addOnDestinationChangedListener { _, destination, _ ->

            //mostar navbar
            when (destination.id) {
                R.id.loginFragment,
                R.id.registerFragment,
                R.id.mainFragment,
                R.id.splashFragment -> binding.navbar.visibility = View.GONE

                else -> binding.navbar.visibility = View.VISIBLE
            }
            //establer icono activo
            setActiveIndex(destination)
            //mostrar topBar
            when (destination.id) {
                R.id.loginFragment,
                R.id.mainFragment,
                R.id.splashFragment -> binding.topBar.visibility = View.GONE

                else -> binding.topBar.visibility = View.VISIBLE
            }
        }
        //eventos de logout y navegación desde BottonNavView
        binding.navbar.onLogoutSelected = {
            accountViewModel.logout()
        }
        binding.navbar.onItemSelected = { destinationId ->
            if (navController.currentDestination?.id != destinationId) {
                navController.navigate(destinationId)
            }
        }
        //validar sesion
        lifecycleScope.launch {
            sessionManager.isLoggedInFlow.collect { isLogged ->
                binding.navbar.disableOptionsButton(isLogged)
                //topBar
                if (isLogged) {
                    binding.topBar.hideLoginButton()
                } else {
                    binding.topBar.showLoginButton {
                        navController.navigate(R.id.action_global_loginFragment)
                    }
                }
            }
        }
        lifecycleScope.launch {
            sessionManager.userTokenFlow.collect { token->
                if (token != null) {
                    //
                }
            }
        }
        //observador del estado de logout
        accountViewModel.logoutState.observe(this) { state ->
            when (state) {
                is Resource.Idle -> Unit
                is Resource.ValidationError -> Unit
                is Resource.Loading -> {
                    Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show()
                }

                is Resource.Success -> {
                    navController.navigate(R.id.action_global_loginFragment)
                    Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                }

                is Resource.Error -> {
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /*
    Establcer en la barra de navegacion el item activo segun el fragment
    * 0 = productos
    * 1 = ordenes
    * 2 = carrito
    * 3 = opciones
    * */
    private fun setActiveIndex(fragment: NavDestination) {
        val id = fragment.id
        val actualFragment = when (id) {
            // productos
            R.id.productFragment, R.id.productsFragment -> 0
            // órdenes
            R.id.orderFragment -> 1
            // perfil / opciones
            R.id.profileFragment -> 3
            // otros (puedes ajustar cuando agregues carrito u otros)
            else -> 0
        }
        binding.navbar.setActiveItem(actualFragment)
    }

    fun Fragment.topBar() = (requireActivity() as MainActivity).topBar

}