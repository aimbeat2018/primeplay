package ott.primeplay.onepay;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import ott.primeplay.R;
import ott.primeplay.network.model.Package;

public class PaymentActivity extends AppCompatActivity {

    private WebView webView;
    private String transactionId;
    private String merchantId;



    private Package aPackage;
    private ott.primeplay.database.DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

       // Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        String jsonString = getIntent().getStringExtra("jsonString");
        String merchantId =  getIntent().getStringExtra("merchantId");
        String transactionId =  getIntent().getStringExtra("transactionId");



        if (getIntent() != null) {
            aPackage = (Package) getIntent().getSerializableExtra("package");
            databaseHelper = new ott.primeplay.database.DatabaseHelper(this);
        }


        this.merchantId = merchantId;
        this.transactionId = transactionId;

        webView = findViewById(R.id.webView);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.setWebViewClient(new CustomWebViewClient(this, this.merchantId, this.transactionId,aPackage));

        try {
            if(jsonString != null) {
                String encData = CommonFunctions.encryptText(jsonString);

                if(encData != null) {
                    Log.d("OnePayrequest", "Encrypted Data: " + encData);

                    String content = "<html>" +
                            "        <body onload=\"document.f.submit();\">" +
                            "          <form id=\"f\" name=\"f\" method=\"post\" action=\"" + Constants.PAYMENT_GATEWAY_URL + "\">" +
                            "            <input type=\"hidden\" name=\"merchantId\" value=\"" + merchantId +"\">" +
                            "            <input type=\"hidden\" name=\"reqData\" value=\"" + encData + "\">" +
                            "          </form>" +
                            "        </body>" +
                            "      </html>";

                    webView.loadData(content, "text/html", "UTF-8");
                }
            }
        } catch (Exception e) {

        }
    }
}