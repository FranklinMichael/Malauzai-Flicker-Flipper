package com.technicalaptitude.michaelfranklin.malauzaiflickerflipper;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Michael Franklin on 1/23/2016.
 */
public class CommunicationWrapper {
    
    private static CommunicationWrapper instance;
    private boolean InternetConnected;

    // singleton calls
    public static final synchronized CommunicationWrapper instance() {
        if (instance == null) {
            instance = new CommunicationWrapper();
        }
        return instance;
    }
    private CommunicationWrapper()
    {

    }

    public void SendCommunicationRequest(Context context, final String Packagefor, final String Url, final JSONObject Request)
    {
        SendCommunicationRequest(context, Packagefor,Url,Request,0);
    }

    /*
    call when you wish to send a request to the API or through SMS if that fails
    Param 1: Unique Identifier for the Request (Refer to APIStrings)
    Param 2: Url Endpoint (not included domain) for API Request
    Param 3: JSON Object holding all data to be sent
    */
    public void SendCommunicationRequest(final Context context, final String Packagefor, final String Url, final JSONObject Request, final int APISendType)
    {
        Thread Communicate = new Thread(new Runnable() {
            @Override
            public void run() {
                // Check For Internet
                if (CheckInternet(context))
                {
                    // Send API Request
                    APICommunicationService.startActionSend(context,Packagefor,Url,Request,APISendType);
                }
                else
                {
                   ((Activity)context).runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           // send connection failed

                       }
                   });
                }
            }
        });

        try
        {
            Communicate.start();
        }
        catch (Exception e)
        {
            Log.d("Comunicate",e.toString());
        }
    }



    public boolean CheckInternet(final Context context)
    {
        // check network connection
        ConnectivityManager ConMgr = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo NetInfo = ConMgr.getActiveNetworkInfo();
        InternetConnected = (NetInfo != null && NetInfo.isConnected());

        Thread InternetCheck = new Thread(new Runnable() {
            @Override
            public void run() {

                // if we have an internet connection
                if (InternetConnected)
                {
                    // test internet properly using actual server
                    Uri.Builder Builder = new Uri.Builder();
                    if (BuildConfig.DEBUG)
                    {
                        Builder.scheme(context.getString(R.string.api_dev_scheme));
                    }
                    else
                    {
                        Builder.scheme(context.getString(R.string.api_scheme));
                    }

                    Builder.authority("www.google.com");


                    HttpURLConnection urlConnection = null;
                    try
                    {
                        URL url = new URL(Builder.build().toString());

                        // open url connection
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setConnectTimeout(10000);
                        urlConnection.setReadTimeout(10000);
                        urlConnection.setInstanceFollowRedirects(true);
                        urlConnection.setRequestMethod("GET");
                        urlConnection.setDoInput(true);
                        urlConnection.connect();
                    }
                    catch (Exception e)
                    {
                        Log.d("Comunicate.ChkInternet", "No Internet: " + e.toString());
                    }

                    try
                    {
                        // get response code
                        int ResCode = urlConnection.getResponseCode();

                        // place code in response category
                        if (ResCode >= 200 && ResCode < 300) {
                            // Connection truly successful
                            InternetConnected = true;
                        }
                        else
                        {
                            Log.d("Comunicate.ChkInternet","Response Code:" + ResCode);
                            // connection failed
                            InternetConnected = false;
                        }
                    }
                    catch (Exception e)
                    {
                        Log.d("Comunicate.ChkInternet",e.toString());

                        // Error getting Response code: connection failed
                        InternetConnected = false;
                    }

                    if (urlConnection != null)
                    {
                        urlConnection.disconnect();
                    }
                }
            }
        });

        try
        {
            InternetCheck.start();
            InternetCheck.join();
        }
        catch (Exception e)
        {
            Log.d("Comunicate.ChkInternet",e.toString());
        }


        return InternetConnected;
    }

}
