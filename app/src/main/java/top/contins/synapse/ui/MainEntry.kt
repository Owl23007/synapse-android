package top.contins.synapse.ui

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
import top.contins.synapse.ui.home.HomeScreen
import top.contins.synapse.ui.splash.SplashScreen

/**
 * 应用主入口
 * 
 * 管理应用的顶级导航流程：
 * - Splash: 启动页，验证用户状态
 * - Auth: 认证页，用户登录
 * - Home: 主应用页面，底部导航管理
 * 
 * 同时设置全局 Snackbar 消息通知系统
 */
@Composable
@Preview
fun MainEntry(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    
    // 使用 SnackbarControllerProvider 包装整个应用，提供全局消息通知能力
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
                            navController.navigate("home") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    )
                }

                composable("auth") {
                    AuthScreen(
                        onLoginSuccess = {
                            navController.navigate("home") {
                                popUpTo("auth") { inclusive = true }
                            }
                        }
                    )
                }

                composable("home") {
                    // 主应用界面，包含底部导航栏和各功能模块
                    HomeScreen(
                        onLogout = {
                            navController.navigate("auth") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}
