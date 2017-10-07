package co.nanoapps.android;

import android.app.DownloadManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import co.nanoapps.android.Constants;

import static co.nanoapps.android.Constants.CAMPAIGN_ID;
import static co.nanoapps.android.Constants.NANOAPP;
import static co.nanoapps.android.Constants.SERVICE_INTENT;
import static co.nanoapps.android.Constants.UPDATE_NANOAPP_STATUS;


/**
 * Created by dineshswamy on 4/16/17.
 */

public class NanoappsUpdaterService extends IntentService implements Response.ErrorListener, Response.Listener<String> {

    private RequestQueue mRequestQueue;
    private String TAG = "NanoappsUpdaterService";

    public NanoappsUpdaterService() {
        super("NanoappsUpdater");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String service_intent = intent.getStringExtra(SERVICE_INTENT);
        switch (service_intent){
            case Constants.UPDATE_NANOAPP_STATUS:
                Log.w(TAG,"Service started for update nanoapp status");
                updateNanoappStatus(intent);
                break;
            case Constants.NANOAPP_USAGE_DETAILS:
                Log.w(TAG,"Service started for nanoapp usage details");
                addNanoappUsageDetails(intent);
                break;
            case Constants.DOWNLOAD_NANOAPP_BUNDLE:
                Log.w(TAG,"Service started for nanoapp usage details");
                NanoappCampaignHandler  nanoappCampaignHandler = new NanoappCampaignHandler(this);
                nanoappCampaignHandler.handleCampaign(intent.getStringExtra(Constants.CAMPAIGN_ID));
                break;
            default:
                Log.w(TAG,"Service started without intent");
        }
    }

    private void addNanoappUsageDetails(Intent intent) {
        final String project_token = intent.getStringExtra("project_token");
        final String details = intent.getStringExtra("details");
        mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        String url = Constants.NANOAPPS_API_URL+"/api/nanoapp/add_usage_details";
        JSONObject data = new JSONObject();
        try {
            data.put("project_token", project_token);
            data.put("details", new JSONArray(details));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.w(TAG, data.toString());
        final JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String status = "", message = "";
                        try {
                            status = response.getString("status");
                            message = response.getString("message");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.w(TAG, "Json parsing error");
                        }
                        Log.w(TAG, "status= "+status + " message= "+message);
                    }
                }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Error occured while adding nanoapp usage details");
                    Log.e(TAG, error.getLocalizedMessage());
                }
        });
        mRequestQueue.add(jsonRequest);
    }

    private void updateNanoappStatus (Intent intent) {
        Boolean is_addition = intent.getBooleanExtra("is_addition", false);
        Boolean is_deletion = intent.getBooleanExtra("is_deletion", false);
        final String app_id = intent.getStringExtra("app_id");
        mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        String url = "";
        if (is_addition || is_deletion) {
            if (is_addition) {
                url = "/add_app";
            } else {
                url = "/delete_app";
            }
            StringRequest jsonRequest = new StringRequest(Request.Method.POST, Constants.NANOAPPS_API_URL+"/"+url, this, this){
                @Override
                public byte[] getBody() {
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put("app_id", app_id);
                    params.put("user_id", "1");
                    return new JSONObject(params).toString().getBytes();
                }

                @Override
                public String getBodyContentType() {
                    return "application/json";
                }
            };
            mRequestQueue.add(jsonRequest);
        }
    }

    @Override
    public void onErrorResponse (VolleyError error){

    }


    @Override
    public void onResponse (String response){

    }
}
