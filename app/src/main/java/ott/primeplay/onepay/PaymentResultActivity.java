package ott.primeplay.onepay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.clevertap.android.sdk.CleverTapAPI;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import okhttp3.ResponseBody;
import ott.primeplay.AppConfig;
import ott.primeplay.FinalPaymentActivity;
import ott.primeplay.MainActivity;
import ott.primeplay.OneUPIPaymentActivity;
import ott.primeplay.R;
import ott.primeplay.network.RetrofitClient;
import ott.primeplay.network.apis.PaymentApi;
import ott.primeplay.network.apis.SubscriptionApi;
import ott.primeplay.network.model.ActiveStatus;
import ott.primeplay.network.model.Package;
import ott.primeplay.network.model.SubscriptionHistory;
import ott.primeplay.network.model.User;
import ott.primeplay.utils.PreferenceUtils;
import ott.primeplay.utils.ToastMsg;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class PaymentResultActivity extends AppCompatActivity {

    private String transactionId;
    private String merchantId;
   // private TextView logText;
  //  private TableLayout tableLayout;
    String payment_status = "";
ProgressBar progressBar;
    String uid = "", uname = "", mobile = "", email = "", order_id = "", orderIdstr = "";
    String plantamount = "";
    private Package aPackage;
    private ott.primeplay.database.DatabaseHelper databaseHelper;

    CleverTapAPI clevertapChergedInstance;

    TextView txt_txn_id,
            txt_falied_reason;
    LinearLayout lnr_success,
            lnr_failed;

    String str_user_age="";
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);

        lnr_success = findViewById(R.id.lnr_success);
        lnr_failed = findViewById(R.id.lnr_failed);
        txt_txn_id = findViewById(R.id.txt_txn_id);
        txt_falied_reason = findViewById(R.id.txt_falied_reason);
        progressBar = findViewById(R.id.progressBar);


        clevertapChergedInstance = CleverTapAPI.getDefaultInstance(getApplicationContext());
        clevertapChergedInstance.setDebugLevel(CleverTapAPI.LogLevel.VERBOSE);

        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.VERBOSE);

        try {
            //  Block of code to try
            SharedPreferences sharedPreferences = PaymentResultActivity.this.getSharedPreferences(ott.primeplay.utils.Constants.USER_AGE, MODE_PRIVATE);
            str_user_age = sharedPreferences.getString("user_age", "20");

        }
        catch(Exception e) {
            e.printStackTrace();
        }

//        Toolbar toolbar = findViewById(R.id.toolbar);
        //   setSupportActionBar(toolbar);

        if (getIntent() != null) {
            aPackage = (Package) getIntent().getSerializableExtra("package");
            databaseHelper = new ott.primeplay.database.DatabaseHelper(this);
        }


        User user = databaseHelper.getUserData();
        uid = user.getUserId();
        uname = user.getName();
//        mobile = "0000000000";
        mobile = user.getPhone();
        email = user.getEmail();
       // logText = findViewById(R.id.lbl_txn_details_log);
      //  tableLayout = findViewById(R.id.tableLayout);


        String merchantId = getIntent().getStringExtra("merchantId");
        String transactionId = getIntent().getStringExtra("transactionId");

        this.merchantId = merchantId;
        this.transactionId = transactionId;

        String parameters = "merchantId=" + merchantId + "&txnId=" + transactionId;

        fetchDataFromUrl(Constants.PAYMENT_GATEWAY_RES_URL, parameters);

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void fetchDataFromUrl(String urlString, String parameters) {
        CompletableFuture.supplyAsync(() -> {
            try {
                return downloadUrl(urlString, parameters);
            } catch (IOException e) {
                return "Error: " + e.getMessage();
            }
        }).thenAccept(result -> runOnUiThread(() -> {
            Log.d("OnePayewsponse", "code :: " + result);

          //  this.logText.setText(result);

            Map<String, String> resultMap = jsonToMap(result);

            payment_status = resultMap.get("trans_status");


            if (payment_status.equals("Ok")) {

                lnr_failed.setVisibility(View.GONE);
                lnr_success.setVisibility(View.VISIBLE);
                txt_txn_id.setText("Transaction Id : " + transactionId);

                saveChargeData(transactionId, "onepay");
                Toast.makeText(PaymentResultActivity.this, "Payment success", Toast.LENGTH_SHORT).show();
            } else {

                lnr_failed.setVisibility(View.VISIBLE);
                lnr_success.setVisibility(View.GONE);
                Toast.makeText(PaymentResultActivity.this, "Payment failure", Toast.LENGTH_SHORT).show();

            }

            //  Log.d("trn_result", "trn_result :: " + res);

            // Print the result
            for (Map.Entry<String, String> entry : resultMap.entrySet()) {
                // System.out.println(entry.getKey() + " : " + entry.getValue());

              //  addDataRow(tableLayout, entry.getKey(), entry.getValue());

            }

        }));
    }



    private static Map<String, String> jsonToMap(String jsonString) {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        return gson.fromJson(jsonString, type);
    }

    private String downloadUrl(String urlString, String parameters) throws IOException {
        try (InputStream is = openConnection(urlString, parameters).getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
            return stringBuilder.toString();
        }
    }



    private HttpURLConnection openConnection(String urlString, String parameters) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);

        // Write the parameters to the request body
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = parameters.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Starts the query
        conn.connect();
        return conn;
    }



    public void saveChargeData(String token, String from) {
          progressBar.setVisibility(View.VISIBLE);
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
                    new ToastMsg(PaymentResultActivity.this).toastIconError(getString(R.string.something_went_wrong));
                    finish();
                     progressBar.setVisibility(View.GONE);
                }
            }


            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                new ToastMsg(PaymentResultActivity.this).toastIconError(getString(R.string.something_went_wrong));
                finish();
                  progressBar.setVisibility(View.GONE);
            }
        });

    }




    private void updateActiveStatus() {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);

        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(AppConfig.API_KEY, PreferenceUtils.getUserId(PaymentResultActivity.this));
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(Call<ActiveStatus> call, retrofit2.Response<ActiveStatus> response) {
                if (response.code() == 200) {
                    ActiveStatus activeStatus = response.body();
                    ott.primeplay.database.DatabaseHelper db = new ott.primeplay.database.DatabaseHelper(getApplicationContext());
                    db.deleteAllActiveStatusData();
                    db.insertActiveStatusData(activeStatus);
                    new ToastMsg(PaymentResultActivity.this).toastIconSuccess(getResources().getString(R.string.payment_success));
                     progressBar.setVisibility(View.GONE);
                    Intent intent = new Intent(PaymentResultActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }


            @Override
            public void onFailure(Call<ActiveStatus> call, Throwable t) {
                t.printStackTrace();
                new ToastMsg(PaymentResultActivity.this).toastIconError(getString(R.string.something_went_wrong));
                finish();
                progressBar.setVisibility(View.GONE);
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
                            paymentAction.put("payment mode", "onepay");
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


    private void addHeaderRow(TableLayout tableLayout, String header1, String header2) {
        TableRow headerRow = new TableRow(this);

        addTextViewToRow(headerRow, header1);
        addTextViewToRow(headerRow, header2);

        tableLayout.addView(headerRow);
    }

    private void addDataRow(TableLayout tableLayout, String data1, String data2) {
        TableRow dataRow = new TableRow(this);

        addTextViewToRow(dataRow, data1);
        addTextViewToRow(dataRow, data2);

        tableLayout.addView(dataRow);
    }

    private void addTextViewToRow(TableRow row, String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        row.addView(textView);
    }
}