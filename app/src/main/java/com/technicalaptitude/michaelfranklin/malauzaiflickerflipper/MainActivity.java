package com.technicalaptitude.michaelfranklin.malauzaiflickerflipper;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private static final String PagerID = "pager";

    private static MainActivity instance;
    private FragmentManager FragMngr = null;
    private PagerManager PagerMngr = null;
    private SwipeRefreshLayout SwipeRefresh;

    public static final synchronized MainActivity instance() {
        if (instance == null) {
            return null;
        }
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = this;

        // setup vars
        FragMngr = getSupportFragmentManager();
        SwipeRefresh = findViewById(R.id.swiperefresh);
        SwipeRefresh.setOnRefreshListener(this);
        // Check if there is any saved data
        JSONArray StoredArray = FileIO.instance().ReadInJSONArrayFromFile(this,getString(R.string.photo_data_filename));
        if (StoredArray != null)
        {
            // we have data. Send it to the Pager Manager
            GetPhotos(StoredArray);
        }
        else {
            // we have no data. Get some.
            GetPhotoData();
        }
    }

    private void GetPhotoData()
    {
        try
        {
            // populate request
            JSONObject RequestJSON = new JSONObject();
            RequestJSON.put(getString(R.string.api_key_format_type),getString(R.string.api_data_format_type));
            CommunicationWrapper.instance().SendCommunicationRequest(this,getString(R.string.packagefor_main),getString(R.string.public_photos_endpoint),RequestJSON);
        }
        catch (Exception e)
        {
            Log.d("Main.GetPhotoData: ", e.toString());
        }
    }

    public void GetPhotos(JSONArray Photos)
    {
        // Add a new Pager Manager fragment
        try
        {
            FragmentTransaction FragTrans = FragMngr.beginTransaction();
            PagerMngr = PagerManager.newInstance(Photos.toString());
            FragTrans.add(R.id.FragmentFrame,PagerMngr,PagerID);
            FragTrans.commit();
        }
        catch (Exception e)
        {
            Log.d("Main.GetPhotos: ", e.toString());
        }

    }

    // Called by SwipeRefresher
    public void onRefreshImages() {
        // Remove Pager Fragment
        FragmentTransaction FragTrans = FragMngr.beginTransaction();
        FragTrans.remove(FragMngr.findFragmentByTag(PagerID));
        FragTrans.commit();

        // delete all saved data
        FileIO.instance().DeleteFiles(this);

        // download new data
        GetPhotoData();
    }

    // Downloader Callbacks
    public void DownloadComplete(String Url)
    {
        // pass result to Pager Manager
        PagerMngr.DownloadComplete(Url);
    }
    public void DownloadFailed(String Url)
    {
        // pass result to Pager Manager
        PagerMngr.DownloadFailed(Url);
    }
    public void DownloadUpdated(String Url, int Percent)
    {
        // pass result to Pager Manager
        PagerMngr.DownloadUpdated(Url, Percent);
    }

    // Swipe to Refresh Callback
    @Override
    public void onRefresh() {
        onRefreshImages();
        SwipeRefresh.setRefreshing(false);
    }
}
