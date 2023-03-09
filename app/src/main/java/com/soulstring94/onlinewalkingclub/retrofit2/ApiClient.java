package com.soulstring94.onlinewalkingclub.retrofit2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://uuuuuf.cafe24.com/retrofit/POST/OWC/";
    private static Retrofit retrofit;

    public static Retrofit getApiClient() {
        Gson gson = new GsonBuilder().generateNonExecutableJson().create();

        if(retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }

        return retrofit;
    }
}
