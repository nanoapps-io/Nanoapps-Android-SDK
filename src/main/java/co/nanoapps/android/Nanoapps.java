package co.nanoapps.android;

/**
 * Created by dineshswamy on 9/5/17.
 */


import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.react.ReactNativeHost;

import java.util.HashMap;
import java.util.Map;

import co.nanoapps.android.NanoappsListActivity;

import static co.nanoapps.android.Constants.AUTH_TOKEN;
import static co.nanoapps.android.Constants.IS_DEVICE_REGISTERED;
import static co.nanoapps.android.Constants.PROJECT_TOKEN;
import static co.nanoapps.android.Constants.PUSH_TOKEN;
import static co.nanoapps.android.Constants.UNIQUE_USER_IDENTIFIER;

/**
 * Created by dineshswamy on 9/4/17.
 */

public class Nanoapps {

    private static Nanoapps nanoapps;
    private static RequestQueue mRequestQueue;
    public static Application context;
    public static String projectToken = "";
    private static String uniqueUserIndentifier = "";
    private static String sdk = "0100";
    private static String TAG = "Nanoapps";
    public static String PUSH_MESSAGE = "nanoapp_push_message";
    public static ReactNativeHost mReactNativeHost;
    public static JSONArray nanoappUsageDetails = new JSONArray();


    public Nanoapps(Application context, String appInstanceId, String uniqueUserIdentifier) {
        this.projectToken = appInstanceId;
        this.context = context;
        this.uniqueUserIndentifier = uniqueUserIdentifier;
    }


    public static Nanoapps init(Application context, String projectToken, String uniqueUserIdentifier) {
        if (nanoapps == null) {
            nanoapps = new Nanoapps(context, projectToken, uniqueUserIdentifier);
        }
        SharedPreferences _prefs = context.getSharedPreferences(Constants.NANOAPP_PREFERENCES, Context.MODE_PRIVATE);
        Boolean isDeviceRegistered = _prefs.getBoolean(IS_DEVICE_REGISTERED, false);
        registerDeviceWithBackendServer();

        return nanoapps;
    }

    public static void startActivity() {
        Intent intent = new Intent(context, NanoappsListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private static void registerDeviceWithBackendServer() {
        Log.i(TAG, "I was never called. I dont know why");
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(context);
        }
        String deviceManufacturerId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        String url = Constants.NANOAPPS_API_URL+"/api/device/add";
        JSONObject deviceParams = new JSONObject();
        //user params
        try {
            deviceParams.put(PROJECT_TOKEN, projectToken);
            deviceParams.put(Constants.OS, Build.VERSION.RELEASE);
            deviceParams.put(Constants.TIMEZONE, String.valueOf(Utils.getTimeZoneOffset()));
            deviceParams.put(Constants.LANGUAGE, Utils.getCorrectedLanguage());
            deviceParams.put(Constants.SDK, sdk);
            deviceParams.put(Constants.SDK_TYPE, "native");
            deviceParams.put(Constants.DEVICE_MANUFACTURER_ID, deviceManufacturerId);
            deviceParams.put(Constants.DEVICE_MODEL, Build.MODEL);
            //Should be removed
            deviceParams.put(Constants.DEVICE_TYPE, "");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, url, deviceParams, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.i(TAG, "Device registered successfully "+response.toString());
                    if (response.getString("status").equalsIgnoreCase("success")) {
                        Log.i(TAG, "Device successfully registered");
                        SharedPreferences _prefs = context.getSharedPreferences(Constants.NANOAPP_PREFERENCES, Context.MODE_PRIVATE);
                        _prefs.edit().putBoolean(IS_DEVICE_REGISTERED, true).commit();
                        Log.i(TAG, "Device successfully registered" + response.getString("auth_token"));
                        _prefs.edit().putString(AUTH_TOKEN, response.getString("auth_token")).commit();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error occured while registering device"+ error.getLocalizedMessage());
            }
        });
        mRequestQueue.add(jsonObjReq);
    }
    private static void addToPreference(String key, String value, boolean encrypt) {
        if (encrypt) {
            key = Utils.encode(key);
            value = Utils.encode(value);
        }
    }

    public static void setPushToken(final String pushToken) {
        SharedPreferences _prefs = context.getSharedPreferences(Constants.NANOAPP_PREFERENCES, Context.MODE_PRIVATE);
        String existingPushToken = _prefs.getString(PUSH_TOKEN, "");
        if (pushToken!=null && !pushToken.isEmpty() && !pushToken.equalsIgnoreCase(existingPushToken)){
            if (mRequestQueue == null) {
                mRequestQueue = Volley.newRequestQueue(context);
            }
            String url = Constants.NANOAPPS_API_URL+"/api/device/add_push_token";
            JSONObject deviceParams = new JSONObject();
            //user params
            try {
                deviceParams.put(PUSH_TOKEN, pushToken);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            final String x_nanoapp_token = "Bearer "+_prefs.getString(AUTH_TOKEN,"");
            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, url, deviceParams, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response.getString("status").equalsIgnoreCase("success")) {
                            Log.i(TAG, "Push token successfully added");
                            SharedPreferences _prefs = context.getSharedPreferences(Constants.NANOAPP_PREFERENCES, Context.MODE_PRIVATE);
                            _prefs.edit().putString(PUSH_TOKEN, pushToken).commit();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Error occured while adding push token"+ error.getLocalizedMessage());
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String>  params = new HashMap<String, String>();
                    params.put("X-Nanoapp-Token",x_nanoapp_token);
                    return params;
                }
            };;
            mRequestQueue.add(jsonObjReq);
        }
    }

}
