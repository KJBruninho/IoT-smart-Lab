package com.example.myapplication

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.auth.BiometricAuthManager
import com.example.myapplication.data.api.NetworkModule
import com.example.myapplication.data.local.TokenStore
import com.example.myapplication.data.repository.AlertasRepository
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.ControloRepository
import com.example.myapplication.data.repository.DashboardRepository
import com.example.myapplication.ui.dashboard.AlertasScreen
import com.example.myapplication.ui.dashboard.AlertasViewModel
import com.example.myapplication.ui.dashboard.ControloScreen
import com.example.myapplication.ui.dashboard.ControloViewModel
import com.example.myapplication.ui.dashboard.DashboardScreen
import com.example.myapplication.ui.login.LoginScreen
import com.example.myapplication.ui.login.LoginViewModel
import com.example.myapplication.ui.sensors.SensorsScreen
import com.example.myapplication.ui.sensors.SensorsViewModel
import com.example.myapplication.auth.JwtUtils

import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private val loggedInState = mutableStateOf(false)
    private val unlockedState = mutableStateOf(false)
    private val biometricErrorState = mutableStateOf<String?>(null)
    private val currentScreenState = mutableStateOf(AppRoutes.LOGIN)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val biometricAuthManager = BiometricAuthManager(this)

        val tokenStore = TokenStore(applicationContext)

        val authApi = NetworkModule.provideAuthApi(tokenStore)
        val authRepository = AuthRepository(authApi, tokenStore)

        val dashboardApi = NetworkModule.provideDashboardApi(tokenStore)
        val dashboardRepository = DashboardRepository(dashboardApi)

        val alertasApi = NetworkModule.provideAlertasApi(tokenStore)
        val alertasRepository = AlertasRepository(alertasApi)

        val controloApi = NetworkModule.provideControloApi(tokenStore)
        val controloRepository = ControloRepository(controloApi)

        val loginViewModel = ViewModelProvider(
            this,
            SimpleViewModelFactory {
                LoginViewModel(authRepository)
            }
        )[LoginViewModel::class.java]

        val sensorsViewModel = ViewModelProvider(
            this,
            SimpleViewModelFactory {
                SensorsViewModel(dashboardRepository)
            }
        )[SensorsViewModel::class.java]

        val alertasViewModel = ViewModelProvider(
            this,
            SimpleViewModelFactory {
                AlertasViewModel(alertasRepository)
            }
        )[AlertasViewModel::class.java]

        val controloViewModel = ViewModelProvider(
            this,
            SimpleViewModelFactory {
                ControloViewModel(controloRepository)
            }
        )[ControloViewModel::class.java]

        fun pedirBiometria() {
            if (!biometricAuthManager.canAuthenticate()) {
                biometricErrorState.value = "Biometria/PIN não disponível neste dispositivo."
                unlockedState.value = true
                currentScreenState.value = AppRoutes.DASHBOARD
                return
            }

            biometricAuthManager.authenticate(
                onSuccess = {
                    biometricErrorState.value = null
                    unlockedState.value = true
                    currentScreenState.value = AppRoutes.DASHBOARD
                },
                onError = { erro ->
                    biometricErrorState.value = erro
                    unlockedState.value = false
                }
            )
        }

        lifecycleScope.launch {
            authRepository.token.collect { token ->

                if (JwtUtils.isExpired(token)) {
                    authRepository.logout()
                    loggedInState.value = false
                    unlockedState.value = false
                    currentScreenState.value = AppRoutes.LOGIN
                    return@collect
                }

                loggedInState.value = true

                if (!unlockedState.value) {
                    currentScreenState.value = AppRoutes.BIOMETRIC
                }
            }
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (!loggedInState.value) {
                        LoginScreen(
                            viewModel = loginViewModel,
                            onLoginSuccess = {
                                unlockedState.value = true
                                currentScreenState.value = AppRoutes.DASHBOARD
                            }
                        )
                    } else if (!unlockedState.value) {
                        BiometricUnlockScreen(
                            error = biometricErrorState.value,
                            onUnlock = {
                                pedirBiometria()
                            },
                            onLogout = {
                                lifecycleScope.launch {
                                    authRepository.logout()
                                }
                            }
                        )
                    } else {
                        when (currentScreenState.value) {
                            AppRoutes.DASHBOARD -> {
                                DashboardScreen(
                                    onOpenSensors = {
                                        sensorsViewModel.carregar()
                                        currentScreenState.value = AppRoutes.SENSORS
                                    },
                                    onOpenAlertas = {
                                        alertasViewModel.carregar()
                                        currentScreenState.value = AppRoutes.ALERTAS
                                    },
                                    onOpenControlo = {
                                        controloViewModel.carregarSensores()
                                        currentScreenState.value = AppRoutes.CONTROLO
                                    },
                                    onLogout = {
                                        lifecycleScope.launch {
                                            authRepository.logout()
                                        }
                                    }
                                )
                            }

                            AppRoutes.SENSORS -> {
                                SensorsScreen(
                                    viewModel = sensorsViewModel,
                                    onBack = {
                                        currentScreenState.value = AppRoutes.DASHBOARD
                                    }
                                )
                            }

                            AppRoutes.ALERTAS -> {
                                AlertasScreen(
                                    viewModel = alertasViewModel,
                                    onBack = {
                                        currentScreenState.value = AppRoutes.DASHBOARD
                                    }
                                )
                            }

                            AppRoutes.CONTROLO -> {
                                ControloScreen(
                                    viewModel = controloViewModel,
                                    onBack = {
                                        currentScreenState.value = AppRoutes.DASHBOARD
                                    }
                                )
                            }

                            else -> {
                                currentScreenState.value = AppRoutes.DASHBOARD
                            }
                        }
                    }
                }
            }
        }
    }
}