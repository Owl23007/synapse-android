package top.contins.synapse;

import android.app.Application;
import android.os.Build;

import java.security.Security;

import dagger.hilt.android.HiltAndroidApp;
import org.conscrypt.Conscrypt;

/**
 * Synapse应用的Application类
 * 使用Hilt进行依赖注入
 */
@HiltAndroidApp
public class SynapseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 禁用动态颜色功能
        disableDynamicColors();
        Security.insertProviderAt(Conscrypt.newProvider(), 1);

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