package com.apiman.go4lunch.services;

import android.content.Context;

import com.apiman.go4lunch.BuildConfig;
import com.apiman.go4lunch.R;

import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class ApiClientConfig {
    static Retrofit getHttpClient(Context context) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        if(BuildConfig.DEBUG){
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(interceptor);
        }

        OkHttpClient okHttpClient = builder.build();

        String baseUrl = context.getString(R.string.api_base_url);

        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .build();
    }

    static Map<String, String> getNearbyDefaultParameters(Context context) {
        Map<String, String> map = getDefaultParameters(context);
        map.put("radius", context.getString(R.string.p_radius));
        map.put("type", context.getString(R.string.p_type_restaurant));

        return map;
    }

    static Map<String, String> getDefaultParameters(Context context) {
        Map<String, String> map = new HashMap<>();
        map.put("key", context.getString(R.string.p_api_key));
        return map;
    }
}
