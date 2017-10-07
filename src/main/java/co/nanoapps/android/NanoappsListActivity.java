package co.nanoapps.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import co.nanoapps.android.Constants;
import co.nanoapps.android.Nanoapp;
import co.nanoapps.android.Nanoapps;
import co.nanoapps.android.NanoappsAdapter;
import co.nanoapps.android.NanoappsHelper;
import co.nanoapps.android.R;
import co.nanoapps.android.Utils;

import static co.nanoapps.android.Constants.NANOAPPS_API_URL;

public class NanoappsListActivity extends AppCompatActivity implements Response.ErrorListener, Response.Listener<String> {

    RecyclerView recycler_view;
    ArrayList<Nanoapp> nanoappsList;
    public static String TAG = "NanoappsListActivity";
    private RequestQueue mRequestQueue;
    private NanoappsAdapter nanoappsAdapter;
    SharedPreferences _prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nanoapps_list);
        recycler_view = (RecyclerView)findViewById(R.id.rv);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        recycler_view.setLayoutManager(llm);
        nanoappsList = new ArrayList<Nanoapp>();
        nanoappsAdapter = new NanoappsAdapter(nanoappsList, this);
        recycler_view.setAdapter(nanoappsAdapter);
        _prefs = PreferenceManager.getDefaultSharedPreferences(this);
        loadNanoappsListView();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        nanoappsAdapter.registerReceiver();
    }

    @Override
    protected void onStop() {
        nanoappsAdapter.unregisterReceiver();
        super.onStop();
    }

    private void loadNanoappsListView() {
        String url = NANOAPPS_API_URL+"/api/project/get_nanoapps?project_token="+Nanoapps.projectToken;
        SharedPreferences _prefs = this.getSharedPreferences(Constants.NANOAPP_PREFERENCES, Context.MODE_PRIVATE);
        final String authToken = Utils.getEncryptedPreference(this, Constants.AUTH_TOKEN, "");
        final Map<String, String> mParams = new HashMap<String, String>();
        mParams.put(Constants.PROJECT_TOKEN, Nanoapps.projectToken);
        if (isNetworkAvailable()) {
            StringRequest jsonObjReq = new StringRequest(Request.Method.GET, url,this,this) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(Constants.AUTH_TOKEN, authToken);
                    return params;
                }
                @Override
                public Map<String, String> getParams() {
                    return mParams;
                }
            };
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
            mRequestQueue.add(jsonObjReq);
        } else {
            String response = _prefs.getString("cached_app_details", "");
            onResponse(response);
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        VolleyLog.d(TAG, "Error: " + error.getMessage());
        Log.w(TAG,"Error: " + error.getMessage());
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onResponse(String response) {
        try {
            _prefs.edit().putString("cached_app_details", response).commit();
            JSONObject jsonResponse = new JSONObject(response);
            if (jsonResponse.getString("status").equalsIgnoreCase("success")) {
                JSONArray jsonArray = jsonResponse.getJSONArray("nanoapps");
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        Nanoapp nanoapp = new Nanoapp(jsonArray.getJSONObject(i));
                        nanoappsList.add(nanoapp);
                    }
                    nanoappsAdapter.notifyDataSetChanged();
                    Log.w("Addons Activity", "Adapted attached");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
