package ott.primeplay;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.mukeshsolanki.OtpView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import ott.primeplay.network.RetrofitClient;
import ott.primeplay.network.apis.LoginApi;
import ott.primeplay.utils.Constants;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class ForgotPinActivity extends AppCompatActivity {
    OtpView newpin;
    AppCompatButton btn_submit_pin;
    AutoCompleteTextView tv_mobile;
    String userEnterOtp = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pin);

        newpin = findViewById(R.id.newpin);
        btn_submit_pin = findViewById(R.id.btn_submit_pin);
        tv_mobile = findViewById(R.id.tv_mobile);

        newpin.setOtpCompletionListener(otp -> userEnterOtp = otp);



        btn_submit_pin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (tv_mobile.equals("")) {

                    Toast.makeText(ForgotPinActivity.this, "Enter Register Mobile Numebr", Toast.LENGTH_SHORT).show();
                } else if (userEnterOtp.equals("")) {

                    Toast.makeText(ForgotPinActivity.this, "Enter PIN", Toast.LENGTH_SHORT).show();

                } else {
                    forgot_pin();

                }
            }
        });


    }


    private void forgot_pin() {
//        progressBar.setVisibility(View.VISIBLE);
        // dialog.show();

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        LoginApi api = retrofit.create(LoginApi.class);

        Call<ResponseBody> call = api.forgot_pin(AppConfig.API_KEY, tv_mobile.getText().toString(), userEnterOtp);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        String status = jsonObject.getString("status");
                        if (status.equals("success")) {//pin not generated


                            Toast.makeText(ForgotPinActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();

                            SharedPreferences.Editor editor1 = getSharedPreferences(Constants.USER_PIN, MODE_PRIVATE).edit();
                            editor1.putString("user_pin", userEnterOtp);
                            editor1.apply();




                            Intent intent = new Intent(ForgotPinActivity.this, MainActivity.class);
                            intent.putExtra("login_status", "user_login");
                            startActivity(intent);
                            finish();


                        } else {


                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // dialog.cancel();

                }
            }


            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
/*//                progressBar.setVisibility(View.GONE);
                changeDeviceSignIn();*/
                //  dialog.cancel();
            }
        });


    }
}