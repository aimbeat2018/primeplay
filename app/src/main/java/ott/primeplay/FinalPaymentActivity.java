package ott.primeplay;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.clevertap.android.sdk.CleverTapAPI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import okhttp3.ResponseBody;
import ott.primeplay.adapters.MyListAdapter;
import ott.primeplay.adapters.MyListData;
import ott.primeplay.adapters.PackageAdapter;
import ott.primeplay.network.RetrofitClient;
import ott.primeplay.database.DatabaseHelper;
import ott.primeplay.network.apis.PaymentApi;
import ott.primeplay.network.apis.PaymentGatewayApi;
import ott.primeplay.network.apis.SubscriptionApi;
import ott.primeplay.network.model.ActiveStatus;
import ott.primeplay.network.model.Package;
import ott.primeplay.network.model.SubscriptionHistory;
import ott.primeplay.network.model.User;
import ott.primeplay.onepay.Constants;
import ott.primeplay.onepay.PaymentActivity;
import ott.primeplay.utils.PreferenceUtils;
import ott.primeplay.utils.ToastMsg;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FinalPaymentActivity extends AppCompatActivity {

    TextView package_name,
            package_validity,
            price;
    CardView card_paytm,
            card_payuMoney,
            card_cashfree, card_razorpay, card_gpay, card_autoupi, card_oneupi, card_stripe, card_aggrepay, card_onepay;
    ImageView close_iv;
    private Package aPackage;
    CleverTapAPI clevertapPaymentStartedInstance, clevertapscreenviewd;

    PaymentSheet paymentSheet;
    String paymentIntentClientSecret;
    PaymentSheet.CustomerConfiguration customerConfig;

    String plantamount = "", strip_plan_amount = "";
    CleverTapAPI clevertapChergedInstance;

    String uid = "", uname = "", mobile = "", email = "", order_id = "", orderIdstr = "";
    private DatabaseHelper databaseHelper;
    MyListData[] myListData;
    Context context;

    private static final String GOOGLE_TEZ_PACKAGE_NAME = "com.google.android.apps.nbu.paisa.user";
    boolean isAppInstalled ;
    List pkgAppsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_payment);
        isAppInstalled = appInstalledOrNot(GOOGLE_TEZ_PACKAGE_NAME);


        clevertapChergedInstance = CleverTapAPI.getDefaultInstance(getApplicationContext());
        clevertapChergedInstance.setDebugLevel(CleverTapAPI.LogLevel.VERBOSE);

        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.VERBOSE);

        aPackage = (Package) getIntent().getSerializableExtra("package");
        databaseHelper = new DatabaseHelper(this);
        init();
        onClick();
        context = this;
        clevertapPaymentStartedInstance = CleverTapAPI.getDefaultInstance(getApplicationContext());
        clevertapscreenviewd = CleverTapAPI.getDefaultInstance(getApplicationContext());
        CleverTapAPI.setDebugLevel(CleverTapAPI.LogLevel.VERBOSE);

        clevertapPaymentStartedInstance = CleverTapAPI.getDefaultInstance(getApplicationContext());
        clevertapscreenviewd = CleverTapAPI.getDefaultInstance(getApplicationContext());

        User user = databaseHelper.getUserData();
        uid = user.getUserId();
        uname = user.getName();
//        mobile = "0000000000";
        mobile = user.getPhone();
        email = user.getEmail();

        strip_plan_amount = String.valueOf((Long.parseLong(aPackage.getPrice()) * 100));

        fetch_stripe_Payment_data(strip_plan_amount);

        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        final Dialog dialog = new Dialog(FinalPaymentActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.confirm_spinner_user_age_dialog);
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        //  Button btConfirm = dialog.findViewById(R.id.btConfirm);
        TextView txtCancel = dialog.findViewById(R.id.txtCancel);
        RecyclerView rec_age = dialog.findViewById(R.id.recyclerView);

//check upi app installed or not
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_DEFAULT);
        mainIntent.addCategory(Intent.CATEGORY_BROWSABLE);
        mainIntent.setAction(Intent.ACTION_VIEW);
        Uri uri1 = new Uri.Builder().scheme("upi").authority("pay").build();
        mainIntent.setData(uri1);
        pkgAppsList =
                context.getPackageManager().queryIntentActivities(mainIntent, 0);


        myListData = new MyListData[]{

                new MyListData("18"),
                new MyListData("19"),
                new MyListData("20"),
                new MyListData("21"),
                new MyListData("22"),
                new MyListData("23"),
                new MyListData("24"),
                new MyListData("25"),
                new MyListData("26"),
                new MyListData("27"),
                new MyListData("28"),

                new MyListData("29"),
                new MyListData("30"),
                new MyListData("31"),
                new MyListData("32"),
                new MyListData("33"),
                new MyListData("34"),
                new MyListData("35"),
                new MyListData("36"),
                new MyListData("37"),
                new MyListData("38"),
                new MyListData("39"),

                new MyListData("40"),
                new MyListData("41"),
                new MyListData("42"),
                new MyListData("42"),
                new MyListData("43"),
                new MyListData("44"),
                new MyListData("45"),
                new MyListData("46"),
                new MyListData("47"),
                new MyListData("48"),
                new MyListData("49"),


                new MyListData("50"),
                new MyListData("51"),
                new MyListData("52"),
                new MyListData("52"),
                new MyListData("53"),
                new MyListData("54"),
                new MyListData("55"),
                new MyListData("56"),
                new MyListData("57"),
                new MyListData("58"),
                new MyListData("59"),

                new MyListData("60"),
                new MyListData("61"),
                new MyListData("62"),
                new MyListData("62"),
                new MyListData("63"),
                new MyListData("64"),
                new MyListData("65"),
                new MyListData("66"),
                new MyListData("67"),
                new MyListData("68"),
                new MyListData("69"),

                new MyListData("70"),
                new MyListData("71"),
                new MyListData("72"),
                new MyListData("72"),
                new MyListData("73"),
                new MyListData("74"),
                new MyListData("75"),
                new MyListData("76"),
                new MyListData("77"),
                new MyListData("78"),
                new MyListData("79"),

                new MyListData("80"),
                new MyListData("81"),
                new MyListData("82"),
                new MyListData("82"),
                new MyListData("83"),
                new MyListData("84"),
                new MyListData("85"),
                new MyListData("86"),
                new MyListData("87"),
                new MyListData("88"),
                new MyListData("89"),

                new MyListData("90"),
                new MyListData("91"),
                new MyListData("92"),
                new MyListData("92"),
                new MyListData("93"),
                new MyListData("94"),
                new MyListData("95"),
                new MyListData("96"),
                new MyListData("97"),
                new MyListData("98"),
                new MyListData("99"),
                new MyListData("100"),
        };

        // MyListAdapter MyListAdapter = new MyListAdapter(myListData,packageList.get(getAdapterPosition()));
        MyListAdapter MyListAdapter = new MyListAdapter(myListData, context, dialog);
        rec_age.setHasFixedSize(true);
        rec_age.setLayoutManager(new LinearLayoutManager(FinalPaymentActivity.this));
        rec_age.setAdapter(MyListAdapter);


/*
        rec_age.addOnItemTouchListener(
                new PackageAdapter.RecyclerItemClickListener(FinalPaymentActivity.this, rec_age, new PackageAdapter.RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        // do whatever
                        if (itemClickListener != null) {
                            itemClickListener.onItemClick(packageList.get(getAdapterPosition()));
                            itemClickListener.onItemClick(packageList.get(getAdapterPosition()));

                        }


                        ott.primeplay.adapters.MyListAdapter.ViewHolder viewHolder = (ott.primeplay.adapters.MyListAdapter.ViewHolder) rec_age.getChildViewHolder(view);

                        View age = viewHolder.itemView.findViewById(position);

                        String agee = String.valueOf(age);

                    }


                })
        );*/


        txtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
//                finish();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);


    }


    private boolean appInstalledOrNot(String googleTezPackageName) {

        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(googleTezPackageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;

    }

    private void init() {
        package_name = findViewById(R.id.package_name);
        package_validity = findViewById(R.id.package_validity);
        price = findViewById(R.id.price);
        close_iv = findViewById(R.id.close_iv);
        card_paytm = findViewById(R.id.card_paytm);
        card_payuMoney = findViewById(R.id.card_payuMoney);
        card_cashfree = findViewById(R.id.card_cashfree);
        card_razorpay = findViewById(R.id.card_razorpay);
        card_gpay = findViewById(R.id.card_gpay);
        card_autoupi = findViewById(R.id.card_autoupi);
        card_oneupi = findViewById(R.id.card_oneupi);
        card_stripe = findViewById(R.id.card_stripe);
        card_aggrepay = findViewById(R.id.card_agrrepay);
        card_onepay = findViewById(R.id.card_onepay);

        package_name.setText(aPackage.getName());
        package_validity.setText(aPackage.getDay() + " Days");

        price.setText("\u20B9 " + aPackage.getPrice());
    }

    public void fetch_stripe_Payment_data(String strip_plan_amount) {
        //   RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "https://hunters.co.in/ppv1/rest-api/v130/stripe_payment";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                //  StringRequest stringRequest = new StringRequest(Request.Method.POST, url + "?amount=" + 100 + "&currency=" + "INR" + "&customer=" + 18,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("striperesponse", response);
                        try {

                            final JSONObject result = new JSONObject(response);

                            customerConfig = new PaymentSheet.CustomerConfiguration(
                                    result.getString("customer"),
                                    result.getString("ephemeralKey")
                            );

                            order_id = result.getString("customer");
                            paymentIntentClientSecret = result.getString("paymentIntent");
                            PaymentConfiguration.init(getApplicationContext(), result.getString("publishableKey"));

                            /* used secret key of dahsbord in APi-    sk_live_.......fgHl*/

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {


            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("API-KEY", AppConfig.API_KEY);
                // headers.put("API-KEY", "xm6CG4a0SC");
                return headers;
            }

            protected Map<String, String> getParams() {
                Map<String, String> paramV = new HashMap<>();
                //paramV.put("amount", plantamount);
                // paramV.put("amount", String.valueOf((Double.parseDouble(plantamount) * 1000)));
                //  paramV.put("amount", String.valueOf((Double.parseDouble(aPackage.getPrice()) * 1000)));
                //  paramV.put("amount", "20000");
                paramV.put("amount", strip_plan_amount);
                paramV.put("currency", "inr");
                paramV.put("customer", uid);

                return paramV;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        queue.add(stringRequest);
    }


    public void onClick() {
/*

        card_autoupi.setOnClickListener(view -> {

            Intent intent = new Intent(FinalPaymentActivity.this, AutoPaymentUpi.class);

            intent.putExtra("package", aPackage);
            intent.putExtra("currency", "currency");
            intent.putExtra("from", "cashfree");

            HashMap<String, Object> paymentstartedAction= new HashMap<String, Object>();
            paymentstartedAction.put("payment mode","Cash Free");
            paymentstartedAction.put("Selected Plan",aPackage.getName());
            paymentstartedAction.put("Amount",aPackage.getPrice());
            paymentstartedAction.put("Days",aPackage.getDay());

            startActivity(intent);

        });
*/


        card_aggrepay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(FinalPaymentActivity.this, AgrrepayActivity.class);
                intent.putExtra("package", aPackage);
                intent.putExtra("currency", "currency");
                intent.putExtra("from", "aggrepay");
                startActivity(intent);

                HashMap<String, Object> paymentstartedAction = new HashMap<String, Object>();
                paymentstartedAction.put("payment mode", "aggrepay");
                paymentstartedAction.put("Selected Plan", aPackage.getName());
                paymentstartedAction.put("Amount", aPackage.getPrice());
                paymentstartedAction.put("Days", aPackage.getDay());
                clevertapPaymentStartedInstance.pushEvent("Payment Started", paymentstartedAction);

                HashMap<String, Object> screenViewedAction = new HashMap<String, Object>();
                screenViewedAction.put("Screen Name", "AgrrepayActivity");
                clevertapscreenviewd.pushEvent("Screen Viewed", screenViewedAction);

            }
        });


        card_onepay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Random rnd = new Random();
                int trans_id = 100000 + rnd.nextInt(900000);
                String str_trn_id = Integer.toString(trans_id);

                Map<String, String> mapData = new HashMap<>();

                //  mapData.put("dateTime", "2023-10-02 18:16:29");
                mapData.put("custMail", email);
                mapData.put("custMobile", mobile);
                mapData.put("udf1", "NA");
                mapData.put("udf2", "NA");
                mapData.put("returnURL", Constants.PAYMENT_GATEWAY_RET_URL);
                mapData.put("productId", "DEFAULT");
                mapData.put("channelId", "0");
                mapData.put("isMultiSettlement", "0");
                mapData.put("txnType", "DIRECT");
                mapData.put("instrumentId", "NA");
                mapData.put("udf3", "NA");
                mapData.put("udf4", "NA");
                mapData.put("udf5", "NA");
                mapData.put("cardDetails", "NA");
                mapData.put("cardType", "NA");
                mapData.put("merchantId", Constants.merchantID);
                mapData.put("apiKey", Constants.secretKey);
                mapData.put("txnId", str_trn_id);
                mapData.put("amount", aPackage.getPrice() + ".00");

                LocalDateTime currentDateTime = null;
                DateTimeFormatter formatter = null;
                String formattedDateTime = null;

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    currentDateTime = LocalDateTime.now();
                    formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    formattedDateTime = currentDateTime.format(formatter);
                }
                if (formattedDateTime != null)
                    mapData.put("dateTime", formattedDateTime);

                // Log.d("OnePay", "Debug message");

                Gson gson = new GsonBuilder().serializeNulls().create();

                // Serialize the map to JSON
                String jsonString = gson.toJson(mapData);
                Log.d("OnePay", "JSON String: " + jsonString);

                Intent intent = new Intent(FinalPaymentActivity.this, PaymentActivity.class);
                intent.putExtra("jsonString", jsonString);
                intent.putExtra("merchantId", Constants.merchantID);
                intent.putExtra("transactionId", str_trn_id);

                intent.putExtra("package", aPackage);
                intent.putExtra("currency", "currency");
                intent.putExtra("from", "oneupi");

                startActivity(intent);

            }
        });


        card_stripe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (paymentIntentClientSecret != null) {

                    try {

                        paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret, new PaymentSheet.Configuration("Primeplay",
                                customerConfig));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                HashMap<String, Object> paymentstartedAction = new HashMap<String, Object>();
                paymentstartedAction.put("payment mode", "strip");
                paymentstartedAction.put("Selected Plan", aPackage.getName());
                paymentstartedAction.put("Amount", aPackage.getPrice());
                paymentstartedAction.put("Days", aPackage.getDay());
                clevertapPaymentStartedInstance.pushEvent("Payment Started", paymentstartedAction);

                HashMap<String, Object> screenViewedAction = new HashMap<String, Object>();
                screenViewedAction.put("Screen Name", "strippayment");
                clevertapscreenviewd.pushEvent("Screen Viewed", screenViewedAction);
            }
        });


        card_cashfree.setOnClickListener(view -> {
            Intent intent = new Intent(FinalPaymentActivity.this, CashFreePaymentActivity.class);
            intent.putExtra("package", aPackage);
            intent.putExtra("currency", "currency");
            intent.putExtra("from", "cashfree");

            HashMap<String, Object> paymentstartedAction = new HashMap<String, Object>();
            paymentstartedAction.put("payment mode", "Cash Free");
            paymentstartedAction.put("Selected Plan", aPackage.getName());
            paymentstartedAction.put("Amount", aPackage.getPrice());
            paymentstartedAction.put("Days", aPackage.getDay());

            clevertapPaymentStartedInstance.pushEvent("Payment Started", paymentstartedAction);

            HashMap<String, Object> screenViewedAction = new HashMap<String, Object>();
            screenViewedAction.put("Screen Name", "cashFreePayment");
            clevertapscreenviewd.pushEvent("Screen Viewed", screenViewedAction);

            startActivity(intent);

        });


        card_payuMoney.setOnClickListener(view -> {
            Intent intent = new Intent(FinalPaymentActivity.this, RazorPayActivity.class);
            intent.putExtra("package", aPackage);
            intent.putExtra("currency", "currency");
            intent.putExtra("from", "payu");
            startActivity(intent);

        });


        card_razorpay.setOnClickListener(view -> {
            Intent intent = new Intent(FinalPaymentActivity.this, RazorPayActivity.class);
            intent.putExtra("package", aPackage);
            intent.putExtra("currency", "currency");
            intent.putExtra("from", "razor");
            startActivity(intent);

        });

        card_oneupi.setOnClickListener(view -> {


            if (pkgAppsList.size() == 0) {

                Toast.makeText(this, "UPI App not installed on your device", Toast.LENGTH_SHORT).show();

            } else {


            Intent intent = new Intent(FinalPaymentActivity.this, OneUPIPaymentActivity.class);
            intent.putExtra("package", aPackage);
            intent.putExtra("currency", "currency");
            intent.putExtra("from", "oneupi");
            startActivity(intent);

            HashMap<String, Object> paymentstartedAction = new HashMap<String, Object>();
            paymentstartedAction.put("payment mode", "oneUPI");
            paymentstartedAction.put("Selected Plan", aPackage.getName());
            paymentstartedAction.put("Amount", aPackage.getPrice());
            paymentstartedAction.put("Days", aPackage.getDay());
            clevertapPaymentStartedInstance.pushEvent("Payment Started", paymentstartedAction);

            HashMap<String, Object> screenViewedAction = new HashMap<String, Object>();
            screenViewedAction.put("Screen Name", "oneUPIPayment");
            clevertapscreenviewd.pushEvent("Screen Viewed", screenViewedAction);

            }
        });


        card_gpay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isAppInstalled) {

                    Intent intent = new Intent(FinalPaymentActivity.this, RazorPayActivity.class);
                    intent.putExtra("package", aPackage);
                    intent.putExtra("currency", "currency");
                    intent.putExtra("from", "gpay");
                    startActivity(intent);

                }

                else {
                    Toast.makeText(FinalPaymentActivity.this, "Google Pay Application is not currently installed", Toast.LENGTH_SHORT).show();

                }
            }
        });
        close_iv.setOnClickListener(view -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPaymentGatewayStatus();
    }

    private void getPaymentGatewayStatus() {
        // dialog.show();
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        PaymentGatewayApi apiInterface = retrofit.create(PaymentGatewayApi.class);
        Call<ResponseBody> call = apiInterface.paymentgatway_status(AppConfig.API_KEY, "");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    try {
                        JSONObject jsonObject = new JSONObject(Objects.requireNonNull(response.body()).string());
                        String payUMoney = jsonObject.getString("Payu money");
                        String Paytm = jsonObject.getString("Paytm");
                        String cashfree = jsonObject.getString("cashfree");
                        String razorpay = jsonObject.getString("razorpay");
                        String gpay = jsonObject.getString("gap");
                        String oneupi = jsonObject.getString("oneupi");
                        String stripe = jsonObject.getString("stripe");
                        String aggrepay = jsonObject.getString("aggrepay");


                        if (payUMoney.equals("1")) {
                            card_payuMoney.setVisibility(View.VISIBLE);
                        } else {
                            card_payuMoney.setVisibility(View.GONE);
                        }

//                        if (Paytm.equals("1")) {
//                            card_paytm.setVisibility(View.VISIBLE);
//                        } else {
//                            card_paytm.setVisibility(View.GONE);
//                        }
                        if (cashfree.equals("1")) {
                            card_cashfree.setVisibility(View.VISIBLE);
                        } else {
                            card_cashfree.setVisibility(View.GONE);
                        }
                        if (razorpay.equals("1")) {
                            card_razorpay.setVisibility(View.VISIBLE);
                        } else {
                            card_razorpay.setVisibility(View.GONE);
                        }
                        if (gpay.equals("1")) {
                            card_gpay.setVisibility(View.VISIBLE);
                        } else {
                            card_gpay.setVisibility(View.GONE);
                        }
                        if (oneupi.equals("1")) {
                            card_oneupi.setVisibility(View.VISIBLE);
                        } else {
                            card_oneupi.setVisibility(View.GONE);
                        }

                        if (stripe.equals("1")) {
                            card_stripe.setVisibility(View.VISIBLE);
                        } else {
                            card_stripe.setVisibility(View.GONE);
                        }

                        if (aggrepay.equals("1")) {
                            card_aggrepay.setVisibility(View.VISIBLE);
                        } else {
                            card_aggrepay.setVisibility(View.GONE);
                        }

                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }


            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                new ToastMsg(FinalPaymentActivity.this).toastIconError("Something went wrong." + t.getMessage());
                // dialog.cancel();
                t.printStackTrace();
            }
        });
    }


    private void onPaymentSheetResult(final PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            Log.e("PaymentSheetResult:", "Canceled");
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            Log.e("PaymentSheetResult:", "Got error: ", ((PaymentSheetResult.Failed) paymentSheetResult).getError());
        } else if (paymentSheetResult instanceof PaymentSheetResult.Completed) {

            //  saveChargeData("","stripe");
            saveChargeData(order_id, "stripe");

            // Display for example, an order confirmation screen
            Log.e("PaymentSheetResult:", "Completed");

            Toast.makeText(FinalPaymentActivity.this, "Payment success", Toast.LENGTH_SHORT).show();

        }
    }


    public void saveChargeData(String token, String from) {
        //  progressBar.setVisibility(View.VISIBLE);
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        PaymentApi paymentApi = retrofit.create(PaymentApi.class);
        Call<ResponseBody> call = paymentApi.savePayment(AppConfig.API_KEY, aPackage.getPlanId(),
                databaseHelper.getUserData().getUserId(),
                aPackage.getPrice(),
                // "1",
                token, "35", from);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                if (response.code() == 200) {

                    plantamount = aPackage.getPrice();
                    updateActiveStatus();

                    getSubscriptionHistory(plantamount);

                } else {
                    new ToastMsg(FinalPaymentActivity.this).toastIconError(getString(R.string.something_went_wrong));
                    finish();
                    // progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                new ToastMsg(FinalPaymentActivity.this).toastIconError(getString(R.string.something_went_wrong));
                finish();
                //progressBar.setVisibility(View.GONE);
            }
        });

    }

    private void updateActiveStatus() {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);

        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(AppConfig.API_KEY, PreferenceUtils.getUserId(FinalPaymentActivity.this));
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(Call<ActiveStatus> call, retrofit2.Response<ActiveStatus> response) {
                if (response.code() == 200) {
                    ActiveStatus activeStatus = response.body();
                    DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                    db.deleteAllActiveStatusData();
                    db.insertActiveStatusData(activeStatus);
                    new ToastMsg(FinalPaymentActivity.this).toastIconSuccess(getResources().getString(R.string.payment_success));
                    // progressBar.setVisibility(View.GONE);
                    Intent intent = new Intent(FinalPaymentActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }


            @Override
            public void onFailure(Call<ActiveStatus> call, Throwable t) {
                t.printStackTrace();
                new ToastMsg(FinalPaymentActivity.this).toastIconError(getString(R.string.something_went_wrong));
                finish();
                //   progressBar.setVisibility(View.GONE);

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
                            paymentAction.put("payment mode", "stripe");
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
                //  progressBar.setVisibility(View.GONE);
                t.printStackTrace();
            }
        });

    }
}