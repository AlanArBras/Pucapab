package net.huitel.pucapab.network;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * https://stackoverflow.com/questions/29339565/calling-rest-api-from-an-android-app
 * Created by root on 1/8/18.
 *
 * Grande image: selected_images (JSONObject) -> display (JSONObject) -> fr (String)
 * Nom du produit:  product_name_fr (String)
 * Marque : brands (String)
 */

public class HttpUtils {
    private static final String BASE_URL = "https://world.openfoodfacts.org/api/v0/product/";
    private static final String EXTENSION = ".json";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String barcode, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(barcode), params, responseHandler);
    }

    public static void getByUrl(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(url, params, responseHandler);
    }

    public static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl + EXTENSION;
    }

}