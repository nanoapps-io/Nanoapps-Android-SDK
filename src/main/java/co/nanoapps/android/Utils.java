package co.nanoapps.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by dineshswamy on 9/6/17.
 */

public class Utils {

    public static int getTimeZoneOffset() {
        TimeZone timezone = Calendar.getInstance().getTimeZone();
        int offset = timezone.getRawOffset();

        if (timezone.inDaylightTime(new Date()))
            offset = offset + timezone.getDSTSavings();

        return offset / 1000;
    }

    public static String getCorrectedLanguage() {
        String lang = Locale.getDefault().getLanguage();

        // https://github.com/OneSignal/OneSignal-Android-SDK/issues/64
        if (lang.equals("iw"))
            return "he";
        if (lang.equals("in"))
            return "id";
        if (lang.equals("ji"))
            return "yi";

        // https://github.com/OneSignal/OneSignal-Android-SDK/issues/98
        if (lang.equals("zh"))
            return lang + "-" + Locale.getDefault().getCountry();

        return lang;
    }

    public static String encode(String input) {
        // This is base64 encoding, which is not an encryption
        return Base64.encodeToString(input.getBytes(), Base64.DEFAULT);
    }

    public static String decode(String input) {
        return new String(Base64.decode(input, Base64.DEFAULT));
    }

    public static String getEncryptedPreference(Context context, String key, String defaultValue) {
        key = Utils.encode(key);
        Log.w("AuthString - r", "key = "+ key);
        SharedPreferences _prefs = context.getSharedPreferences(Constants.NANOAPP_PREFERENCES, Context.MODE_PRIVATE);
        String value = _prefs.getString(key, defaultValue);
        if (!value.equalsIgnoreCase(defaultValue)) {
            value = Utils.decode(value);
        }
        Log.w("AuthString - r", "value = "+ value);
        return value;
    }

    public static String parseDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(date);
    }

}
