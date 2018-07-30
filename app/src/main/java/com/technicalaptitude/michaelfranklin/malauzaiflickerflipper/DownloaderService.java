package com.technicalaptitude.michaelfranklin.malauzaiflickerflipper;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/*
Put This in AndroidManifest.xml
 <service
    android:name=".DownloaderService">
</service>
<receiver
    android:name=".DownloaderReceiver">
    <intent-filter>
        <action android:name="MichaelFranklin.Library.DownloaderService.broadcast" />
    </intent-filter>
</receiver>
*/


/**
 * Created by Michael Franklin on 2/09/2016.
 */
public class DownloaderService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_DOWNLOAD = "com.telvida.cruxplayer.action.DOWNLOAD";

    // TODO: Rename parameters
    private static final String EXTRA_URL = "com.telvida.cruxplayer.extra.URL";
    private static final String EXTRA_FILEPATH = "com.telvida.cruxplayer.extra.FILEPATH";
    private static final String EXTRA_FILECHECKSUM = "com.telvida.cruxplayer.extra.FILECHECKSUM";
    private int PastPercentage;
    private int TotalPercentageAfterDownload = 90;
    public static final String RESPONSE = "MichaelFranklin.Library.DownloaderService.response";
    public static final String BROADCAST = "malauzaiflickerflipper.MichaelFranklin.Library.DownloaderService.broadcast";
    private static int TotalFails = 0;
    private int FileFails = 0;
    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionDownload(Context context, String Url, String FilePath, String FileChecksum) {
        Intent intent = new Intent(context, DownloaderService.class);
        intent.setAction(ACTION_DOWNLOAD);
        intent.putExtra(EXTRA_URL, Url);
        intent.putExtra(EXTRA_FILEPATH, FilePath);
        intent.putExtra(EXTRA_FILECHECKSUM, FileChecksum);
        context.startService(intent);
    }
    public static void resetDownloadFails()
    {
        TotalFails = 0;
    }

    public DownloaderService() {
        super("DownloaderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DOWNLOAD.equals(action)) {
                final String Url = intent.getStringExtra(EXTRA_URL);
                final String FilePath = intent.getStringExtra(EXTRA_FILEPATH);
                final String FileChecksum = intent.getStringExtra(EXTRA_FILECHECKSUM);
                handleActionDownload(Url, FilePath, FileChecksum);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionDownload(String Url, String FilePath, String FileChecksum) {
        PastPercentage = -1;
        FileFails = 0;
        Intent intent = new Intent(BROADCAST);
        JSONObject ResJSON = new JSONObject();
        try
        {
            boolean FileVerified = DownloadLooper(Url,FilePath,FileChecksum);

            ResJSON.put("URL",Url);
            ResJSON.put("Verified", FileVerified);
            ResJSON.put("ResType", "Complete");

        }
        catch (Exception e)
        {
            Log.d("Downloader.Download", e.toString());
            // file download failed
            TotalFails++;
            FileFails++;
            try
            {
                // create response
                ResJSON.put("URL",Url);
                ResJSON.put("TotalFailed", TotalFails);
                ResJSON.put("FileFails", FileFails);
                ResJSON.put("ResType", "Failed");
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
            }
        }
        intent.putExtra(RESPONSE, ResJSON.toString());
        sendBroadcast(intent);

    }

    private void DownloadFile(String Url, String FileName)
            throws Exception
    {
        URL url = new URL(Url);

        // open url connection
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setConnectTimeout(15000);
        urlConnection.setReadTimeout(10000);
        urlConnection.setRequestMethod("GET");
        urlConnection.setDoInput(true);
        urlConnection.connect();
        // download file
        int fileLength = urlConnection.getContentLength();
        FileOutputStream fos = openFileOutput(FileName, Context.MODE_PRIVATE);
        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));

        StringBuilder sb = new StringBuilder();

        byte[] buffer = new byte[8192];
        int len;
        long total = 0;
        while ((len = in.read(buffer)) != -1) {
            // the buffer, not the whole buffer
            fos.write(buffer, 0, len);  //  file to save app

            total += len;
            if (fileLength > 0) // only if total length is known
            {
                Intent intent = new Intent(BROADCAST);
                JSONObject ResJSON = new JSONObject();
                int Percent = -1;
                try
                {

                    float test1 = (float)total/fileLength;
                    Percent = (int)(test1 * TotalPercentageAfterDownload);
                    ResJSON.put("URL",Url);
                    ResJSON.put("Percent",Percent);
                    ResJSON.put("ResType","Update");
                }
                catch (Exception e)
                {
                    Log.d("Downloader.Download",e.toString());
                }

                // if percent downloaded has changed
                if (PastPercentage != Percent)
                {
                    // set PastPercentage to current Percent
                    PastPercentage = Percent;

                    // broadcast update
                    intent.putExtra(RESPONSE, ResJSON.toString());
                    sendBroadcast(intent);
                }
            }
        }

        //flush OutputStream to write any buffered data to file
        in.close();
        fos.flush();
        fos.close();
        urlConnection.disconnect();
    }

    private String GetFileChecksum(String FilePath)
            throws Exception
    {
        String output = null;
        // get checksum of temp file
        // convert temp file to string
        InputStream is = new FileInputStream(new File(FilePath));

        MessageDigest digest = null;
        try
        {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        byte[] buffer = new byte[8192];
        int read;
        try
        {
            while ((read = is.read(buffer)) > 0)
            {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');

        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to process file for MD5", e);
        }
        finally
        {
            is.close();
        }

        return output;
    }
    /*
    FilePath is relative to the Application Directory
     */
    private boolean DownloadLooper(String Url, String FilePath, String FileChecksum)
        throws Exception
    {

        try {
            String DownloadFilePath = null; // name of actual file to be downloaded
            String DownloadFileName = null;
            String FileName = null; // name of temp file
            // get filename
            if (FilePath.length()-1 == '/')
            {
                // FilePath is a directory path parse url to get filename
                DownloadFileName = Url.substring(Url.lastIndexOf('/')+1,Url.length());
                DownloadFilePath = FilePath + DownloadFileName;
            }
            else
            {
                // FilePath is a directory with a filename parse it to get filename
                DownloadFileName = FilePath.substring(FilePath.lastIndexOf('/')+1,FilePath.length());
                DownloadFilePath = FilePath;
            }

            // get extension of file mane
            String Ext = DownloadFileName.substring(DownloadFileName.lastIndexOf(".")+1,DownloadFileName.length());
            // build temp filename
            FileName = "temp." + Ext;
            String TempChecksum = null; // string ot of Temp file's Checksum
            // while both checksums do not equal eachother
            do
            {
                // check internet connection
                if (CommunicationWrapper.instance().CheckInternet(this)) // if we are connected continue with download
                {
                    // download file
                    DownloadFile(Url,FileName);

                    // get checksum of temp file
                    if (FileChecksum == null)
                    {
                        break; // file validation not needed leave loop
                    }
                    else
                    {
                        TempChecksum = GetFileChecksum(getFilesDir() + "/" + FileName);
                    }

                }
                else
                {
                    // break loop and exit download
                    return false;
                }
            }  while (!FileChecksum.equalsIgnoreCase(TempChecksum));
            // copy Temp file to Actual file
            InputStream is = openFileInput(FileName);
            // get length of downloaded file
            File Tempfile = new File(getFilesDir() + "/" + FileName);
            int fileLength = (int)Tempfile.length();

            // Trim FileName for directory Checking
            String DownloadFileDirectory = DownloadFilePath.substring(0,DownloadFilePath.lastIndexOf("/"));

            File contentDir = new File(DownloadFileDirectory);
            if (!contentDir.exists() );
            {
                contentDir.mkdirs();
            }


            FileOutputStream os = new FileOutputStream(DownloadFilePath);

            byte[] buffer = new byte[1024];
            int bytesRead;
            long total = 0;
            //read from is to buffer
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);

                total += bytesRead;

                if (fileLength > 0) // only if total length is known
                {
                    Intent intent = new Intent(BROADCAST);
                    JSONObject ResJSON = new JSONObject();
                    int Percent = -1;
                    try
                    {

                        float test1 = (float)total/fileLength;
                        Percent = (int)(test1 * (100 - TotalPercentageAfterDownload));
                        Percent += TotalPercentageAfterDownload;
                        ResJSON.put("URL",Url);
                        ResJSON.put("Percent",Percent);
                        ResJSON.put("ResType","Update");
                    }
                    catch (Exception e)
                    {
                        Log.d("Downloader.Download",e.toString());
                    }

                    // if percent downloaded has changed
                    if (PastPercentage != Percent)
                    {
                        // set PastPercentage to current Percent
                        PastPercentage = Percent;

                        // broadcast update
                        intent.putExtra(RESPONSE, ResJSON.toString());
                        sendBroadcast(intent);
                    }
                }
            }
            is.close();
            //flush OutputStream to write any buffered data to file
            os.flush();
            os.close();

            // delete temp file
            deleteFile(FileName);
            if (FileChecksum == null || FileChecksum.equals(""))
            {
                // no checksum file not verified
                return false;
            }
            // checksum passed file verified
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
