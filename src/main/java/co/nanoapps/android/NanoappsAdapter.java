package co.nanoapps.android;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


import static android.R.attr.id;
import static android.content.Context.DOWNLOAD_SERVICE;
import static co.nanoapps.android.Constants.NANOAPPS_ASSETS_URL;
import static co.nanoapps.android.Constants.SERVICE_INTENT;
import static co.nanoapps.android.Constants.UPDATE_NANOAPP_STATUS;
import static co.nanoapps.android.NanoappsHelper.deleteFile;
import static co.nanoapps.android.NanoappsHelper.isFileExists;

/**
 * Created by dineshswamy on 4/16/17.
 */

public class NanoappsAdapter extends RecyclerView.Adapter<NanoappsAdapter.NanoappViewHolder>{

    List<Nanoapp> nanoappList;
    private Activity context;
    private long enqueue;
    private DownloadManager dm;
    private HashMap<Long, Integer> dm_id_map = new HashMap<Long,Integer>();
    NanoappsHelper nanoappsHelper;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                long downloadId = intent.getLongExtra(
                        DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                dm_id_map.keySet();
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                Cursor c = dm.query(query);
                if (c.moveToFirst()) {
                    int columnIndex = c
                            .getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                        int position = dm_id_map.get(downloadId);
                        Nanoapp nanoapp = nanoappList.get(position);
                        nanoapp.is_downloading = false;
                        nanoapp.installed = true;
                        nanoappList.set(position, nanoapp);
                        notifyDataSetChanged();
                        Intent nanoapps_intent = new Intent(context, NanoappsUpdaterService.class);
                        nanoapps_intent.putExtra(SERVICE_INTENT, UPDATE_NANOAPP_STATUS);
                        nanoapps_intent.putExtra("app_id",nanoapp.id);
                        nanoapps_intent.putExtra("user_id","1");
                        nanoapps_intent.putExtra("is_addition", true);
                        context.startService(nanoapps_intent);
                    }
                }
            }
        }
    };

    public void registerReceiver()
    {
        context.registerReceiver(receiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public void unregisterReceiver()
    {
        context.unregisterReceiver(receiver);
    }

    public  NanoappsAdapter(List<Nanoapp> addonList, Activity context) {
        this.nanoappList = addonList;
        this.nanoappsHelper = new NanoappsHelper(Nanoapps.context);
        this.context = context;
    }
    public class NanoappViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView cv;
        TextView title,description,add_icon;
        ImageView thumbnail;
        ProgressBar progress_bar;

        public NanoappViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            title = (TextView)itemView.findViewById(R.id.title_of_the_app);
            description = (TextView)itemView.findViewById(R.id.description_of_the_app);
            add_icon = (TextView) itemView.findViewById(R.id.add_icon);
            thumbnail = (ImageView) itemView.findViewById(R.id.app_thumbnail);
            progress_bar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
            add_icon.setOnClickListener(this);
            cv.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Nanoapp nanoapp = nanoappList.get(position);
            if (view.getId() == R.id.add_icon) {
                if (nanoapp.installed) {
                    nanoappsHelper.appLauncher(nanoapp);
                    return;
                }
                handleAddonClick(nanoapp, position);
            } else if(view.getId() == R.id.cv) {
                //Log.w("Component name", nanoapp.main_component_name);
                nanoappsHelper.appLauncher(nanoapp);
            }
        }
    }

    public void downloadBundle(Nanoapp nanoapp, int position){
        String folder = "/nanoapps/"+nanoapp.package_name+"/"+nanoapp.version_code+"/";
        String file = "index.android.js";
        if (isFileExists(folder+file)) {
            deleteFile(folder+file);
        }
        dm = (DownloadManager) this.context.getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(Constants.NANOAPPS_API_URL+"/"+nanoapp.bundle_url));
        request.setDestinationInExternalPublicDir(folder, file);

        long dm_id = dm.enqueue(request);
        dm_id_map.put(dm_id, position);
    }


    public  void handleAddonClick(Nanoapp nanoapp,int itemPosition) {
        nanoapp.is_downloading = true;
        nanoappList.set(itemPosition, nanoapp);
        notifyDataSetChanged();
        downloadBundle(nanoapp, itemPosition);
    }

    @Override
    public NanoappViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_card_view, parent, false);
        NanoappViewHolder nanoappViewHolder = new NanoappViewHolder(view);
        return nanoappViewHolder;
    }

    @Override
    public void onBindViewHolder(NanoappViewHolder holder, int position) {
        holder.title.setText(nanoappList.get(position).name);
        holder.description.setText(nanoappList.get(position).description);
        String image_url = NANOAPPS_ASSETS_URL+nanoappList.get(position).image_url;
        Picasso.with(context)
                .load(image_url)
                .resize(100, 130)
                .centerCrop()
                .into(holder.thumbnail);
        if (nanoappList.get(position).is_downloading) {
            holder.progress_bar.setVisibility(View.VISIBLE);
            holder.add_icon.setVisibility(View.INVISIBLE);
        } else if (nanoappList.get(position).installed) {
            holder.progress_bar.setVisibility(View.INVISIBLE);
            holder.add_icon.setVisibility(View.VISIBLE);
            holder.add_icon.setText("OPEN");
        } else  {
            holder.progress_bar.setVisibility(View.INVISIBLE);
            holder.add_icon.setVisibility(View.VISIBLE);
            holder.add_icon.setText("ADD");
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return this.nanoappList.size();
    }
}
