package com.technicalaptitude.michaelfranklin.malauzaiflickerflipper;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PagerManager extends Fragment {
    private static final String ARG_Photos = "Photos";
    private static int PageNum = 20;

    private JSONArray PhotosData;
    private List<Fragment> Photos = new ArrayList<>();
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    public PagerManager() {}

    public static PagerManager newInstance(String param1) {
        PagerManager fragment = new PagerManager();
        Bundle args = new Bundle();
        args.putString(ARG_Photos, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            try
            {
                PhotosData = new JSONArray(getArguments().getString(ARG_Photos));
                PageNum = PhotosData.length();


                // populate pager
                // for each photo
                for (int i = 0; i < PhotosData.length();i++)
                {

                    JSONObject PhotoData = PhotosData.getJSONObject(i);
                    // get photo title
                    String title = PhotoData.getString(getString(R.string.store_key_photo_title)) + ".jpg";

                    // add a new photo fragment
                    Photos.add(PhotoFragment.newInstance(title,i));

                    // start downloading photo
                    // check if file exists
                    String FileName = getActivity().getFilesDir().toString() + "/" + i + ".jpg";
                    File f = new File(FileName);
                    if (!f.exists() || !f.isFile())
                    {
                        // no image. download file
                        String Url = PhotoData.getString(getString(R.string.store_key_photo_image));

                        DownloaderService.startActionDownload(getActivity(),Url,FileName,null);
                    }
                }
            }
            catch (Exception e)
            {
                Log.d("PagerManager.onCreate:", e.toString());
            }
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pager_manager, container, false);


        // Instantiate a ViewPager and a PagerAdapter.
        mPager = v.findViewById(R.id.pager);
        mPagerAdapter = new PagerManagerAdapter(getFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        return v;
    }

    private class PagerManagerAdapter extends FragmentStatePagerAdapter {
        public PagerManagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // find fragment for this position
            return Photos.get(position);
        }

        @Override
        public int getCount() {
            return PageNum;
        }
    }

    // Downloader Callbacks
    private int GetPhotoIndexFromUrl(String Url)
    {
        try
        {
            for (int i = 0; i < PhotosData.length();i++)
            {
                if (PhotosData.getJSONObject(i).getString(getString(R.string.store_key_photo_image)).equals(Url))
                {
                    return i;
                }
            }
        }
        catch (Exception e)
        {
            Log.d("PagerMngr.PhotoFromUrl:", e.toString());
        }
        return -1;
    }
    public void DownloadComplete(String Url)
    {
        // get Fragment that belongs to the passed Url
        PhotoFragment Photo = (PhotoFragment)Photos.get(GetPhotoIndexFromUrl(Url));
        // send that download is complete
        Photo.AddImage();
    }
    public void DownloadFailed(String Url)
    {
        // get Fragment that belongs to the passed Url
        PhotoFragment Photo = (PhotoFragment)Photos.get(GetPhotoIndexFromUrl(Url));
        // send that download failed

    }
    public void DownloadUpdated(String Url, int Percent)
    {
        // get Fragment that belongs to the passed Url
        PhotoFragment Photo = (PhotoFragment)Photos.get(GetPhotoIndexFromUrl(Url));
        // send that download updated
        Photo.DownloadUpdate(Percent);
    }

}
