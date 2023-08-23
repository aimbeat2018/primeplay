package ott.primeplay;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ott.primeplay.adapters.PackageAdapter;
import ott.primeplay.bottomshit.PaymentBottomShitDialog;
import ott.primeplay.database.DatabaseHelper;
import ott.primeplay.network.apis.PackageApi;
import ott.primeplay.network.apis.PaymentApi;
import ott.primeplay.network.apis.SubscriptionApi;
import ott.primeplay.network.model.ActiveStatus;
import ott.primeplay.network.model.AllPackage;
import ott.primeplay.network.model.Package;
import ott.primeplay.network.model.User;
import ott.primeplay.network.model.config.PaymentConfig;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cashfree.pg.api.CFPaymentGatewayService;
import com.cashfree.pg.core.api.CFSession;
import com.cashfree.pg.core.api.CFTheme;
import com.cashfree.pg.core.api.callback.CFCheckoutResponseCallback;
import com.cashfree.pg.core.api.exception.CFException;
import com.cashfree.pg.core.api.utils.CFErrorResponse;
import com.cashfree.pg.ui.api.CFDropCheckoutPayment;
import com.cashfree.pg.ui.api.CFPaymentComponent;
import com.clevertap.android.sdk.CleverTapAPI;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import ott.primeplay.R;
import ott.primeplay.network.RetrofitClient;
import ott.primeplay.utils.PreferenceUtils;
import ott.primeplay.utils.ApiResources;
import ott.primeplay.utils.RtlUtils;
import ott.primeplay.utils.ToastMsg;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PurchasePlanActivity extends AppCompatActivity implements PackageAdapter.OnItemClickListener/*, PaymentBottomShitDialog.OnBottomShitClickListener*/, CFCheckoutResponseCallback {

    private static final String TAG = PurchasePlanActivity.class.getSimpleName();
    private static final int PAYPAL_REQUEST_CODE = 100;
    private TextView noTv;
    private ProgressBar progressBar;
    private ImageView closeIv;
    private RecyclerView packageRv;
    private List<Package> packages = new ArrayList<>();
    private List<ImageView> imageViews = new ArrayList<>();
    private String currency = "";
    private String exchangeRate;
    private boolean isDark;

    private Package packageItem;
    private PaymentBottomShitDialog paymentBottomShitDialog;


    String orderID = "ORDER_ID";
    String token = "TOKEN";
    String order_token = "order_token";

    CFSession.Environment cfEnvironment = CFSession.Environment.PRODUCTION;


    String uid = "", uname = "", mobile = "", email = "", order_id = "", orderIdstr = "";
    static int min, max, create_otp;

    private DatabaseHelper databaseHelper;

    ProgressDialog dialog;

    CleverTapAPI clevertapscreenviewd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RtlUtils.setScreenDirection(this);
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        isDark = sharedPreferences.getBoolean("dark", false);


        clevertapscreenviewd = CleverTapAPI.getDefaultInstance(getApplicationContext());


        if (isDark) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }

        setContentView(R.layout.activity_purchase_plan);

        Log.e(TAG, "onCreate: payPal client id: " + ApiResources.PAYPAL_CLIENT_ID);

        initView();
        databaseHelper = new DatabaseHelper(this);

        User user = databaseHelper.getUserData();
        uid = user.getUserId();
        uname = user.getName();
        mobile = user.getPhone();
        email = user.getEmail();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait");
        dialog.setCancelable(false);
        // getting currency symbol
        PaymentConfig config = new DatabaseHelper(PurchasePlanActivity.this).getConfigurationData().getPaymentConfig();
        currency = config.getCurrencySymbol();
        exchangeRate = config.getExchangeRate();
        packageRv.setHasFixedSize(true);
        packageRv.setLayoutManager(new LinearLayoutManager(this));

        getPurchasePlanInfo();

        try {
            CFPaymentGatewayService.getInstance().setCheckoutCallback(this);
        } catch (CFException e) {
            e.printStackTrace();
        }
    }

    private void getPurchasePlanInfo() {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        final PackageApi packageApi = retrofit.create(PackageApi.class);
        Call<AllPackage> call = packageApi.getAllPackage(AppConfig.API_KEY);
        call.enqueue(new Callback<AllPackage>() {
            @Override
            public void onResponse(Call<AllPackage> call, Response<AllPackage> response) {
                AllPackage allPackage = response.body();
                packages = allPackage.getPackage();
                if (allPackage.getPackage().size() > 0) {
                    noTv.setVisibility(View.GONE);
                    PackageAdapter adapter = new PackageAdapter(PurchasePlanActivity.this, allPackage.getPackage(), currency);
                    adapter.setItemClickListener(PurchasePlanActivity.this);
                    packageRv.setAdapter(adapter);
                } else {
                    noTv.setVisibility(View.VISIBLE);
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<AllPackage> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                t.printStackTrace();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        closeIv.setOnClickListener(view -> finish());
    }

    private void completePayment(String paymentDetails) {
        try {
            JSONObject jsonObject = new JSONObject(paymentDetails);
            sendDataToServer(jsonObject.getJSONObject("response"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendDataToServer(JSONObject response) {
        try {
            String payId = response.getString("id");
            final String state = response.getString("state");
            final String userId = PreferenceUtils.getUserId(PurchasePlanActivity.this);

            Retrofit retrofit = RetrofitClient.getRetrofitInstance();
            PaymentApi paymentApi = retrofit.create(PaymentApi.class);
            Call<ResponseBody> call = paymentApi.savePayment(AppConfig.API_KEY, packageItem.getPlanId(), userId, packageItem.getPrice(),
                    payId, "Paypal");

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.code() == 200) {

                        updateActiveStatus(userId);

                    } else {
                        new ToastMsg(PurchasePlanActivity.this).toastIconError(getString(R.string.something_went_wrong));
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    new ToastMsg(PurchasePlanActivity.this).toastIconError(getString(R.string.something_went_wrong));
                    t.printStackTrace();
                    Log.e("PAYMENT", "error: " + t.getLocalizedMessage());
                }

            });

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void updateActiveStatus(String userId) {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);
        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(AppConfig.API_KEY, userId);
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(Call<ActiveStatus> call, Response<ActiveStatus> response) {
                if (response.code() == 200) {
                    ActiveStatus activiStatus = response.body();
                    saveActiveStatus(activiStatus);
                } else {
                    new ToastMsg(PurchasePlanActivity.this).toastIconError("Payment info not save to the own server. something went wrong.");
                }
            }

            @Override
            public void onFailure(Call<ActiveStatus> call, Throwable t) {
                new ToastMsg(PurchasePlanActivity.this).toastIconError(t.getMessage());
                t.printStackTrace();
            }
        });

    }



    private void saveActiveStatus(ActiveStatus activeStatus) {
        DatabaseHelper db = new DatabaseHelper(PurchasePlanActivity.this);
        if (db.getActiveStatusCount() > 1) {
            db.deleteAllActiveStatusData();
        }
        if (db.getActiveStatusCount() == 0) {
            db.insertActiveStatusData(activeStatus);
        } else {
            db.updateActiveStatus(activeStatus, 1);
        }
        new ToastMsg(PurchasePlanActivity.this).toastIconSuccess(getResources().getString(R.string.payment_success));

        Intent intent = new Intent(PurchasePlanActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void initView() {
        noTv = findViewById(R.id.no_tv);
        progressBar = findViewById(R.id.progress_bar);
        packageRv = findViewById(R.id.pacakge_rv);
        closeIv = findViewById(R.id.close_iv);
    }

    @Override
    public void onItemClick(Package pac) {
        packageItem = pac;

//        min = 111111;
//        max = 999999;
//        Random r = new Random();
//        create_otp = r.nextInt(max - min + 1) + min;
//        String order_id1 = String.valueOf(create_otp);
//        orderID = order_id1;
//        Log.d("order id", "" + order_id1);
//
//        getToken("", packageItem.getPrice());

/*
        paymentBottomShitDialog = new PaymentBottomShitDialog();
        paymentBottomShitDialog.show(getSupportFragmentManager(), "PaymentBottomShitDialog");*/


//        payment gateway comment code prime play..only below 4 line comment  ..2.8.2022
        Intent intent = new Intent(PurchasePlanActivity.this, FinalPaymentActivity.class);
        intent.putExtra("package", packageItem);
        intent.putExtra("currency", currency);

        HashMap<String, Object> screenViewedAction = new HashMap<String, Object>();
        screenViewedAction.put("Screen Name", "FinalPaymentActivity");
        clevertapscreenviewd.pushEvent("Screen Viewed", screenViewedAction);

        startActivity(intent);


//        Intent intent = new Intent(PurchasePlanActivity.this, CashFreePaymentActivity.class);
//        intent.putExtra("package", packageItem);
//        intent.putExtra("currency", "currency");
//        intent.putExtra("from", "cashfree");
//        startActivity(intent);
    }

   /* @Override
    public void onBottomShitClick(String paymentMethodName) {
        if (paymentMethodName.equals(PaymentBottomShitDialog.PAYPAL)) {
            processPaypalPayment(packageItem);

        } else if (paymentMethodName.equals(PaymentBottomShitDialog.STRIP)) {
            Intent intent = new Intent(PurchasePlanActivity.this, StripePaymentActivity.class);
            intent.putExtra("package", packageItem);
            intent.putExtra("currency", currency);
            startActivity(intent);

        } else if (paymentMethodName.equalsIgnoreCase(PaymentBottomShitDialog.RAZOR_PAY)) {
            Intent intent = new Intent(PurchasePlanActivity.this, RazorPayActivity.class);
            intent.putExtra("package", packageItem);
            intent.putExtra("currency", currency);
            startActivity(intent);
        } else if (paymentMethodName.equalsIgnoreCase(PaymentBottomShitDialog.OFFLINE_PAY)) {
            //show an alert dialog
            showOfflinePaymentDialog();
        }
    }*/

    private void showOfflinePaymentDialog() {
        DatabaseHelper helper = new DatabaseHelper(this);
        PaymentConfig paymentConfig = helper.getConfigurationData().getPaymentConfig();
        new MaterialAlertDialogBuilder(this)
                .setTitle(paymentConfig.getOfflinePaymentTitle())
                .setMessage(paymentConfig.getOfflinePaymentInstruction())
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();

    }


    public void getToken(final String orderId, final String amount) {
        dialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, /*"https://primeplay.co.in/webworld_backoffice/rest-api/v130/cashfree"*/"https://primeplay.co.in/webworld_backoffice/rest-api/v130/cashfree_test"/*"https://test.cashfree.com/api/v2/cftoken/order"*/, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
//                    cf_order_id = jsonObject.getString("cf_order_id");

                    if (jsonObject.has("message")) {
                        Toast.makeText(PurchasePlanActivity.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    } else {
                        if (jsonObject.has("order_id"))
                            order_id = jsonObject.getString("order_id");

                        if (jsonObject.has("order_token"))
                            order_token = jsonObject.getString("order_token");
//                    order_status = jsonObject.getString("order_status");
                        token = order_token;

                        doDropCheckoutPayment();
                    }
//                    Log.d("orderId", "" + orderId);
                    Log.d("cashfreeToken", "" + jsonObject);
                    Log.d("orderId", "" + order_id);

                    dialog.dismiss();
                } catch (JSONException e) {
                    Toast.makeText(PurchasePlanActivity.this, "json exception" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();

                    dialog.dismiss();
                }
            }
        }, error -> {
            Toast.makeText(PurchasePlanActivity.this, "json error exception" + error.getMessage(), Toast.LENGTH_SHORT).show();
            error.printStackTrace();

            dialog.dismiss();
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                return params;
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                JSONObject jsonObject = new JSONObject();
                final String requestBody;

                try {
                    min = 111111;
                    max = 999999;
                    Random r = new Random();
                    create_otp = r.nextInt(max - min + 1) + min;

//                    jsonObject.put("order_id", String.valueOf(create_otp));
                    jsonObject.put("order_amount", amount);
                    jsonObject.put("order_currency", "INR");
                    JSONObject customerObject = new JSONObject();
                    customerObject.put("customer_id", uid);
                    customerObject.put("customer_name", uname);
                    customerObject.put("customer_email", email);
                    customerObject.put("customer_phone", mobile);
                    jsonObject.put("customer_details", customerObject);
                    requestBody = jsonObject.toString();

                    Log.d("object", "" + requestBody);

                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (JSONException | UnsupportedEncodingException e) {
//                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }

            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("API-KEY", AppConfig.API_KEY);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(100000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    public void doDropCheckoutPayment() {
        if (order_id.equals("") || TextUtils.isEmpty(order_id)) {
            Toast.makeText(this, "Please set the orderId (DropCheckoutActivity.class,  line: 22)", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (token.equals("TOKEN") || TextUtils.isEmpty(token)) {
            Toast.makeText(this, "Please set the token (DropCheckoutActivity.class,  line: 23)", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        try {
            CFSession cfSession = new CFSession.CFSessionBuilder()
                    .setEnvironment(cfEnvironment)
                    .setOrderToken(token)
                    .setOrderId(order_id)
                    .build();
            CFPaymentComponent cfPaymentComponent = new CFPaymentComponent.CFPaymentComponentBuilder()
                    .add(CFPaymentComponent.CFPaymentModes.CARD)
                    .add(CFPaymentComponent.CFPaymentModes.UPI)
                    .build();
            CFTheme cfTheme = new CFTheme.CFThemeBuilder()
                    .setNavigationBarBackgroundColor("#0d2366")
                    .setNavigationBarTextColor("#ffffff")
                    .setButtonBackgroundColor("#0d2366")
                    .setButtonTextColor("#ffffff")
                    .setPrimaryTextColor("#000000")
                    .setSecondaryTextColor("#000000")
                    .build();
            CFDropCheckoutPayment cfDropCheckoutPayment = new CFDropCheckoutPayment.CFDropCheckoutPaymentBuilder()
                    .setSession(cfSession)
                    //By default all modes are enabled. If you want to restrict the payment modes uncomment the next line
//                        .setCFUIPaymentModes(cfPaymentComponent)
                    .setCFNativeCheckoutUITheme(cfTheme)
                    .build();
            CFPaymentGatewayService gatewayService = CFPaymentGatewayService.getInstance();
            Log.d("cashfreedata", "" + cfDropCheckoutPayment);
            gatewayService.doPayment(PurchasePlanActivity.this, cfDropCheckoutPayment);
        } catch (CFException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void onPaymentVerify(String s) {
        saveChargeData(order_id, "cashfree");
    }

    @Override
    public void onPaymentFailure(CFErrorResponse cfErrorResponse, String s) {
//        order_id = "";
        Toast.makeText(this, cfErrorResponse.getMessage(), Toast.LENGTH_SHORT).show();
    }

    public void saveChargeData(String token, String from) {
//        progressBar.setVisibility(View.VISIBLE);
        dialog.show();
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        PaymentApi paymentApi = retrofit.create(PaymentApi.class);
        Call<ResponseBody> call = paymentApi.savePayment(AppConfig.API_KEY, packageItem.getPlanId(),
                databaseHelper.getUserData().getUserId(),
                packageItem.getPrice(),
                token, from);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.code() == 200) {
                    updateActiveStatus();

                } else {
                    Toast.makeText(PurchasePlanActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
//                    new ToastMsg(PurchasePlanActivity.this).toastIconError(getString(R.string.something_went_wrong));
                    finish();
//                    progressBar.setVisibility(View.GONE);
                }

                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                dialog.dismiss();
                Toast.makeText(PurchasePlanActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
//                new ToastMsg(PurchasePlanActivity.this).toastIconError(getString(R.string.something_went_wrong));
                finish();
//                progressBar.setVisibility(View.GONE);
            }
        });


    }

    private void updateActiveStatus() {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);

        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(AppConfig.API_KEY, PreferenceUtils.getUserId(PurchasePlanActivity.this));
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(Call<ActiveStatus> call, retrofit2.Response<ActiveStatus> response) {
                if (response.code() == 200) {
                    ActiveStatus activeStatus = response.body();
                    DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                    db.deleteAllActiveStatusData();
                    db.insertActiveStatusData(activeStatus);
                    Toast.makeText(PurchasePlanActivity.this, getResources().getString(R.string.payment_success), Toast.LENGTH_SHORT).show();
//                    new ToastMsg(PurchasePlanActivity.this).toastIconSuccess(getResources().getString(R.string.payment_success));
//                    progressBar.setVisibility(View.GONE);
                    Intent intent = new Intent(PurchasePlanActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
//                    order_id = "";
                }
            }


            @Override
            public void onFailure(Call<ActiveStatus> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(PurchasePlanActivity.this, getResources().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
                finish();
//                progressBar.setVisibility(View.GONE);
            }
        });

    }
}

