package ott.primeplay.onepay;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import ott.primeplay.network.model.Package;

public class CustomWebViewClient extends WebViewClient {

    private Context context;
    private String merchantId;
    private String transactionId;
    private String plan_amount;
    private Package aPackage ;

    // Custom constructor to pass parameters
    public CustomWebViewClient(Context context, String merchantId, String transactionId, Package aPackage) {
        this.context = context;
        this.merchantId = merchantId;
        this.transactionId = transactionId;
        this.aPackage = aPackage;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();

        // Example: Intercept requests and load from local assets for specific URLs
        if (url == Constants.PAYMENT_GATEWAY_RET_URL) {
            redirectIfResponse(view, url);
            return null;
        }

        // Return null to allow the WebView to handle the request
        return null;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        // Here you can access the additionalParameter and perform custom logic
        String url = request.getUrl().toString();

        if (url == Constants.PAYMENT_GATEWAY_RET_URL) {
            redirectIfResponse(view, url);
            return false;
        }

        // Continue with the default behavior
        return true;
    }



    @Override
    @SuppressWarnings("deprecation") // For API level < 24
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        // Deprecated, but still needed for compatibility
        if (url == Constants.PAYMENT_GATEWAY_RET_URL) {
            redirectIfResponse(view, url);
            return false;
        }

        return true;
    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        // Handle the error
        int errorCode = error.getErrorCode();
        if (errorCode == WebViewClient.ERROR_UNSUPPORTED_SCHEME) {
            String url = request.getUrl().toString();

            Intent intent = new Intent(view.getContext(), PaymentResultActivity.class);

            intent.putExtra("package", aPackage);
            intent.putExtra("currency", "currency");
            intent.putExtra("from", "onepay");

            intent.putExtra("merchantId", this.merchantId);
            intent.putExtra("transactionId", this.transactionId);
            view.getContext().startActivity(intent);
        }
    }

    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
        Log.d("OnePay", "in doUpdateVisitedHistory URL changed to: " + url);

        redirectIfResponse(view, url);
    }

    public void redirectIfResponse(WebView view, String url) {
        if (url == Constants.PAYMENT_GATEWAY_RET_URL) {
            // Start a new activity with the extracted data
            Intent intent = new Intent(view.getContext(), PaymentResultActivity.class);

            intent.putExtra("package", aPackage);
            intent.putExtra("currency", "currency");
            intent.putExtra("from", "onepay");

            intent.putExtra("merchantId", this.merchantId);
            intent.putExtra("transactionId", this.transactionId);
            view.getContext().startActivity(intent);
        }
    }
}

