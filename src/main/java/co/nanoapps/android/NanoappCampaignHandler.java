package co.nanoapps.android;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import static co.nanoapps.android.Constants.NANOAPP;
import static co.nanoapps.android.Constants.NANOAPPS_ASSETS_URL;
import static co.nanoapps.android.NanoappsHelper.deleteFile;
import static co.nanoapps.android.NanoappsHelper.isFileExists;

/**
 * Created by dineshswamy on 10/6/17.
 */

public class NanoappCampaignHandler {
    Context context;
    private String TAG = "CampaignHandler";
    private String CampaignName = "";
    private String title = "";
    private String subTitle = "";
    private String nanoappId = "";
    private String imageUrl = "";
    private String bundleUrl = "";
    private String mainComponentName = "";
    private String packageName = "";
    private String versionCode = "";
    private String versionName = "";
    private Nanoapp nanoapp;

    public NanoappCampaignHandler(Context context){
        this.context = context;
    }

    public class PushNotificationTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {

            InputStream in;
            try {
                URL url = new URL(imageUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                in = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(in);
                return myBitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            Log.w(TAG, "onPostExecute....");
            // Create the style object with BigPictureStyle subclass.
            NotificationCompat.BigPictureStyle notiStyle = new
                    NotificationCompat.BigPictureStyle();
            notiStyle.setBigContentTitle(title);
            notiStyle.setSummaryText(subTitle);
            notiStyle.bigPicture(bitmap);
            Intent resultIntent = new Intent(context, PreNanoappActivity.class);
            resultIntent.putExtra(NANOAPP, nanoapp);
            // Adds the Intent that starts the Activity to the top of the stack.
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, resultIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            Notification notification = new NotificationCompat.Builder(context)
                    .setAutoCancel(true)
                    .setLargeIcon(bitmap)
                    .setContentIntent(pendingIntent)
                    .setContentTitle(title)
                    .setContentText(subTitle)
                    .setSmallIcon(android.R.drawable.arrow_up_float)
                    .setStyle(notiStyle).build();
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(11221, notification);
        }
    }

    private void downloadBundle() {
        String url = NANOAPPS_ASSETS_URL + bundleUrl;
        String folder = "/nanoapps/" + packageName + "/" + versionCode + "/";
        final String file = "index.android.js";
        final String absoluteFolderPath =
                Environment.getExternalStorageDirectory().getAbsolutePath() + folder;

        InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, url,
                new Response.Listener<byte[]>() {
                    @Override
                    public void onResponse(byte[] response) {
                        try {
                            if (response != null) {
                                Log.w(TAG, "File download complete");
                                File folder = new File(absoluteFolderPath);
                                if (!folder.exists()) {
                                    folder.mkdirs();
                                    System.out.println("Making dirs");
                                }
                                File bundleFile = new File(folder.getAbsolutePath(), file);
                                bundleFile.createNewFile();
                                FileOutputStream outputStream;
                                outputStream = new FileOutputStream(bundleFile, false);
                                outputStream.write(response);
                                outputStream.close();
                                new PushNotificationTask().execute();
                            }
                        } catch (Exception e) {

                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError err) {

                    }
        }, null);
        RequestQueue mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
        mRequestQueue.add(request);
    }

    public void handleCampaign(String campaignId) {
        RequestQueue mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
        String url = Constants.NANOAPPS_API_URL+"/api/campaign/details/"+campaignId;
        JSONObject data = new JSONObject();
        Log.w(TAG, data.toString());
        final JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, data,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            title = response.getString("title");
                            subTitle = response.getString("subtitle");
                            imageUrl = response.getString("image_url");
                            JSONObject nanoappJson = response.getJSONObject("nanoapp");
                            nanoapp = new Nanoapp(nanoappJson);
                            bundleUrl = nanoappJson.getString("bundle_url");
                            mainComponentName = nanoappJson.getString("main_component_name");
                            versionCode = nanoappJson.getString("version_code");
                            packageName = nanoappJson.getString("package_name");
                            downloadBundle();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.w(TAG, "Json parsing error");
                        }
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
}
