package com.careercompass.network;

import android.os.Build;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Intelligent Singleton Retrofit HTTP Client.
 *
 * Automatically detects whether the app is executing inside the Android Studio Emulator
 * (using 10.0.2.2 alias) or on a physical Android phone (using Mac's host Wi-Fi IP 10.173.188.75).
 * Additionally equips an auto-failover OkHttp interceptor that retries alternate hosts seamlessly.
 */
public class ApiClient {

    public static final String EMULATOR_HOST = "10.0.2.2";
    public static final String PHYSICAL_HOST = "10.173.188.75";
    public static final int PORT = 8080;

    private static String currentBaseUrl = getDefaultBaseUrl();
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    private static String getDefaultBaseUrl() {
        if (isEmulator()) {
            return "http://" + EMULATOR_HOST + ":" + PORT + "/api/";
        } else {
            return "http://" + PHYSICAL_HOST + ":" + PORT + "/api/";
        }
    }

    private static boolean isEmulator() {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk");
    }

    public static synchronized void setBaseUrl(String newUrl) {
        if (newUrl != null && !newUrl.equals(currentBaseUrl)) {
            if (!newUrl.endsWith("/")) {
                newUrl += "/";
            }
            currentBaseUrl = newUrl;
            retrofit = null;
            apiService = null;
        }
    }

    public static String getBaseUrl() {
        return currentBaseUrl;
    }

    public static synchronized ApiService getService() {
        if (apiService == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Failover interceptor: if 10.0.2.2 fails, retries 10.173.188.75 (and vice-versa)
            Interceptor failoverInterceptor = chain -> {
                Request request = chain.request();
                try {
                    return chain.proceed(request);
                } catch (IOException originalException) {
                    String host = request.url().host();
                    String alternateHost = null;
                    if (EMULATOR_HOST.equals(host)) {
                        alternateHost = PHYSICAL_HOST;
                    } else if (PHYSICAL_HOST.equals(host)) {
                        alternateHost = EMULATOR_HOST;
                    }

                    if (alternateHost != null) {
                        HttpUrl newUrl = request.url().newBuilder()
                                .host(alternateHost)
                                .build();
                        Request retryRequest = request.newBuilder()
                                .url(newUrl)
                                .build();
                        try {
                            Response response = chain.proceed(retryRequest);
                            // Set working URL for subsequent calls
                            currentBaseUrl = "http://" + alternateHost + ":" + PORT + "/api/";
                            return response;
                        } catch (IOException retryException) {
                            throw originalException;
                        }
                    }
                    throw originalException;
                }
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor(failoverInterceptor)
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(currentBaseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }
}
