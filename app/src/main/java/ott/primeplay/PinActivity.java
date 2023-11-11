package ott.primeplay;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mukeshsolanki.OtpView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import ott.primeplay.database.DatabaseHelper;
import ott.primeplay.network.RetrofitClient;
import ott.primeplay.network.apis.LoginApi;
import ott.primeplay.network.model.Package;
import ott.primeplay.utils.Constants;
import ott.primeplay.utils.PreferenceUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PinActivity extends AppCompatActivity {

    OtpView  otp_viewIndia;
    String userEnterOtp = "";
    AppCompatButton login;
    TextView txt_forgotpin;
    String str_pin="",str_pin_from_response="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        otp_viewIndia = findViewById(R.id.otp_viewIndia);
        login = findViewById(R.id.btn_login);
        txt_forgotpin = findViewById(R.id.txt_forgotpin);
        otp_viewIndia.setOtpCompletionListener(otp -> userEnterOtp = otp);


        SharedPreferences sharedPreferencesfamily = PinActivity.this.getSharedPreferences(Constants.USER_PIN, MODE_PRIVATE);
        str_pin = sharedPreferencesfamily.getString("user_pin", "0000");


        if (getIntent() != null) {

            str_pin_from_response  = getIntent().getExtras().getString("pin","defaultKey");

        }


        txt_forgotpin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent = new Intent(PinActivity.this, ForgotPinActivity.class);
                startActivity(intent);
                finish();


            }
        });



        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (userEnterOtp.equals("")) {

                    Toast.makeText(PinActivity.this, "Enter PIN", Toast.LENGTH_SHORT).show();

                } else if (!userEnterOtp.equals(str_pin)  &&  !userEnterOtp.equals(str_pin_from_response)  ) {

                    Toast.makeText(PinActivity.this, "Enter Valid PIN", Toast.LENGTH_SHORT).show();
                }

                else if (    (!userEnterOtp.equals(str_pin_from_response) && userEnterOtp.equals(str_pin) ) || (!userEnterOtp.equals(str_pin) && userEnterOtp.equals(str_pin_from_response) )   || (userEnterOtp.equals(str_pin) && userEnterOtp.equals(str_pin_from_response) )  ) {

                    Intent intent = new Intent(PinActivity.this, MainActivity.class);
                    intent.putExtra("login_status", "user_login");
                    startActivity(intent);
                    finish();

                }

                else {
/*
                    Intent intent = new Intent(PinActivity.this, MainActivity.class);
                   *//* intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);*//*
                    startActivity(intent);
                    finish();*/

                }
            }
        });


    }




}