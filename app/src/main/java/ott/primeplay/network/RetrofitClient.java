package ott.primeplay.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ott.primeplay.AppConfig;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {


    public static final String API_URL_EXTENSION = "/v130/";
    //    public static final String API_URL_EXTENSION_TEST= "/Testapi/";
    //copy your api username and password from php admin dashboard & paste below
    public static final String API_USER_NAME = "admin";
    public static final String API_PASSWORD = "1234";

    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance() {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new BasicAuthInterceptor(API_USER_NAME, API_PASSWORD)).build();
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(AppConfig.API_SERVER_URL + API_URL_EXTENSION)
//                    .baseUrl(AppConfig.API_SERVER_URL + API_URL_EXTENSION_TEST)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(client)
                    .build();
        }
        return retrofit;
    }
}
