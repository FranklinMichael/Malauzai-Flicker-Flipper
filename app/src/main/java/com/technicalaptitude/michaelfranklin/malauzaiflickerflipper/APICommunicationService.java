package com.technicalaptitude.michaelfranklin.malauzaiflickerflipper;

/*
Put This in AndroidManifest.xml
 <service
    android:name=".APICommunicationService">
</service>
<receiver
    android:name=".APIResponseReceiver">
    <intent-filter>
        <action android:name="Package.MichaelFranklin.Library.APICommunicationService.broadcast" />
    </intent-filter>
</receiver>
*/

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Michael Franklin on 1/18/2016.
 */
public class APICommunicationService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_SEND = "SEND";

    // TODO: Rename parameters
    private static final String EXTRA_URL = "URL";
    private static final String EXTRA_DATA = "DATA";
    private static final String EXTRA_PACKAGEFOR = "PACKAGEFOR";
    private static final String EXTRA_TYPE = "TYPE";
    public static final String RESPONSE = "MichaelFranklin.Library.APICommunicationService.response";
    public static final String BROADCAST = "malauzaiflickerflipper.MichaelFranklin.Library.APICommunicationService.broadcast";
    public APICommunicationService() {
        super("APICommunicationService");
    }


    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionSend(Context context, String packageFor, String url, JSONObject data) {
        startActionSend(context, packageFor, url, data, 0);
    }
    public static void startActionSend(Context context, String packageFor, String url, JSONObject data, int SendType) {
        Intent intent = new Intent(context, APICommunicationService.class);
        intent.setAction(ACTION_SEND);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_PACKAGEFOR, packageFor);
        intent.putExtra(EXTRA_DATA, data.toString());
        intent.putExtra(EXTRA_TYPE, SendType);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null)
        {
            final Intent CmdIntent = intent;
            final String action = CmdIntent.getAction();
            if (ACTION_SEND.equals(action))
            {
                try
                {
                    final String Url = CmdIntent.getStringExtra(EXTRA_URL);
                    final JSONObject data = new JSONObject(CmdIntent.getStringExtra(EXTRA_DATA));
                    final String PackageFor = CmdIntent.getStringExtra(EXTRA_PACKAGEFOR);
                    final int Type = CmdIntent.getIntExtra(EXTRA_TYPE,0);
                    handleActionSend(PackageFor, Url, data, Type);
                }
                catch (Exception e)
                {
                    Log.d("APIComSer",e.toString());
                }
            }
        }
    }

    /**
     * Handle action Send in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSend(String PackageFor, String Url, JSONObject data, int SendType) {
        // build URL domain
        Uri.Builder Builder = new Uri.Builder();

        if (BuildConfig.DEBUG) {
            // use development domains
            Builder.scheme(getString(R.string.api_dev_scheme));
            Builder.authority(getString(R.string.api_dev_domain));
        }
        else
        {
            // use live domains
            Builder.scheme(getString(R.string.api_scheme));
            Builder.authority(getString(R.string.api_domain));
        }




        // append URL endpoint
        String AppendURL = Url;
        while (!AppendURL.equals(""))
        {
            if (AppendURL.contains("/"))
            {
                String SubUrl = AppendURL.substring(0, AppendURL.indexOf('/'));
                Builder.appendPath(SubUrl);
                AppendURL = AppendURL.substring(AppendURL.indexOf('/')+1,AppendURL.length());
            }
            else
            {
                Builder.appendPath(AppendURL);
                AppendURL = "";
            }
        }


        HttpURLConnection urlConnection = null; // holds URL Connection
        JSONObject ResJSON = new JSONObject(); // holds Response Data
        try
        {
            if (SendType == 0)
            {
                // Build Get URL
                // append Url Parameters
                Iterator iterator = data.keys();
                while (iterator.hasNext())
                {
                    String key = (String) iterator.next();
                    Builder.appendQueryParameter(key,data.getString(key).toString());
                }

                // send data GET
                URL url = new URL(Builder.build().toString());
                //URL url = new URL("http://www.google.com");
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(15000);
                urlConnection.setInstanceFollowRedirects(true);
                urlConnection.setRequestProperty("Access-Control-Allow-Headers", "Authorization");
                urlConnection.setRequestProperty("Accept","application/json");
                urlConnection.setRequestMethod("GET");

                urlConnection.setDoInput(true);
            }
            else
            {
                // Build Post URL
                // append Url Parameters
                Iterator iterator = data.keys();
                while (iterator.hasNext())
                {
                    String key = (String) iterator.next();
                    if (!(key.equals("FailedIntentPackageFor") || key.equals("FailedIntentData") || key.equals("FailedIntentUrl")))
                    {
                        Builder.appendQueryParameter(key,data.getString(key).toString());
                    }
                }
                // Get Post Query Parameters
                String urlParameters = Builder.build().getQuery();
                byte[] postData       = urlParameters.getBytes(StandardCharsets.UTF_8);
                int    postDataLength = postData.length;

                // Clear Query Paramenters
                Builder.clearQuery();

                // send data POST
                URL url = new URL(Builder.build().toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(15000);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty( "charset", "utf-8");
                urlConnection.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
                urlConnection.setDoInput(true);
                urlConnection.setDoOutput(true);

                //Send request
                DataOutputStream wr = new DataOutputStream(
                        urlConnection.getOutputStream ());
                wr.write(postData);
                wr.close ();
            }

            //Get Response
            InputStream is = urlConnection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();

            ResJSON = new JSONObject();
            ResJSON.put("data",response.toString());


        }
        catch (Exception e)
        {
            Map<String,List<String>> Headers = urlConnection.getHeaderFields();
            Log.d("APIComSer", e.toString());
        }

        int ResCode =0;
        boolean bValidToken = true; // set to false if token is invalid to prevent broadcasting
        String ResCategory = "Unhandled Error";
        String ResMsg = "Unknown Code";
        try
        {
            // get responsde code
            ResCode = urlConnection.getResponseCode();

            // place code in response category
            if (ResCode >= 200 && ResCode < 300)
            {
                // Create Response: OK
                ResCategory = getString(R.string.api_response_ok);
                ResMsg = "Data Received";
            }
            else if (ResCode >= 300 && ResCode < 500)
            {
                // Test for certain Error Cases
                switch (ResCode)
                {
                    case 404:
                        // Create Response: Not Found
                        ResCategory = getString(R.string.api_error_NotFound);
                        ResMsg = getString(R.string.api_error_NotFound) + ": " + urlConnection.getURL().toString();
                        break;
                    default:
                        // Create Response: UnHandled Error
                        ResCategory = getString(R.string.api_error_unhandled);
                        ResMsg = getString(R.string.api_error_unhandled) + "; " + ResCode;
                        break;
                }
            }
            else
            {
                // Create Response: UnHandled Error
                ResCategory = getString(R.string.api_error_unhandled);
                ResMsg = getString(R.string.api_error_unhandled) + "; " + ResCode;
            }
        }
        catch (UnknownHostException e)
        {
            Log.d("APIComSer", e.toString());
            // Create error message (debug only)
            ResCategory = getString(R.string.api_error_noHost);
            try
            {
                ResMsg = e.toString();
            }
            catch (Exception e2)
            {
                e2.toString();
            }
        }
        catch (SocketTimeoutException e)
        {
            Log.d("APIComSer", e.toString());
            // Create error message (debug only)
            ResCategory = getString(R.string.api_error_timedout);
            ResMsg = getString(R.string.api_error_timedout) + ": " + urlConnection.getURL().toString();
        }
        catch(Exception e)
        {
            Log.d("APIComSer", e.toString());
        }


        try
        {
            // package message
            ResJSON.put("Response Category", ResCategory);
            ResJSON.put("Response Message", ResMsg);
        }
        catch(Exception e)
        {
            Log.d("APIComSer", e.toString());
        }

        if (urlConnection != null)
        {
            urlConnection.disconnect();
        }

        // broadcast response
        Intent intent = new Intent(BROADCAST);
        intent.putExtra(RESPONSE, PackageResponse(PackageFor, ResJSON.toString()));
        sendBroadcast(intent);
    }

    private String PackageResponse(String PackageFor, String Response) {
        JSONObject ResJSON = null;
        try {
            // read in response
            ResJSON = new JSONObject(Response);
            // append Passed PackageFor string
            ResJSON.put("packageFor", PackageFor);


        } catch (Exception e) {
            Log.d("APIComSer", e.toString());
        }
        // output new JSON String
        return ResJSON.toString();
    }
}
