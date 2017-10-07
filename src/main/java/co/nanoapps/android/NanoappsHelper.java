package co.nanoapps.android;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static co.nanoapps.android.Constants.NANOAPP_ID;

/**
 * Created by dineshswamy on 9/24/17.
 */

public class NanoappsHelper {

    Application context;
    public NanoappsHelper(Application context) {
        this.context = context;
    }

    public  ReactNativeHost getReactNativeHost(final String fileLocation) {
        final ReactNativeHost defaultReactNativeHost = new ReactNativeHost(this.context) {
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
                return location;
            }
        };
        return defaultReactNativeHost;
    }

    public void appLauncher(Nanoapp nanoapp) {
        Intent intent = new Intent(context, PreNanoappActivity.class);
        intent.putExtra(Constants.NANOAPP, nanoapp);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean isFileExists(String filename){
        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + filename);
        return folder.exists();
    }
    public static boolean deleteFile( String filename){
        File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + filename);
        return folder.delete();
    }
}

