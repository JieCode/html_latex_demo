package cn.edu.bnu.demo_collection;
import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @CreateDate: 2025/7/1
 * @author: jingjie
 * @desc:
 * 支持多 host 配置的 Retrofit 工具类
 * 功能：
 * 1. 多 host 配置管理
 * 2. 动态切换 baseUrl
 * 3. 统一错误处理
 * 4. 请求日志打印
 * 5. 缓存控制
 * 6. 公共请求头管理
 */
public class RetrofitClient {

    // 默认配置
    private static final long DEFAULT_TIMEOUT = 30;
    private static final long DEFAULT_CACHE_SIZE = 10 * 1024 * 1024; // 10MB

    private static Map<String, Retrofit> retrofitMap = new HashMap<>();
    private static Map<String, OkHttpClient> clientMap = new HashMap<>();
    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    // 私有构造防止实例化
    private RetrofitClient() {}

    /**
     * 初始化 Retrofit 客户端
     * @param context 上下文
     * @param hostName host 名称（用于标识不同的 host）
     * @param baseUrl 基础URL
     * @param isDebug 是否调试模式
     */
    public static void init(Context context, String hostName, String baseUrl, boolean isDebug) {
        if (!retrofitMap.containsKey(hostName)) {
            synchronized (RetrofitClient.class) {
                if (!retrofitMap.containsKey(hostName)) {
                    Retrofit retrofit = createRetrofit(context, hostName, baseUrl, isDebug);
                    retrofitMap.put(hostName, retrofit);
                }
            }
        }
    }

    /**
     * 创建 Retrofit 实例
     */
    private static Retrofit createRetrofit(Context context, String hostName, String baseUrl, boolean isDebug) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(getOkHttpClient(context, hostName, isDebug))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    /**
     * 获取 OkHttpClient
     */
    private static OkHttpClient getOkHttpClient(Context context, String hostName, boolean isDebug) {
        if (!clientMap.containsKey(hostName)) {
            synchronized (RetrofitClient.class) {
                if (!clientMap.containsKey(hostName)) {
                    OkHttpClient.Builder builder = new OkHttpClient.Builder()
                            .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                            .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                            .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                            .cache(createCache(context))
                            .addInterceptor(createHeaderInterceptor());

                    // 调试模式下添加日志拦截器
                    if (isDebug) {
                        builder.addInterceptor(createLoggingInterceptor());
                    }

                    clientMap.put(hostName, builder.build());
                }
            }
        }
        return clientMap.get(hostName);
    }

    /**
     * 创建缓存
     */
    private static Cache createCache(Context context) {
        File cacheDir = new File(context.getCacheDir(), "retrofit_cache");
        return new Cache(cacheDir, DEFAULT_CACHE_SIZE);
    }

    /**
     * 创建日志拦截器
     */
    private static HttpLoggingInterceptor createLoggingInterceptor() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return interceptor;
    }

    /**
     * 创建公共请求头拦截器
     */
    private static Interceptor createHeaderInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request request = original.newBuilder()
                        .header("User-Agent", "RetrofitClient/1.0")
                        .header("Accept", "application/json")
                        .method(original.method(), original.body())
                        .build();
                return chain.proceed(request);
            }
        };
    }

    /**
     * 获取 API 服务
     * @param hostName host 名称
     * @param serviceClass 接口类
     */
    public static <T> T getService(String hostName, Class<T> serviceClass) {
        if (!retrofitMap.containsKey(hostName)) {
            throw new IllegalStateException("RetrofitClient for host " + hostName + " not initialized");
        }
        return retrofitMap.get(hostName).create(serviceClass);
    }

    /**
     * 动态更换 BaseUrl（为指定 host 创建新的 Retrofit 实例）
     * @param hostName host 名称
     * @param newBaseUrl 新的 baseUrl
     */
    public static void changeBaseUrl(Context context, String hostName, String newBaseUrl, boolean isDebug) {
        Retrofit newRetrofit = createRetrofit(context, hostName, newBaseUrl, isDebug);
        retrofitMap.put(hostName, newRetrofit);
    }

    /**
     * 添加自定义拦截器
     * @param hostName host 名称
     * @param interceptor 自定义拦截器
     */
    public static void addInterceptor(String hostName, Interceptor interceptor) {
        if (clientMap.containsKey(hostName)) {
            OkHttpClient currentClient = clientMap.get(hostName);
            OkHttpClient newClient = currentClient.newBuilder()
                    .addInterceptor(interceptor)
                    .build();
            clientMap.put(hostName, newClient);

            // 更新对应的 Retrofit 实例
            Retrofit retrofit = retrofitMap.get(hostName);
            Retrofit newRetrofit = retrofit.newBuilder()
                    .client(newClient)
                    .build();
            retrofitMap.put(hostName, newRetrofit);
        }
    }

    /**
     * 添加自定义转换器
     * @param hostName host 名称
     * @param factory 转换器工厂
     */
    public static void addConverterFactory(String hostName, retrofit2.Converter.Factory factory) {
        if (retrofitMap.containsKey(hostName)) {
            Retrofit retrofit = retrofitMap.get(hostName);
            Retrofit newRetrofit = retrofit.newBuilder()
                    .addConverterFactory(factory)
                    .build();
            retrofitMap.put(hostName, newRetrofit);
        }
    }

    /**
     * 网络请求结果封装类
     */
    public static class ApiResult<T> {
        private T data;
        private Throwable error;
        private int statusCode;
        private boolean isSuccess;

        private ApiResult(T data, Throwable error, int statusCode, boolean isSuccess) {
            this.data = data;
            this.error = error;
            this.statusCode = statusCode;
            this.isSuccess = isSuccess;
        }

        public static <T> ApiResult<T> success(T data) {
            return new ApiResult<>(data, null, 200, true);
        }

        public static <T> ApiResult<T> error(Throwable error, int statusCode) {
            return new ApiResult<>(null, error, statusCode, false);
        }

        public T getData() {
            return data;
        }

        public Throwable getError() {
            return error;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public boolean isSuccess() {
            return isSuccess;
        }
    }
}
