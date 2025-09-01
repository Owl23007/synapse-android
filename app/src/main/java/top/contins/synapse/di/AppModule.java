package top.contins.synapse.di;

import android.content.Context;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

/**
 * 应用程序级别的依赖注入模块
 * 提供应用程序范围的单例对象
 */
@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public Context provideContext(@ApplicationContext Context context) {
        return context;
    }
}
