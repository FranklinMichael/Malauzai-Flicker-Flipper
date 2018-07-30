package com.technicalaptitude.michaelfranklin.malauzaiflickerflipper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLDecoder;

/**
 * Created by Michael Franklin on 1/18/2016.
 */
public class APIResponseReceiver extends BroadcastReceiver {
    public static final String RESPONSE = "malauzaiflickerflipper.MichaelFranklin.Library.APIResponseReceiver.response";
    public static final String BROADCAST = "MichaelFranklin.Library.APIResponseReceiver.broadcast";

    public APIResponseReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            try
            {
                JSONObject Response = new JSONObject(bundle.getString(APICommunicationService.RESPONSE));
                String PackageFor = Response.getString("packageFor");
                APIResponse(context, Response, PackageFor);
            }
            catch (Exception e)
            {
                Log.d("APIReceiver.onAPIRecv", e.toString());
            }

        }
    }

    private void APIResponse(Context context, JSONObject Response, String PackageFor)
            throws Exception
    {
        // check Response Category for OK message from API
        if (Response.getString("Response Category").equals(context.getString(R.string.api_response_ok)))
        {
            // Determine data type of Data String
            //Object Data = new JSONTokener(Response.getString("data")).nextValue();
            Object Data = Response.get("data");
            // call method based on PackageFor
            if (PackageFor.equals(context.getString(R.string.packagefor_main)))
            {
                if ((Data instanceof String))
                {
                    MainActivityResponse(context, (String) Data);
                }
                else
                {
                    throw new Exception(PackageFor + " Expected String - Got: " + Data.getClass().getCanonicalName());
                }
            }
            else
            {
                throw new Exception("API errored: No Valid PackageFor Label " + PackageFor + " on Response");
            }
        }
        else
        {
            Log.d("APIReceiver.ResNotOK","Response Category: " + Response.getString("Response Category") +"\nResponse Message: " + Response.getString("Response Message"));
            // Broadcast error back to requester

            // check the type of request
            if (PackageFor.equals(context.getString(R.string.packagefor_main)))
            {


            }
            else
            {
                throw new Exception("API errored: No Valid PackageFor Label " + PackageFor + " on Error Handling");
            }
        }
    }

    private void MainActivityResponse(Context context, String Response)
            throws Exception
    {
        // extract response data
        JSONArray Photos = new JSONArray(); // will hold extracted data

        // trim beginning
        Response = Response.substring(Response.indexOf('{'),Response.lastIndexOf('}')+1);

        // Convert to JSON for extraction
        JSONObject ResponseJSON = new JSONObject(Response);
        JSONArray ResponseList = ResponseJSON.getJSONArray(context.getString(R.string.api_key_photo_list));

        // Extract Photo data for each photo
        for (int i = 0; i < ResponseList.length();i++)
        {
            JSONObject PhotoJSON = new JSONObject(); // holds relevant photo data to be stored in list

            // Trim title if needed
            String title = ResponseList.getJSONObject(i).getString(context.getString(R.string.api_key_photo_title));
            if (title.contains(".jpg"))
            {
                title = title.substring(0,title.length()-4);
            }
            if (title.equals(" ") || title.equals(""))
            {
                title = "<No Title>";
            }
            PhotoJSON.put(context.getString(R.string.store_key_photo_title),title);
            // trim url
            String URL = ResponseList.getJSONObject(i).getString(context.getString(R.string.api_key_photo_image));
            URL = URL.substring(6,URL.length()-2);
            // remove escape slashes
            URL = URL.replace("\\","");
            PhotoJSON.put(context.getString(R.string.store_key_photo_image),URL);

            Photos.put(PhotoJSON);
        }
        // write photos list to file to be read during startup
        FileIO.instance().JSONToFile(context,Photos,context.getString(R.string.photo_data_filename));

        // send photos list to Main
        MainActivity.instance().GetPhotos(Photos);
    }

}
