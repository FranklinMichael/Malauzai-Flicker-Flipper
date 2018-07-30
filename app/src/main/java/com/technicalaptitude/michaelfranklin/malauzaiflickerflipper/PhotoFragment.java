package com.technicalaptitude.michaelfranklin.malauzaiflickerflipper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;

public class PhotoFragment extends Fragment {
    private static final String ARG_Title = "title";
    private static final String ARG_Index = "index";

    private String Title;
    private Bitmap Image;
    private int ImageIndex;

    private TextView TitleView;
    private ImageView PhotoView;
    private LinearLayout DownloadingMessage;
    private ProgressBar DownloadProgress;

    public PhotoFragment() {}

    public static PhotoFragment newInstance(String param1, int param2) {
        PhotoFragment fragment = new PhotoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_Title, param1);
        args.putInt(ARG_Index, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Title = getArguments().getString(ARG_Title);
            ImageIndex = getArguments().getInt(ARG_Index);
            Image = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_photo, container, false);
        TitleView = v.findViewById(R.id.ImageTitle);
        TitleView.setText(Title);
        DownloadingMessage = v.findViewById(R.id.DownloadingMessage);
        DownloadingMessage.setVisibility(View.GONE);
        DownloadProgress = v.findViewById(R.id.DownloadProgressBar);
        PhotoView = v.findViewById(R.id.ImagePhoto);
        PhotoView.setVisibility(View.GONE);

        // check for file
        String FileName = getActivity().getFilesDir().toString() + "/"  + ImageIndex + ".jpg";
        File f = new File(FileName);
        if (f.exists() && f.isFile())
        {
            // we have file. show it
            AddImage();
        }
        // if we have no image
        if (Image == null)
        {
            // show downloading message
            DownloadingMessage.setVisibility(View.VISIBLE);
        }
        else
        {
            // show image
            PhotoView.setImageBitmap(Image);
            PhotoView.setVisibility(View.VISIBLE);
        }
        return v;
    }

    // Called when image is downloaded
    public void AddImage()
    {
        // load image from file
        this.Image = decodeSampledBitmapFromFile(getActivity().getFilesDir() + "/" + ImageIndex + ".jpg",300,300);
        if (PhotoView != null)
        {
            PhotoView.setImageBitmap(Image);
        }

        // hide downloading message if need be
        if (DownloadingMessage != null && DownloadingMessage.getVisibility() == View.VISIBLE)
        {
            DownloadingMessage.setVisibility(View.GONE);
            PhotoView.setVisibility(View.VISIBLE);
        }
    }
    public void DownloadUpdate(int Percent)
    {
        DownloadProgress.setProgress(Percent);
    }
    private static Bitmap decodeSampledBitmapFromFile(String fileName, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileName, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(fileName, options);

    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

}
