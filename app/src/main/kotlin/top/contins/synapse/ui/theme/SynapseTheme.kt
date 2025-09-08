package top.contins.synapse.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect

import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 浅蓝色亮色主题
private val LightBlueColorScheme = lightColorScheme(
    primary = LightBluePrimary,
    onPrimary = LightBlueOnPrimary,
    primaryContainer = LightBluePrimaryContainer,
    onPrimaryContainer = LightBlueOnPrimaryContainer,
    secondary = LightBlueSecondary,
    onSecondary = LightBlueOnSecondary,
    secondaryContainer = LightBlueSecondaryContainer,
    onSecondaryContainer = LightBlueOnSecondaryContainer,
    tertiary = LightBlueSecondary,
    onTertiary = LightBlueOnSecondary,
    tertiaryContainer = LightBlueSecondaryContainer,
    onTertiaryContainer = LightBlueOnSecondaryContainer,
    error = LightBlueError,
    onError = LightBlueOnError,
    errorContainer = LightBlueErrorContainer,
    onErrorContainer = LightBlueOnErrorContainer,
    outline = LightBlueOutline,
    outlineVariant = LightBlueOutlineVariant,
    background = LightBlueBackground,
    onBackground = LightBlueOnBackground,
    surface = LightBlueSurface,
    onSurface = LightBlueOnSurface,
    surfaceVariant = LightBlueSurfaceVariant,
    onSurfaceVariant = LightBlueOnSurfaceVariant,
)

// 浅蓝色暗色主题
private val DarkBlueColorScheme = darkColorScheme(
    primary = DarkBluePrimary,
    onPrimary = DarkBlueOnPrimary,
    primaryContainer = DarkBluePrimaryContainer,
    onPrimaryContainer = DarkBlueOnPrimaryContainer,
    secondary = DarkBlueSecondary,
    onSecondary = DarkBlueOnSecondary,
    secondaryContainer = DarkBlueSecondaryContainer,
    onSecondaryContainer = DarkBlueOnSecondaryContainer,
    tertiary = DarkBlueSecondary,
    onTertiary = DarkBlueOnSecondary,
    tertiaryContainer = DarkBlueSecondaryContainer,
    onTertiaryContainer = DarkBlueOnSecondaryContainer,
    error = DarkBlueError,
    onError = DarkBlueOnError,
    errorContainer = DarkBlueErrorContainer,
    onErrorContainer = DarkBlueOnErrorContainer,
    outline = DarkBlueOutline,
    outlineVariant = DarkBlueOutlineVariant,
    background = DarkBlueBackground,
    onBackground = DarkBlueOnBackground,
    surface = DarkBlueSurface,
    onSurface = DarkBlueOnSurface,
    surfaceVariant = DarkBlueSurfaceVariant,
    onSurfaceVariant = DarkBlueOnSurfaceVariant,
)

@Composable
fun SynapseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,  // 禁用动态颜色
    content: @Composable () -> Unit
) {
    // 强制使用固定的浅蓝色主题，不跟随系统动态颜色
    val colorScheme = if (darkTheme) DarkBlueColorScheme else LightBlueColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val insetsController = WindowCompat.getInsetsController(window, view)

            // 使用现代API设置状态栏样式
            insetsController.isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
