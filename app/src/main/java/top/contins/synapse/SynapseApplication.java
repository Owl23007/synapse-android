package top.contins.synapse;

import android.app.Application;
import android.os.Build;

import dagger.hilt.android.HiltAndroidApp;

/**
 * Synapse 应用的 Application 类
 * 负责应用全局的初始化工作，包括：
 * - Hilt 依赖注入框架初始化
 * - 禁用 Android 12+ 的动态颜色功能，保持应用自定义主题一致性
 */
@HiltAndroidApp
public class SynapseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 禁用动态颜色功能
        disableDynamicColors();
    }

    /**
     * 禁用Android 12+的动态颜色功能
     */
    private void disableDynamicColors() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // 强制使用应用自定义主题，不跟随系统配色
            try {
                getApplicationContext().getTheme().applyStyle(R.style.Theme_Synapse, true);
            } catch (Exception e) {
                // 忽略异常，继续使用默认配置
            }
        }
    }
}