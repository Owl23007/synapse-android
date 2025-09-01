package top.contins.synapse.util;

import android.content.Context;
import android.content.res.Resources;
import androidx.annotation.StringRes;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * 资源工具类
 * 提供资源访问的便捷方法
 */
@Singleton
public class ResourceUtils {

    private final Context context;

    @Inject
    public ResourceUtils(Context context) {
        this.context = context;
    }

    /**
     * 获取字符串资源
     */
    public String getString(@StringRes int resId) {
        return context.getString(resId);
    }

    /**
     * 获取格式化字符串资源
     */
    public String getString(@StringRes int resId, Object... formatArgs) {
        return context.getString(resId, formatArgs);
    }

    /**
     * 获取Resources实例
     */
    public Resources getResources() {
        return context.getResources();
    }
}
