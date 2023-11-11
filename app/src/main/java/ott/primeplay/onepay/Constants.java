package ott.primeplay.onepay;

public class Constants {


    public static final String merchantID = "M00006152";
    public static final String secretKey = "ud5jc8PL4oR6hQ8Ha7ad8pb0aG2HX6Fh";
    public static final String iv = "ud5jc8PL4oR6hQ8H";
    public static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    public static final String PAYMENT_GATEWAY_URL = "https://pa-preprod.1pay.in/payment/payprocessorV2";
    // public static final String PAYMENT_GATEWAY_RET_URL = "https://pa-preprod.1pay.in/payment/merchantResponse.jsp";  //change checkURL function in webV if this is change
    //public static final String PAYMENT_GATEWAY_RET_URL = "https://expressrates.in/response_json.php";
    // public static final String PAYMENT_GATEWAY_RET_URL = "https://florix.net/1Pay_php/response.php";

   // public static final String PAYMENT_GATEWAY_RET_URL = "www.primeplay.co.in";
    public static final String PAYMENT_GATEWAY_RET_URL = "onepay://onepay.payment.response";
    // public static final String PAYMENT_GATEWAY_RET_URL = "https://lance23074.pythonanywhere.com/loader";

    public static final String PAYMENT_GATEWAY_RES_URL = "https://pa-preprod.1pay.in/payment/getTxnDetails";


/*

    public static final String merchantID = "M00005353";
    public static final String secretKey = "fj0RH5hm2El7FV8yr6ns7UL7qr3Np3km";
    public static final String iv = "fj0RH5hm2El7FV8y";//16 first from above key
    public static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    public static final String PAYMENT_GATEWAY_URL = "https://pa-preprod.1pay.in/payment/payprocessorV2";
    // public static final String PAYMENT_GATEWAY_RET_URL = "https://pa-preprod.1pay.in/payment/merchantResponse.jsp";  //change checkURL function in webV if this is change
    //public static final String PAYMENT_GATEWAY_RET_URL = "https://expressrates.in/response_json.php";
    // public static final String PAYMENT_GATEWAY_RET_URL = "https://florix.net/1Pay_php/response.php";
    public static final String PAYMENT_GATEWAY_RET_URL = "onepay://onepay.payment.response";
    //public static final String PAYMENT_GATEWAY_RET_URL = "https://lance23074.pythonanywhere.com/loader";
    public static final String PAYMENT_GATEWAY_RES_URL = "https://pa-preprod.1pay.in/payment/getTxnDetails";

*/




}






