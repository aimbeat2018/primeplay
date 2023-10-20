package ott.primeplay;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;
import static ott.primeplay.MoreActivity.familycontent;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.appsflyer.AppsFlyerLib;
import com.appsflyer.attribution.AppsFlyerRequestListener;
import com.bumptech.glide.Glide;
//import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.CleverTapAPI;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import io.branch.referral.Branch;
import okhttp3.ResponseBody;
import ott.primeplay.adapters.NavigationAdapter;
import ott.primeplay.database.DatabaseHelper;
import ott.primeplay.firebaseservice.Config;
import ott.primeplay.models.NavigationModel;
import ott.primeplay.nav_fragments.MainHomeFragment;
import ott.primeplay.network.RetrofitClient;
import ott.primeplay.network.apis.LoginApi;
import ott.primeplay.network.apis.OrderstatusResponse;
import ott.primeplay.network.apis.PassResetApi;
import ott.primeplay.network.apis.PaymentApi;
import ott.primeplay.network.apis.SubscriptionApi;
import ott.primeplay.network.apis.UserDataApi;
import ott.primeplay.network.model.ActiveStatus;
import ott.primeplay.network.model.User;
import ott.primeplay.reels.ReelsListActivity;
import ott.primeplay.utils.Constants;
import ott.primeplay.utils.HelperUtils;
import ott.primeplay.utils.PreferenceUtils;
import ott.primeplay.utils.RtlUtils;
import ott.primeplay.utils.ToastMsg;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import com.appsflyer.AppsFlyerLib;
import com.appsflyer.AppsFlyerLib;
import com.appsflyer.AFInAppEventType; // Predefined event names
import com.appsflyer.AFInAppEventParameterName; // Predefined parameter names


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Serializable {

    private FirebaseAnalytics mFirebaseAnalytics;
    public boolean isDark;
    private final int PERMISSION_REQUEST_CODE = 100;
    private DatabaseHelper db;
    private boolean vpnStatus;
    private HelperUtils helperUtils;
    private final Handler handler = new Handler();
    Runnable mToastRunnable;
    String userProfileStatus = "";

    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private static final String VERSION_CODE_KEY = "version_code";
    private AlertDialog updateDailog;
    AppsFlyerRequestListener appsFlyerRequestListener;
    CleverTapAPI clevertapDefaultInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RtlUtils.setScreenDirection(this);

        AppsFlyerLib.getInstance().init("bn7QHQurvKuoUEydf57E78", null, this);
        AppsFlyerLib.getInstance().waitForCustomerUserId(true);
        AppsFlyerLib.getInstance().setCustomerUserId(PreferenceUtils.getUserId(MainActivity.this));

        AppsFlyerLib.getInstance().start(this);
        AppsFlyerLib.getInstance().setDebugLog(true);
        AppsFlyerLib.getInstance().setDebugLog(true);


        AppsFlyerLib.getInstance().start(getApplicationContext(), "bn7QHQurvKuoUEydf57E78", new AppsFlyerRequestListener() {
            @Override
            public void onSuccess() {
                Log.d("APPSFLYER", "Launch sent successfully, got 200 response code from server");
            }

            @Override
            public void onError(int i, @NonNull String s) {
                Log.d("APPSFLYER", "Launch failed to be sent:\n" +
                        "Error code: " + i + "\n"
                        + "Error description: " + s);
            }
        });


//        Map<String, Object> eventValues = new HashMap<String, Object>();
//        eventValues.put(AFInAppEventParameterName.PRICE, 1234.56);
//        eventValues.put(AFInAppEventParameterName.CONTENT_ID,"1234567");
//
//        AppsFlyerLib.getInstance().logEvent(getApplicationContext(),
//                AFInAppEventType.ADD_TO_WISHLIST , eventValues);


        db = new DatabaseHelper(MainActivity.this);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        CleverTapAPI clevertapDefaultInstance = CleverTapAPI.getDefaultInstance(getApplicationContext());

        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        isDark = sharedPreferences.getBoolean("dark", false);
        if (isDark) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

//to  get user  city location
        clevertapDefaultInstance = CleverTapAPI.getDefaultInstance(getApplicationContext());
        clevertapDefaultInstance.enableDeviceNetworkInfoReporting(true);

        //check vpn connection
        helperUtils = new HelperUtils(MainActivity.this);
        vpnStatus = helperUtils.isVpnConnectionAvailable();
        if (vpnStatus) {
            helperUtils.showWarningDialog(MainActivity.this, getString(R.string.vpn_detected), getString(R.string.close_vpn));
            return;
        }
        // To resolve cast button visibility problem. Check Cast State when app is open.
        CastContext castContext = CastContext.getSharedInstance(this);
        castContext.getCastState();

//        navMenuStyle = db.getConfigurationData().getAppConfig().getMenu();

        //---analytics-----------
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "main_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        if (sharedPreferences.getBoolean("firstTime", true)) {
            showTermServicesDialog();
        }

        //update subscription
        if (PreferenceUtils.isLoggedIn(MainActivity.this)) {
            PreferenceUtils.updateSubscriptionStatus(MainActivity.this);
        }

//        createDownloadDir();

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkNotificationPermission();
        }

        if (!PreferenceUtils.okClicked) {
            getImage("");
        }
        //----external method call--------------
        loadFragment(new MainHomeFragment());
    }

    private boolean checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED/*
                    && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED*/) {

//                Log.v(TAG, "Permission is granted");
                return true;

            } else {
//                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS/*, Manifest.permission.READ_EXTERNAL_STORAGE*/}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
//            Log.v(TAG, "Permission is granted");
            return true;
        }
    }


    private void initRemoteConfig() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        HashMap<String, Object> firebaseDefaultMap = new HashMap<>();
        firebaseDefaultMap.put(VERSION_CODE_KEY, getCurrentVersionCode());
        mFirebaseRemoteConfig.setDefaultsAsync(firebaseDefaultMap);
        mFirebaseRemoteConfig.setConfigSettingsAsync(
                new FirebaseRemoteConfigSettings.Builder()
                        .setMinimumFetchIntervalInSeconds(TimeUnit.HOURS.toSeconds(0))
                        .build());
        mFirebaseRemoteConfig.fetch().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mFirebaseRemoteConfig.activate().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        final int latestAppVersion = (int) mFirebaseRemoteConfig.getDouble(VERSION_CODE_KEY);
                        runOnUiThread(() ->
                                checkForUpdate(latestAppVersion));
                    }
                });
            }
        });

    }

    private int getCurrentVersionCode() {
        int versionCode = 1;
        try {
            final PackageInfo pInfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                versionCode = (int) pInfo.getLongVersionCode();
            } else {
                versionCode = pInfo.versionCode;
            }
        } catch (PackageManager.NameNotFoundException e) {
            //log exception
        }
        return versionCode;
    }

    private void goToPlayStore() {
        try {
            Intent appStoreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName()));
            appStoreIntent.setPackage("com.android.vending");
            startActivity(appStoreIntent);
        } catch (android.content.ActivityNotFoundException exception) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    private void checkForUpdate(int latestAppVersion) {
        int version = getCurrentVersionCode();
        if (latestAppVersion > version) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Update App");
            builder.setMessage("New Version Available On Play store.\n" + "Please Update Your App");
            builder.setPositiveButton("Update", (dialog, which) -> {
                goToPlayStore();
                updateDailog.dismiss();
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> {
                updateDailog.dismiss();
                finishAffinity();
            });
            // create and show the alert dialog
            updateDailog = builder.create();
            updateDailog.setCancelable(false);
            updateDailog.show();

        }
    }

    private boolean loadFragment(Fragment fragment) {

        if (fragment != null) {

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();

            return true;
        }
        return false;

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_search:

                final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {

                        Intent intent = new Intent(MainActivity.this, SearchResultActivity.class);
                        intent.putExtra("q", s);
                        startActivity(intent);

                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        return false;
                    }
                });

                return true;

        }
        return super.onOptionsItemSelected(item);
    }

//    public void openDrawer() {
//        mDrawerLayout.openDrawer(GravityCompat.START);
//    }

    @Override
    public void onBackPressed() {

        /*if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        } else {*/
        new AlertDialog.Builder(MainActivity.this).setMessage("Do you want to exit ?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finishAffinity();
//                        System.exit(0);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create().show();

//        }
    }

    //----nav menu item click---------------
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        // set item as selected to persist highlight
        menuItem.setChecked(true);
//        mDrawerLayout.closeDrawers();
        return true;
    }

    private void showTermServicesDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_term_of_services);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;

        WebView webView = dialog.findViewById(R.id.webView);
        Button declineBt = dialog.findViewById(R.id.bt_decline);
        Button acceptBt = dialog.findViewById(R.id.bt_accept);
        //populate webView with data
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return false;
            }
        });
        webView.loadUrl(AppConfig.TERMS_URL);

        if (isDark) {
            declineBt.setBackground(getResources().getDrawable(R.drawable.btn_rounded_grey_outline));
            acceptBt.setBackground(getResources().getDrawable(R.drawable.btn_rounded_dark));
        }

        ((ImageButton) dialog.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });

        acceptBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = getSharedPreferences("push", MODE_PRIVATE).edit();
                editor.putBoolean("firstTime", false);
                editor.apply();
                dialog.dismiss();

                getUserProfileData(PreferenceUtils.getUserId(MainActivity.this));

            }
        });

        declineBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }


    // ------------------ checking storage permission ------------
    private boolean checkStoragePermission() {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");

                    // creating the download directory named oxoo
//                    createDownloadDir();

                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }

    }

    public void goToSearchActivity() {
        startActivity(new Intent(MainActivity.this, SearchActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();

//        order_status_details(PreferenceUtils.getUserId(MainActivity.this));


        //check vpn connection
        helperUtils = new HelperUtils(MainActivity.this);
        vpnStatus = helperUtils.isVpnConnectionAvailable();
        if (vpnStatus) {
            helperUtils.showWarningDialog(MainActivity.this, getString(R.string.vpn_detected), getString(R.string.close_vpn));
        }

        initRemoteConfig();


        if (PreferenceUtils.getUserId(MainActivity.this) == null || PreferenceUtils.getUserId(MainActivity.this).equals("")) {
//            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//            if (user != null) {
//                FirebaseAuth.getInstance().signOut();
//            }
//
//            SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
//            editor.putBoolean(Constants.USER_LOGIN_STATUS, false);
//            editor.apply();
//            editor.commit();
//
//            DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this);
//            databaseHelper.deleteUserData();
//
//            PreferenceUtils.clearSubscriptionSavedData(MainActivity.this);
//
//            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
//            startActivity(intent);
//            finish();
        } else {
            getProfile(PreferenceUtils.getUserId(MainActivity.this));
        }

    }


    private void doTheAutoRefresh() {
       /* handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Write code for your refresh logic
                getProfile();
            }
        }, 3000);*/
        mToastRunnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 3000);
                getProfile(PreferenceUtils.getUserId(MainActivity.this));
            }
        };
        mToastRunnable.run();

    }

    /*public final Runnable runnable = new Runnable() {

        @Override
        public void run() {
            // your code while refreshing activity
//            getProfile();
            Toast.makeText(MainActivity.this, "Refreshing data", Toast.LENGTH_SHORT).show();
        }
    };
*/

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        handler.removeCallbacks(mToastRunnable);
//    }

    private void getProfile(String uid) {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        UserDataApi api = retrofit.create(UserDataApi.class);
        Call<User> call = api.getUserData(AppConfig.API_KEY, uid);
        call.enqueue(new Callback<User>() {

            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200) {

                    try{

                        if (response.body() != null) {
                            User user = response.body();
                            if (user != null)
                                if (user.getLogout_status().equals("1")) {
                                    String deviceNoDynamic = user.getDevice_no();
                                    String deviceNo = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                                    if (deviceNoDynamic != null) {
                                        if (!deviceNoDynamic.equals("")) {
                                            if (!deviceNo.equals(deviceNoDynamic)) {
                                                Toast.makeText(MainActivity.this, "Logged in other device", Toast.LENGTH_SHORT).show();
                                                logoutUser(uid);
                                            }
                                        }
                                    }
                                } else {
                                    logoutUser(uid);
                                }
                        }

                    }catch (Exception e){e.printStackTrace();}

                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
            }
        });

    }

    private void getImage(String uid) {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        UserDataApi api = retrofit.create(UserDataApi.class);
        Call<ResponseBody> call = api.getImage(AppConfig.API_KEY, "");
        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        try {
                            JSONObject jsonObject = new JSONObject(Objects.requireNonNull(response.body()).string());
                            String imageUrl = jsonObject.getString("image_url");
                            if (!imageUrl.equals("")) {
                                newUpdateDialog(imageUrl);
                            }
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });

    }

    public void newUpdateDialog(String image) {

        SharedPreferences sharedPreferences = getSharedPreferences(Constants.FAMILYCONTENTSTATUS, MODE_PRIVATE);
        familycontent = sharedPreferences.getBoolean("familycontent", false);

        if (familycontent == false) {

            final Dialog dialog = new Dialog(MainActivity.this);
            dialog.setContentView(R.layout.new_update_dialog_layout);
            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);
            Window window = dialog.getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setDimAmount(0.3F);

            AppCompatImageView img_image = dialog.findViewById(R.id.img_image);
            Glide.with(MainActivity.this).load(image).into(img_image);

            CardView card_cancel = dialog.findViewById(R.id.card_cancel);
            card_cancel.setOnClickListener(v -> {
                dialog.dismiss();

                PreferenceUtils.okClicked = true;
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

                String currentDate = sdf.format(new Date());
                SharedPreferences sharedPref = getSharedPreferences("dialog", 0);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("LAST_LAUNCH_DATE", currentDate);
                editor.commit();
            });

            dialog.show();
        }


    }

    private void logoutUser(String id) {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        LoginApi api = retrofit.create(LoginApi.class);
        Call<User> call = api.postLogoutStatus(AppConfig.API_KEY, id);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200) {
                    assert response.body() != null;
                    if (response.body().getStatus().equalsIgnoreCase("success")) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            FirebaseAuth.getInstance().signOut();
                        }

                        SharedPreferences.Editor editor = getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
                        editor.putBoolean(Constants.USER_LOGIN_STATUS, false);
                        editor.apply();
                        editor.commit();

                        DatabaseHelper databaseHelper = new DatabaseHelper(MainActivity.this);
                        databaseHelper.deleteUserData();

                        PreferenceUtils.clearSubscriptionSavedData(MainActivity.this);

                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        new ToastMsg(MainActivity.this).toastIconError(response.body().getData());

                    }
                } else {

                    new ToastMsg(MainActivity.this).toastIconError(getString(R.string.error_toast));
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {

                new ToastMsg(MainActivity.this).toastIconError(getString(R.string.error_toast));
            }
        });
    }

    private void getUserProfileData(String uid) {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        UserDataApi api = retrofit.create(UserDataApi.class);
        Call<User> call = api.getUserData(AppConfig.API_KEY, uid);
        call.enqueue(new Callback<User>() {

            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.code() == 200) {
                    if (response.body() != null) {
                        User user = response.body();
                        userProfileStatus = user.getProfile_status();

                        if (userProfileStatus != null)
                            if (userProfileStatus.equals("0")) {
                                Toast.makeText(MainActivity.this, "Verify your mobile number for better service.", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                                intent.putExtra("from", "main");
                                startActivity(intent);

//                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
////                            builder.setMessage("Update your profile for better service.")
//                                builder.setMessage("Verify your mobile number for better service.")
//                                        .setCancelable(false)
//                                        .setNegativeButton("Cancel", (dialog, id) -> {
//                                            finishAffinity();
//                                            dialog.dismiss();
//                                        })
//                                        .setPositiveButton("OK", (dialog1, id) -> {
//                                            dialog1.dismiss();
//                                            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
//                                            intent.putExtra("from", "main");
//                                            startActivity(intent);
//                                        });
//                                AlertDialog alert = builder.create();
//                                alert.show();
                            }
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
            }
        });


    }
}
