package co.nanoapps.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.facebook.react.ReactActivity;
import com.facebook.react.ReactActivityDelegate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import static co.nanoapps.android.Constants.NANOAPP_USAGE_DETAILS;
import static co.nanoapps.android.Constants.SERVICE_INTENT;
import static co.nanoapps.android.Nanoapps.nanoappUsageDetails;

public class NanoappActivity extends ReactActivity {

    /**
     * Returns the name of the main component registered from JavaScript.
     * This is used to schedule rendering of the component.
     */
    Date startTime, endTime;
    String nanoappId="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startTime = new Date();
        nanoappId = getIntent().getStringExtra("nanoapp_id");
    }
    public static String current_component_name="";
    @Override
    protected String getMainComponentName() {
        return current_component_name;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        endTime = new Date();
        JSONObject usageDetail = new JSONObject();
        try {
            usageDetail.put("nanoapp_id",Integer.parseInt(nanoappId));
            usageDetail.put("start_time", Utils.parseDate(startTime));
            usageDetail.put("end_time", Utils.parseDate(endTime));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        nanoappUsageDetails.put(usageDetail);
        Intent intent = new Intent(this, NanoappsUpdaterService.class);
        intent.putExtra(SERVICE_INTENT, NANOAPP_USAGE_DETAILS);
        intent.putExtra("details", nanoappUsageDetails.toString());
        intent.putExtra("project_token", Nanoapps.projectToken);
        this.startService(intent);
        nanoappUsageDetails = new JSONArray();
    }
}