package com.headshot2017.wifikb

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.headshot2017.wifikb.ui.ConnectionScreen
import com.headshot2017.wifikb.ui.MainMenuScreen
import com.headshot2017.wifikb.ui.UDPViewModel
import com.headshot2017.wifikb.ui.theme.WifikbClientTheme

class MainActivity : ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WifikbClientTheme {
                WifikbApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifikbApp(
    navController: NavHostController = rememberNavController(),
    theViewModel: UDPViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text("wifikb client")},
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = Routes.MAINMENU) {
            composable(Routes.MAINMENU) {
                MainMenuScreen(
                    navController,
                    Modifier.padding(innerPadding),
                    theViewModel
                )
            }
            composable(Routes.CONNECTION) { backStackEntry ->
                ConnectionScreen(
                    navController,
                    Modifier.padding(innerPadding),
                    theViewModel
                )
            }
        }
    }
}
