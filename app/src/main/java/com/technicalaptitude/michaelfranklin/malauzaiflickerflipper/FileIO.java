package com.technicalaptitude.michaelfranklin.malauzaiflickerflipper;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by Michael Franklin on 1/13/2016.
 */
public class FileIO {

    private static FileIO instance;

    public static final synchronized FileIO instance() {
        if (instance == null) {
            instance = new FileIO();
        }
        return instance;
    }
    public FileIO() {

    }

    public JSONArray GetJSONArrayFromJSONObjectsFile(Context context,String FileName)
    {
        JSONArray data = new JSONArray();


        try {
            InputStream inputStream = context.openFileInput(FileName);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                    data.put(new JSONObject(receiveString));
                }

                inputStream.close();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("FileIO", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("FileIO", "Can not read file: " + e.toString());
        }
        catch (Exception e) {
            Log.e("FileIO", e.toString());
        }

        return data;
    }

    public JSONArray ReadInJSONArrayFromFile(Context context,String FileName)
    {

        String ret = "";
        JSONArray Data = null;
        try {
            InputStream inputStream = context.openFileInput(FileName);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();

                Data = new JSONArray(ret);
            }
        }
        catch (FileNotFoundException e) {
            Log.e("FileIO", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("FileIO", "Can not read file: " + e.toString());
        }
        catch (Exception e) {
            Log.e("FileIO", e.toString());
        }

        return Data;
    }
    public JSONObject ReadInJSONObjectFromFile(Context context,String FileName)
    {

        String ret = "";
        JSONObject Data = null;
        try {
            InputStream inputStream = context.openFileInput(FileName);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();

                Data = new JSONObject(ret);
            }
        }
        catch (FileNotFoundException e) {
            Log.e("FileIO", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("FileIO", "Can not read file: " + e.toString());
        }
        catch (Exception e) {
            Log.e("FileIO", e.toString());
        }

        return Data;
    }
    public void AppendJSONToFile(Context context, Object Data, String FileName)
    {
        try
        {
            // write data to file
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(FileName, Context.MODE_PRIVATE | Context.MODE_APPEND));
            outputStreamWriter.write(Data.toString());
            outputStreamWriter.close();
        } catch (Exception e)
        {
            Log.d("FileIO.UpdateLayouts", e.toString());
        }
    }
    public void JSONToFile(Context context, Object Data, String FileName)
    {
        try
        {
            // write data to file
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(FileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(Data.toString());
            outputStreamWriter.close();
        }
        catch (Exception e)
        {
            Log.d("FileIO.UpdateLayouts", e.toString());
        }

    }


    public void DeleteFiles(Context context){
        // get application data directory
        File appDir = new File(context.getFilesDir().toString());
        if (!appDir.exists())
        {
            appDir.mkdirs();
        }
        // get a list of files in data directory
        File[] files = appDir.listFiles();

        // delete all files
        try
        {
            if (files != null)
            {
                // for each file in data directory
                for (File f : appDir.listFiles()) {
                    if (f.isFile())
                    {
                        f.delete(); // Delete
                    }
                }
            }
        }
        catch (Exception e)
        {
            Log.d("FileIO.Delete",e.toString());
        }
    }
}
