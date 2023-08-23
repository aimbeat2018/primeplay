package ott.primeplay;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import ott.primeplay.network.RetrofitClient;
import ott.primeplay.network.apis.PassResetApi;
import ott.primeplay.utils.ToastMsg;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ChangePasswordActivity extends AppCompatActivity {

    TextInputEditText password, confirmpass;
    Button submit;
    String mobile = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_change_password);
        password = findViewById(R.id.password);
        confirmpass = findViewById(R.id.confirmpassword);
        submit = findViewById(R.id.submit);

        mobile = getIntent().getStringExtra("mobile_no");

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!password.getText().toString().equals(confirmpass.getText().toString())) {

                    new ToastMsg(ChangePasswordActivity.this).toastIconError("confirm password should match with password");

                } else {

                    changepassword(mobile, password.getText().toString());
                }
            }
        });

    }


    private void changepassword(String mobile, String password) {
        //dialog.show();
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        PassResetApi passResetApi = retrofit.create(PassResetApi.class);
        Call<ChangePasswordResponse> call = passResetApi.password_change(AppConfig.API_KEY, mobile, password);
        call.enqueue(new Callback<ChangePasswordResponse>() {
            @Override
            public void onResponse(Call<ChangePasswordResponse> call, Response<ChangePasswordResponse> response) {
                if (response.code() == 200) {
                    ChangePasswordResponse pr = response.body();
                    if (pr.getStatus().equals("success")) {
                        new ToastMsg(ChangePasswordActivity.this).toastIconSuccess(pr.getData());


                        Intent intent = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                        startActivity(intent);
                        //dialog.cancel();
                    } else {
                        new ToastMsg(ChangePasswordActivity.this).toastIconError(pr.getData());
                        // dialog.cancel();
                    }
                }
            }

            @Override
            public void onFailure(Call<ChangePasswordResponse> call, Throwable t) {
                new ToastMsg(ChangePasswordActivity.this).toastIconError("Something went wrong." + t.getMessage());
                // dialog.cancel();
                t.printStackTrace();
            }
        });
    }

}