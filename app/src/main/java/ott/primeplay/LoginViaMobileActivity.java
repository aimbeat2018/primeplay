package ott.primeplay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//import com.clevertap.android.sdk.CleverTapAPI;
import com.appsflyer.AFInAppEventParameterName;
import com.appsflyer.AFInAppEventType;
import com.appsflyer.AppsFlyerLib;
import com.clevertap.android.sdk.CleverTapAPI;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import ott.primeplay.database.DatabaseHelper;
import ott.primeplay.network.RetrofitClient;
import ott.primeplay.network.apis.LoginApi;
import ott.primeplay.network.apis.PassResetApi;
import ott.primeplay.network.apis.SubscriptionApi;
import ott.primeplay.network.model.ActiveStatus;
import ott.primeplay.network.model.PasswordReset;
import ott.primeplay.network.model.User;
import ott.primeplay.utils.ApiResources;
import ott.primeplay.utils.Constants;
import ott.primeplay.utils.RtlUtils;
import ott.primeplay.utils.ToastMsg;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginViaMobileActivity extends AppCompatActivity {

    private EditText etEmail, etPass;
    private AppCompatButton btnReset;
    TextView forgotPass,tv_password, txtcountryCode;
    private ProgressDialog dialog;
    private View backgroundView;
    TextView txt_login_with_otp;
    private String deviceId = "";
    private String firebaseToken = "", mobile = "", countryCode = "91";

  CleverTapAPI clevertapDefaultInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RtlUtils.setScreenDirection(this);
        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        boolean isDark = sharedPreferences.getBoolean("dark", false);

        clevertapDefaultInstance = CleverTapAPI.getDefaultInstance(getApplicationContext());

        mobile = getIntent().getStringExtra("mobile");
        countryCode = getIntent().getStringExtra("countryCode");

        if (isDark) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_via_mobile);
//        Toolbar toolbar = findViewById(R.id.toolbar);
        backgroundView = findViewById(R.id.background_view);

/*
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/

        //---analytics-----------
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "login_via_mobile_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        etEmail = findViewById(R.id.email);
        btnReset = findViewById(R.id.reset_pass);
        etPass = findViewById(R.id.etPass);
        txt_login_with_otp = findViewById(R.id.txt_login_with_otp);
        forgotPass = findViewById(R.id.forgotPass);
        txtcountryCode = findViewById(R.id.txtcountryCode);
        tv_password = findViewById(R.id.tv_password);

        txtcountryCode.setText(String.format("+%s ", countryCode));
        etEmail.setText(mobile);

        if (isDark) {
            backgroundView.setBackgroundColor(getResources().getColor(R.color.nav_head_bg));
            btnReset.setBackground(getResources().getDrawable(R.drawable.btn_rounded_dark));
        }
        deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait");
        dialog.setCancelable(false);

        forgotPass.setOnClickListener(view -> startActivity(new Intent(LoginViaMobileActivity.this, PassResetActivity.class)));

        btnReset.setOnClickListener(v -> {

            etPass.setVisibility(View.VISIBLE);
            tv_password.setVisibility(View.VISIBLE);
            forgotPass.setVisibility(View.VISIBLE);


            /*if (!isValidEmailAddress(etEmail.getText().toString())) {
                new ToastMsg(LoginActivity.this).toastIconError("Please enter valid email");
            } else*/
            if (etPass.getText().toString().equals("")) {
                new ToastMsg(LoginViaMobileActivity.this).toastIconError("Please enter password");
            } else {
                FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (task.isComplete()) {
                            firebaseToken = task.getResult();
                            Log.e("AppConstants", "onComplete: new Token got: " + firebaseToken);

                            String email = /*etEmail.getText().toString()*/mobile;
                            String pass = etPass.getText().toString();

                            login(email, pass);
                        }
                    }
                });
            }
        });


        txt_login_with_otp.setOnClickListener(v -> {
            if (!validCellPhone(etEmail.getText().toString())) {
                new ToastMsg(LoginViaMobileActivity.this).toastIconError("please enter valid mobile number");
                return;
            } else {
                if (countryCode.contains("91")) {
                    sendOtp();
                } else {
                    Intent intent = new Intent(LoginViaMobileActivity.this, OtpActivity.class);
                    intent.putExtra("from", "login");
                    intent.putExtra("countryCode", countryCode);
                    intent.putExtra("mobile_no", etEmail.getText().toString());
                    startActivity(intent);
                }
            }

        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean validCellPhone(String number) {
        return android.util.Patterns.PHONE.matcher(number).matches();
    }


    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    private void sendOtp() {
//        progressBar.setVisibility(View.VISIBLE);
        dialog.show();
  /*      String phoneNo = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
        if (phoneNo.contains("+91")) {
            phoneNo = phoneNo.replace("+91", "");
        }
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();*/
//        String mobile = "", email = "";
//        boolean validEmail = isValidEmailAddress(etEmail.getText().toString());
//        boolean validMobile = validCellPhone(etEmail.getText().toString());
//        if (validEmail) {
//            email = etEmail.getText().toString();
//            mobile = "";
//        } else if (validMobile) {
////            mobile = /*etEmail.getText().toString()*/mobile;
//            email = "";
//        }


        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        LoginApi api = retrofit.create(LoginApi.class);


        Call<ResponseBody> call = api.login_otp(AppConfig.API_KEY, mobile, "");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        String status = jsonObject.getString("status");

                        if (status.equals("success")) {
                            Toast.makeText(LoginViaMobileActivity.this, jsonObject.getString("data"), Toast.LENGTH_SHORT).show();
                            String otp = jsonObject.getString("otp");
                            Intent intent = new Intent(LoginViaMobileActivity.this, OtpActivity.class);
                            intent.putExtra("from", "login");
                            intent.putExtra("otp", otp);
                            intent.putExtra("countryCode", countryCode);
                            intent.putExtra("mobile_no", etEmail.getText().toString());

                            Log.d("OTP:", otp);

                            startActivity(intent);
                        } else {
//                            Toast.makeText(LoginViaMobileActivity.this, jsonObject.getString("data"), Toast.LENGTH_SHORT).show();
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginViaMobileActivity.this);
                            builder.setMessage("Mobile number not register, Please Register First.")
                                    .setCancelable(false)
                                    .setNegativeButton("Cancel", (dialog, id) -> {
                                        dialog.dismiss();
                                    })
                                    .setPositiveButton("OK", (dialog1, id) -> {
//                                        startActivity(new Intent(LoginViaMobileActivity.this, SignUpActivity.class));
                                        dialog.dismiss();
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    dialog.cancel();
                  /*  if (response.body().getStatus().equals("success")) {
//                        Toast.makeText(LoginViaMobileActivity.this, deviceId, Toast.LENGTH_LONG).show();
                        User user = response.body();
                        DatabaseHelper db = new DatabaseHelper(LoginViaMobileActivity.this);
                        db.deleteUserData();
                        db.insertUserData(user);
                        ApiResources.USER_PHONE = user.getPhone();

                        SharedPreferences.Editor preferences = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
                        preferences.putBoolean(Constants.USER_LOGIN_STATUS, true);
                        preferences.apply();
                        preferences.commit();

                        //save user login time, expire time
                        updateSubscriptionStatus(db.getUserData().getUserId());
                    }*/

                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
/*//                progressBar.setVisibility(View.GONE);
                changeDeviceSignIn();*/
                dialog.cancel();
            }
        });


    }


    public void addUserToCleverTap(String name, String id, String email, String mobile) {
        HashMap<String, Object> profileUpdate = new HashMap<String, Object>();
        profileUpdate.put("Name", name);    // String
        profileUpdate.put("Identity", id);      // String or number
        profileUpdate.put("Email", email); // Email address of the user
        profileUpdate.put("Phone", mobile);   // Phone (with the country code, starting with +)
//        profileUpdate.put("Gender", "M");             // Can be either M or F
//        profileUpdate.put("DOB", new Date());         // Date of Birth. Set the Date object to the appropriate value first
//// optional fields. controls whether the user will be sent email, push etc.
//        profileUpdate.put("MSG-email", false);        // Disable email notifications
        profileUpdate.put("MSG-push", true);          // Enable push notifications
//        profileUpdate.put("MSG-sms", false);          // Disable SMS notifications
//        profileUpdate.put("MSG-whatsapp", true);      // Enable WhatsApp notifications
//        ArrayList<String> stuff = new ArrayList<String>();
//        stuff.add("bag");
//        stuff.add("shoes");
//        profileUpdate.put("MyStuff", stuff);                        //ArrayList of Strings
//        String[] otherStuff = {"Jeans", "Perfume"};
//        profileUpdate.put("MyStuff", otherStuff);                   //String Array

        clevertapDefaultInstance.onUserLogin(profileUpdate);
       // clevertapDefaultInstance.pushProfile(profileUpdate);
    }


    private void login(String email, final String password) {
        btnReset.setClickable(false);
        dialog.show();
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        LoginApi api = retrofit.create(LoginApi.class);
        Call<User> call = api.postLoginStatus(AppConfig.API_KEY, email, password, deviceId, firebaseToken);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200) {
                    assert response.body() != null;
                    if (response.body().getStatus().equalsIgnoreCase("success")) {
                        User user = response.body();
                        ott.primeplay.database.DatabaseHelper db = new DatabaseHelper(LoginViaMobileActivity.this);
                        db.deleteUserData();
                        db.insertUserData(user);
                        ApiResources.USER_PHONE = user.getPhone();

                        SharedPreferences.Editor preferences = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
                        preferences.putBoolean(Constants.USER_LOGIN_STATUS, true);
                        preferences.apply();
                        preferences.commit();


                        String ccode = user.getCountry_code();
                        if (ccode != null) {
                            if (user.getPhone().contains(ccode)) {
                                addUserToCleverTap(user.getName(), user.getUserId(), user.getEmail(), user.getPhone());
                                addUserToAppsflyer(user.getName(), user.getUserId(), user.getEmail(), user.getPhone());
                            }
                            else {
                                addUserToCleverTap(user.getName(), user.getUserId(), user.getEmail(), ccode + user.getPhone());
                                addUserToAppsflyer(user.getName(), user.getUserId(), user.getEmail(), ccode + user.getPhone());
                            }

                        } else {
                            addUserToCleverTap(user.getName(), user.getUserId(), user.getEmail(), "+91" + user.getPhone());
                            addUserToAppsflyer(user.getName(), user.getUserId(), user.getEmail(), "+91" + user.getPhone());
                        }



                        //save user login time, expire time
                        updateSubscriptionStatus(db.getUserData().getUserId());
                    } else if (response.body().getStatus().equalsIgnoreCase("0")) {

                        User user = response.body();

                        addUserToCleverTap(user.getName(), user.getUserId(), user.getEmail(), user.getPhone());

                        btnReset.setClickable(true);
                        AlertDialog.Builder builder = new AlertDialog.Builder(LoginViaMobileActivity.this);
                        builder.setMessage("Already Login in other device.Do you want to change device?")
                                .setCancelable(false)
                                .setNegativeButton("Cancel", (dialog, id) -> {
                                    finishAffinity();
                                    dialog.dismiss();
                                })
                                .setPositiveButton("OK", (dialog1, id) -> {
                                    dialog1.dismiss();
                                    dialog.dismiss();
//                                    changeDeviceSignIn();
//                                    sendOtp();
                                    if (countryCode.contains("91")) {
                                        sendOtp();
                                    } else {
                                        Intent intent = new Intent(LoginViaMobileActivity.this, OtpActivity.class);
                                        intent.putExtra("from", "login");
                                        intent.putExtra("countryCode", countryCode);
                                        intent.putExtra("mobile_no", etEmail.getText().toString());
                                        startActivity(intent);
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    } else {
                        new ToastMsg(LoginViaMobileActivity.this).toastIconError(response.body().getData());
                        dialog.dismiss();
                        btnReset.setClickable(true);
                    }
                } else {
                    dialog.cancel();
                    new ToastMsg(LoginViaMobileActivity.this).toastIconError(getString(R.string.error_toast));
                    btnReset.setClickable(true);
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                dialog.cancel();
                new ToastMsg(LoginViaMobileActivity.this).toastIconError(getString(R.string.error_toast));
                btnReset.setClickable(true);
            }
        });
    }


    private void addUserToAppsflyer(String name, String userId, String email, String phone) {

        HashMap<String, Object> loginUpdate = new HashMap<String, Object>();
        loginUpdate.put("Phone", phone);   // Phone (with the country code, starting with +)
        AppsFlyerLib.getInstance().logEvent(getApplicationContext(),
                "Login" , loginUpdate);

    }


    public void updateSubscriptionStatus(String userId) {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);

        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(AppConfig.API_KEY, userId);
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(Call<ActiveStatus> call, Response<ActiveStatus> response) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        ActiveStatus activeStatus = response.body();

                        DatabaseHelper db = new DatabaseHelper(LoginViaMobileActivity.this);
                        db.deleteAllActiveStatusData();
                        db.insertActiveStatusData(activeStatus);

                        GoogleSignInOptions gso = new GoogleSignInOptions.
                                Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                                build();

                        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(LoginViaMobileActivity.this, gso);
                        googleSignInClient.signOut();

                        Intent intent = new Intent(LoginViaMobileActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

                        startActivity(intent);
                        finish();
                        dialog.cancel();

                    }
                }
            }

            @Override
            public void onFailure(Call<ActiveStatus> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}