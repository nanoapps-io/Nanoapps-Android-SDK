package co.nanoapps.android;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;

import static co.nanoapps.android.Constants.NANOAPP;
import static co.nanoapps.android.Constants.NANOAPPS_ASSETS_URL;
import static co.nanoapps.android.Constants.NANOAPP_ID;
import static co.nanoapps.android.Nanoapps.context;

/**
 * Created by dineshswamy on 10/6/17.
 */

public class PreNanoappActivity extends AppCompatActivity implements Response.Listener<byte[]>, Response.ErrorListener {
    public static final String TAG = "PreNanoappActivity";
    private Nanoapp nanoapp;
    public ReactNativeHost getReactNativeHost(final String fileLocation) {
        final ReactNativeHost defaultReactNativeHost = new ReactNativeHost(context) {
            @Override
            public boolean getUseDeveloperSupport() {
                return false;
            }

            @Override
            protected List<ReactPackage> getPackages() {
                return Arrays.<ReactPackage>asList(
                        new MainReactPackage()
                );
            }

            @Override
            protected String getJSBundleFile() {
                String location = Environment.getExternalStorageDirectory().getAbsolutePath()+fileLocation;
                Log.w("Using JS bundle", location);
                //return "assets://index.android.js";
                return location;
            }
        };
        return defaultReactNativeHost;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pre_nanoapp_activity);
        nanoapp = getIntent().getParcelableExtra(NANOAPP);
        if (nanoapp.exists()) {
            this.appLauncher(nanoapp);
        } else {
            this.downloadBundle(nanoapp);
        }
    }

    private void downloadBundle(final Nanoapp nanoapp) {
        String url = NANOAPPS_ASSETS_URL + nanoapp.bundle_url;
        InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, url,this, this, null);
        RequestQueue mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
        mRequestQueue.add(request);
    }

    public void appLauncher(Nanoapp nanoapp) {
        String file = "/nanoapps/"+nanoapp.package_name+"/"+nanoapp.version_code+"/index.android.js";
        Nanoapps.mReactNativeHost= getReactNativeHost(file);
        NanoappActivity.current_component_name = nanoapp.main_component_name;
        Intent intent = new Intent(this, NanoappActivity.class);
        intent.putExtra(NANOAPP_ID, nanoapp.id);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out);
        return;
    }


    @Override
    public void onResponse(byte[] response) {
        try {
            if (response != null) {
                File folder = new File(nanoapp.getAbsoluteFolder());
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                File bundleFile = new File(folder.getAbsolutePath(), "index.android.js");
                bundleFile.createNewFile();
                FileOutputStream outputStream;
                outputStream = new FileOutputStream(bundleFile, false);
                outputStream.write(response);
                outputStream.close();
                this.appLauncher(nanoapp);
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }
    @Override
    public void onErrorResponse(VolleyError err) {

    }


    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
