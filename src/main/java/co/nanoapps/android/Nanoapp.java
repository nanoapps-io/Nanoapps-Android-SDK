package co.nanoapps.android;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by dineshswamy on 9/24/17.
 */

public class Nanoapp implements Parcelable {

    public String id,
            name,
            image_url,
            description,
            main_component_name,
            bundle_url,
            package_name,
            created_at,
            version_code,
            version_name;

    public Boolean installed, is_downloading;
    public Nanoapp() {
    }
    public Nanoapp(JSONObject jsonObject) throws JSONException {
        this.id = jsonObject.getString("id");
        this.name = jsonObject.getString("name");
        this.image_url = jsonObject.getString("image_url");
        this.bundle_url = jsonObject.getString("bundle_url");
        this.description = jsonObject.getString("description");
        this.version_code = jsonObject.getString("version_code");
        this.version_name = jsonObject.getString("version_name");
        this.package_name = jsonObject.getString("package_name");
        this.main_component_name = jsonObject.getString("main_component_name");
        this.created_at = jsonObject.getString("created_at");
        this.installed = false;
        this.is_downloading = false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.image_url);
        dest.writeString(this.description);
        dest.writeString(this.main_component_name);
        dest.writeString(this.bundle_url);
        dest.writeString(this.package_name);
        dest.writeString(this.created_at);
        dest.writeString(this.version_code);
        dest.writeString(this.version_name);
    }

    protected Nanoapp(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.image_url = in.readString();
        this.description = in.readString();
        this.main_component_name = in.readString();
        this.bundle_url = in.readString();
        this.package_name = in.readString();
        this.created_at = in.readString();
        this.version_code = in.readString();
        this.version_name = in.readString();
    }

    public static final Parcelable.Creator<Nanoapp> CREATOR = new Parcelable.Creator<Nanoapp>() {
        @Override
        public Nanoapp createFromParcel(Parcel source) {
            return new Nanoapp(source);
        }

        @Override
        public Nanoapp[] newArray(int size) {
            return new Nanoapp[size];
        }
    };

    public boolean exists() {
        String folder = "/nanoapps/" + package_name + "/" + version_code + "/";
        final String file = "index.android.js";
        final String absoluteFolderPath =
                Environment.getExternalStorageDirectory().getAbsolutePath() + folder + file;
        return new File(absoluteFolderPath).exists();
    }

    public String getAbsoluteFolder() {
        String folder = "/nanoapps/" + package_name + "/" + version_code + "/";
        return Environment.getExternalStorageDirectory().getAbsolutePath() + folder;
    }

    public String getFolder() {
        return "/nanoapps/" + package_name + "/" + version_code + "/";
    }

    public String getAbsoluteFile() {
        String file = "index.android.js";
        String folder = "/nanoapps/" + package_name + "/" + version_code + "/";
        return Environment.getExternalStorageDirectory().getAbsolutePath() + folder + file;
    }

    public String getFile() {
        String file = "index.android.js";
        String folder = "/nanoapps/" + package_name + "/" + version_code + "/";
        return folder+file;
    }

}