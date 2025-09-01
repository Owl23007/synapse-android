package top.contins.synapse;

import android.app.Application;
import dagger.hilt.android.HiltAndroidApp;

/**
 * Synapse应用的Application类
 * 使用Hilt进行依赖注入
 */
@HiltAndroidApp
public class SynapseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 应用程序初始化逻辑
    }
}