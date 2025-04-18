package com.android.chatappcompose.auth.ui

import android.content.Intent
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.android.chatappcompose.MainActivity
import com.android.chatappcompose.auth.ui.viewModel.AuthViewModel
import com.android.chatappcompose.ui.theme.ChatAppComposeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : ComponentActivity() {

    private val authViewModel by viewModels<AuthViewModel>()

    override fun onStart() {
        super.onStart()
        if (authViewModel.currentUser != null) {
            StartMainActivity()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            ChatAppComposeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        val navHostController = rememberNavController()
                        // val authViewModel = hiltViewModel<AuthViewModel>()
                        AuthNavGraph(
                            navHostController = navHostController,
                            activity = this@AuthActivity,
                            authViewModel = authViewModel,
                            onUserStored = {
                                StartMainActivity()
                            }
                        )
                    }
                }
            }
        }
    }
    fun StartMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("name", "ashgsajgs")
        startActivity(intent)
        finish()
    }
}