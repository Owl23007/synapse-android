package top.contins.synapse.network.client;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import top.contins.synapse.network.interceptor.AuthInterceptor;

import java.io.IOException;

public class NetworkClient {
    private final OkHttpClient client;

    public NetworkClient() {
        this.client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor()) // 添加认证拦截器
                .build();
    }

    public String get(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}
