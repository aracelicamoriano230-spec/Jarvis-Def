package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.JarvisViewModel
import com.example.ui.JarvisMainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: JarvisViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize the central viewmodel using standard providers
        viewModel = ViewModelProvider(this)[JarvisViewModel::class.java]

        setContent {
            MyApplicationTheme(
                darkTheme = true, // Force sci-fi dark obsidian layouts
                dynamicColor = false // Force custom ironman-mark-85 palette
            ) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    JarvisMainScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

