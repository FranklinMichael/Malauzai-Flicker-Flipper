package com.technicalaptitude.michaelfranklin.malauzaiflickerflipper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

/**
 * Created by Michael Franklin on 2/09/2016.
 */
public class DownloaderReceiver extends BroadcastReceiver {
    public DownloaderReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null)
        {
            try
            {
                JSONObject Response = new JSONObject(bundle.getString(DownloaderService.RESPONSE));
                HandleResponse(Response);

            } catch (Exception e) {
                Log.d("DownloaderRecv.Recv", e.toString());
            }
        }
    }

    private void HandleResponse(JSONObject Response)
    {
        try
        {
            switch (Response.getString("ResType"))
            {
                case "Failed":
                    String DownloadedURL = Response.getString("URL");
                    int TotalFails = Response.getInt("TotalFailed");
                    int FileFails = Response.getInt("FileFails");
                    
		            // broadcast failure
                    MainActivity.instance().DownloadFailed(DownloadedURL);

                    break;
                case "Complete":
                    // get downloaded URL
                    DownloadedURL = Response.getString("URL");
                    // broadcast completion
                    MainActivity.instance().DownloadComplete(DownloadedURL);
                    break;
                case "Update":
                    DownloadedURL = Response.getString("URL");
                    int Percent = Response.getInt("Percent");
                    // broadcast update
                    MainActivity.instance().DownloadUpdated(DownloadedURL, Percent);
                    break;
            }
        } catch (Exception e) {
            Log.d("DownloaderRecv.Handle", e.toString());
        }
    }
}
