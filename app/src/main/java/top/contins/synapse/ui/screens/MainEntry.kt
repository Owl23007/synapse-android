package top.contins.synapse.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import top.contins.synapse.core.ui.compose.snackbar.LevelSnackbarHost
import top.contins.synapse.core.ui.compose.snackbar.SnackbarControllerProvider
import top.contins.synapse.feature.auth.AuthScreen

/**
 * 应用程序主屏幕
 * 管理应用内的导航逻辑
 */
@Composable
@Preview
fun MainEntry(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    
    // 使用 SnackbarControllerProvider 包装整个应用
    SnackbarControllerProvider { snackbarHostState, currentLevel ->
        Scaffold(
            snackbarHost = {
                LevelSnackbarHost(
                    hostState = snackbarHostState,
                    level = currentLevel
                )
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "splash",
                modifier = Modifier
                    .then(modifier)
                    .padding(paddingValues)
            ) {
                composable("splash") {
                    SplashScreen(
                        onNavigateToAuth = {
                            navController.navigate("auth") {
                                popUpTo("splash") { inclusive = true }
                            }
                        },
                        onNavigateToMain = {
                            navController.navigate("main") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    )
                }

                composable("auth") {
                    AuthScreen(
                        onLoginSuccess = {
                            navController.navigate("main") {
                                popUpTo("auth") { inclusive = true }
                            }
                        }
                    )
                }

                composable("main") {
                    // 主应用界面
                    HomeScreen(
                        onLogout = {
                            navController.navigate("auth") {
                                popUpTo("main") { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}
