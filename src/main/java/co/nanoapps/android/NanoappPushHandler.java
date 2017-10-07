package co.nanoapps.android;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.util.Map;


/**
 * Created by dineshswamy on 10/6/17.
 */

public class NanoappPushHandler {
    private static String TAG = "NanoappPushHandler";
    private Context context;
    private NanoappPushHandler nanoappPushHandler;

    public static void handleMessage(Context context, Map<String,String> data) {
        Log.w(TAG, "handling message");
        String purpose = data.get(Constants.PURPOSE).toString();
        if (purpose.equalsIgnoreCase(Constants.CAMPAIGN_PURPOSE)){
            String campaignId = data.get(Constants.CAMPAIGN_ID).toString();

//            String title = data.get(Constants.TITLE).toString();
//            String subtitle = data.get(Constants.SUB_TITLE).toString();
//            String imgUrl = data.get(Constants.IMG_URL).toString();
//            String bundleUrl = data.get(Constants.BUNDLE_URL).toString();
//            String packageName = data.get(Constants.PURPOSE).toString();
//            String versionCode = data.get(Constants.VERSION_CODE).toString();
//            String mainComponentName = data.get(Constants.MAIN_COMPONENT_NAME).toString();
//
//            Nanoapp nanoapp = new Nanoapp();
//            nanoapp.id = id;
//            nanoapp.bundle_url = bundleUrl;
//            nanoapp.package_name = packageName;
//            nanoapp.version_code = versionCode;
//            nanoapp.main_component_name = mainComponentName;

            Intent  intent = new Intent(context, NanoappsUpdaterService.class);
            intent.putExtra(Constants.CAMPAIGN_ID, campaignId);
            intent.putExtra(Constants.SERVICE_INTENT, Constants.DOWNLOAD_NANOAPP_BUNDLE);
//            intent.putExtra(Constants.TITLE, title);
//            intent.putExtra(Constants.SUB_TITLE, subtitle);
//            intent.putExtra(Constants.NANOAPP, nanoapp);

            context.startService(intent);
        } else if (purpose.equalsIgnoreCase(Constants.UPDATE_PURPOSE)) {

        }
    }
}
