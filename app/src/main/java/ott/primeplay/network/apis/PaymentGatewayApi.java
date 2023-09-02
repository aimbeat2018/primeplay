package ott.primeplay.network.apis;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import ott.primeplay.network.model.PayuMoneyModel;
import ott.primeplay.network.model.ResponseStatus;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface PaymentGatewayApi {
//    @FormUrlEncoded
    @GET("payumoney")
    Call<ResponseBody> payumoney(@Header("API-KEY") String apiKey,
                                   @Query("key") String key);


    @FormUrlEncoded
    @POST("paymentgatway_status")
    Call<ResponseBody> paymentgatway_status(@Header("API-KEY") String apiKey,
                                            @Field("userId") String userId);

}
