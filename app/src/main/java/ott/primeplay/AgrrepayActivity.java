package ott.primeplay;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.clevertap.android.sdk.CleverTapAPI;
import com.test.pg.secure.pgsdkv4.PGConstants;
import com.test.pg.secure.pgsdkv4.PaymentGatewayPaymentInitializer;
import com.test.pg.secure.pgsdkv4.PaymentParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Random;

import okhttp3.ResponseBody;
import ott.primeplay.database.DatabaseHelper;
import ott.primeplay.network.RetrofitClient;
import ott.primeplay.network.apis.PaymentApi;
import ott.primeplay.network.apis.SubscriptionApi;
import ott.primeplay.network.model.ActiveStatus;
import ott.primeplay.network.model.Package;
import ott.primeplay.network.model.SubscriptionHistory;
import ott.primeplay.network.model.User;
import ott.primeplay.utils.Constants;
import ott.primeplay.utils.PreferenceUtils;
import ott.primeplay.utils.ToastMsg;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class AgrrepayActivity extends AppCompatActivity {
    ProgressBar pb;
    // TextView transactionIdView;
    //  TextView transactionStatusView,tvprice;
    TextView tvprice,tvplanname;
    private Package aPackage;
    private ott.primeplay.database.DatabaseHelper databaseHelper;

    String uid = "", uname = "", mobile = "", email = "", order_id = "", orderIdstr = "";
    String plantamount = "";

    CleverTapAPI clevertapChergedInstance;
    String str_user_age="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agrrepay);

        clevertapChergedInstance = CleverTapAPI.getDefaultInstance(getApplicationContext());
        clevertapChergedInstance.setDebugLevel(CleverTapAPI.LogLevel.VERBOSE);
        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.VERBOSE);
        try {
            //  Block of code to try
            SharedPreferences sharedPreferences = AgrrepayActivity.this.getSharedPreferences(Constants.USER_AGE, MODE_PRIVATE);
            str_user_age = sharedPreferences.getString("user_age", "20");

        }
        catch(Exception e) {
            e.printStackTrace();
        }

        if (getIntent() != null) {
            aPackage = (Package) getIntent().getSerializableExtra("package");
            databaseHelper = new DatabaseHelper(this);
        }

        User user = databaseHelper.getUserData();
        uid = user.getUserId();
        uname = user.getName();
//        mobile = "0000000000";
        mobile = user.getPhone();
        email = user.getEmail();

        pb = findViewById(R.id.progressBar2);
        pb.setVisibility(View.GONE);

        Button clickButton = (Button) findViewById(R.id.ee);
        //   transactionIdView = (TextView) findViewById(R.id.transactionid);
        //transactionStatusView = (TextView) findViewById(R.id.status);
        tvprice = (TextView) findViewById(R.id.price);
        tvplanname = (TextView) findViewById(R.id.tvplanname);

        tvprice.setText(aPackage.getPrice());
        tvplanname.setText(aPackage.getName());

        clickButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                pb.setVisibility(View.VISIBLE);

                Random rnd = new Random();
                int n = 100000 + rnd.nextInt(900000);
                SampleAppConstants.PG_ORDER_ID = Integer.toString(n);

                //use -com.test.pg.secure.pgsdkv4..  not payu
                com.test.pg.secure.pgsdkv4.PaymentParams pgPaymentParams = new PaymentParams();
                pgPaymentParams.setAPiKey(SampleAppConstants.PG_API_KEY);
                  pgPaymentParams.setAmount(aPackage.getPrice());
               // pgPaymentParams.setAmount("2");

                pgPaymentParams.setEmail(user.getEmail());
                pgPaymentParams.setName(user.getName());
                pgPaymentParams.setPhone(user.getPhone());
                pgPaymentParams.setOrderId(SampleAppConstants.PG_ORDER_ID);
                pgPaymentParams.setCurrency(SampleAppConstants.PG_CURRENCY);
                pgPaymentParams.setDescription(SampleAppConstants.PG_DESCRIPTION);
                pgPaymentParams.setCity(SampleAppConstants.PG_CITY);
                pgPaymentParams.setState(SampleAppConstants.PG_STATE);

                pgPaymentParams.setZipCode(SampleAppConstants.PG_ZIPCODE);
                pgPaymentParams.setCountry(SampleAppConstants.PG_COUNTRY);
                pgPaymentParams.setReturnUrl(SampleAppConstants.PG_RETURN_URL);
                pgPaymentParams.setMode(SampleAppConstants.PG_MODE);
                // pgPaymentParams.setEnableAutoRefund("n");
                //  pgPaymentParams.setOfferCode("");
                //pgPaymentParams.setSplitInfo("{\"vendors\":[{\"vendor_code\":\"24VEN985\",\"split_amount_percentage\":\"20\"}]}");

                PaymentGatewayPaymentInitializer pgPaymentInitialzer = new PaymentGatewayPaymentInitializer(pgPaymentParams, AgrrepayActivity.this);
                pgPaymentInitialzer.initiatePaymentProcess();

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PGConstants.REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    String paymentResponse = data.getStringExtra(PGConstants.PAYMENT_RESPONSE);
                    System.out.println("paymentResponse: " + paymentResponse);

                    if (paymentResponse.equals("null")) {
                        System.out.println("Transaction Error!");
                        // transactionIdView.setText("Transaction ID: NIL");
                        // transactionStatusView.setText("Transaction Status: Transaction Error!");
                    } else {


                        JSONObject response = new JSONObject(paymentResponse);

                        if (response.getString("response_code").equals("0")) {//response_code=0 for transaction successfull

                            Toast.makeText(this, "Payment successfully done", Toast.LENGTH_SHORT).show();
                            // transactionIdView.setText("Transaction ID: " + response.getString("transaction_id"));
                            // transactionStatusView.setText("Transaction Status: " + response.getString("response_message"));
                            saveChargeData(response.getString("transaction_id"), "aggrepay");

                        } else {

                            Toast.makeText(this, response.getString("response_message"), Toast.LENGTH_SHORT).show();
                            // transactionIdView.setText("Transaction ID: " + response.getString("transaction_id"));
                            //   transactionStatusView.setText("Transaction Status: " + response.getString("response_message"));

                        }

                        //   transactionIdView.setText("Transaction ID: " + response.getString("transaction_id"));
                        //  transactionStatusView.setText("Transaction Status: " + response.getString("response_message"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }

        }
    }


    public void saveChargeData(String token, String from) {
        // progressBar.setVisibility(View.VISIBLE);
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        PaymentApi paymentApi = retrofit.create(PaymentApi.class);
        Call<ResponseBody> call = paymentApi.savePayment(AppConfig.API_KEY, aPackage.getPlanId(),
                databaseHelper.getUserData().getUserId(),
                aPackage.getPrice(),
                // "1",
                token,str_user_age, from);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.code() == 200) {

                    plantamount = aPackage.getPrice();
                    updateActiveStatus();

                    getSubscriptionHistory(plantamount);

                } else {
                    new ToastMsg(AgrrepayActivity.this).toastIconError(getString(R.string.something_went_wrong));
                    finish();
                    // progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                new ToastMsg(AgrrepayActivity.this).toastIconError(getString(R.string.something_went_wrong));
                finish();
                //progressBar.setVisibility(View.GONE);
            }
        });

    }


    private void updateActiveStatus() {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);

        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(AppConfig.API_KEY, PreferenceUtils.getUserId(AgrrepayActivity.this));
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(Call<ActiveStatus> call, retrofit2.Response<ActiveStatus> response) {
                if (response.code() == 200) {
                    ActiveStatus activeStatus = response.body();
                    ott.primeplay.database.DatabaseHelper db = new ott.primeplay.database.DatabaseHelper(getApplicationContext());
                    db.deleteAllActiveStatusData();
                    db.insertActiveStatusData(activeStatus);
                    new ToastMsg(AgrrepayActivity.this).toastIconSuccess(getResources().getString(R.string.payment_success));
                    // progressBar.setVisibility(View.GONE);
                    Intent intent = new Intent(AgrrepayActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }


            @Override
            public void onFailure(Call<ActiveStatus> call, Throwable t) {
                t.printStackTrace();
                new ToastMsg(AgrrepayActivity.this).toastIconError(getString(R.string.something_went_wrong));
                finish();
                // progressBar.setVisibility(View.GONE);
            }
        });

    }


    private void getSubscriptionHistory(String plantamount) {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);
        Call<SubscriptionHistory> call = subscriptionApi.getSubscriptionHistory(AppConfig.API_KEY, uid);
        call.enqueue(new Callback<SubscriptionHistory>() {
            @Override
            public void onResponse(Call<SubscriptionHistory> call, retrofit2.Response<SubscriptionHistory> response) {
                SubscriptionHistory subscriptionHistory = response.body();
                if (response.code() == 200) {

                    try {
                        if (subscriptionHistory.getActiveSubscription().size() > 0) {

                            HashMap<String, Object> paymentAction = new HashMap<String, Object>();
                            paymentAction.put("payment mode", "aggrepay");
                            paymentAction.put("amount", plantamount);
                            paymentAction.put("subscription plan", subscriptionHistory.getActiveSubscription().get(0).getPlanTitle());
                            paymentAction.put("Payment ID", subscriptionHistory.getActiveSubscription().get(0).getPaymentInfo());
                            paymentAction.put("Subscription ID", subscriptionHistory.getActiveSubscription().get(0).getSubscriptionId());
                            paymentAction.put("subscription plan id", subscriptionHistory.getActiveSubscription().get(0).getPlanId());
                            paymentAction.put("subscription Start date", subscriptionHistory.getActiveSubscription().get(0).getStartDate());
                            paymentAction.put("subscription End date", subscriptionHistory.getActiveSubscription().get(0).getExpireDate());
                            clevertapChergedInstance.pushEvent("Charged", paymentAction);

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<SubscriptionHistory> call, Throwable t) {
                // progressBar.setVisibility(View.GONE);
                t.printStackTrace();
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        pb.setVisibility(View.GONE);
    }
}
