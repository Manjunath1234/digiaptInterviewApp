package com.gopal.mplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

//Created by Manjunath v M
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button record_btn, gallery_btn;
    static final int REQUEST_VIDEO_CAPTURE = 1;
    private static final int REQUEST_VIDEO_SELECT = 2;
    private VideoView videoView;
    private String ERROR_MSG = "TAG";

    private static final String VIDEO_DIRECTORY = "/test_delete_dir";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();


    }

    private void initViews() {
        record_btn = findViewById(R.id.vB_record);

        gallery_btn = findViewById(R.id.vB_selectFromGallery);
        videoView = findViewById(R.id.videoView);
        record_btn.setOnClickListener(this);
        gallery_btn.setOnClickListener(this);
    }


    private void dispatchTakeVideoIntent() {
        //intent to record video from camera//
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = intent.getData();
            mediaSetup(videoView, videoUri);
        } else if (requestCode == REQUEST_VIDEO_SELECT) {
            if ((intent.getData().toString().contains("video"))) {
                mediaSetup(videoView, intent.getData());
            }
        }
    }
/*Kept common for both*/
    private void mediaSetup(VideoView videoView, Uri videoUri) {
        final MediaController mediacontroller = new MediaController(this);
        mediacontroller.setAnchorView(videoView);
        videoView.setMediaController(mediacontroller);
        saveVideoToFile(videoUri.getPath());
        Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(getFilePath(videoUri),
                MediaStore.Images.Thumbnails.MINI_KIND);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(thumbnail);
        videoView.setBackgroundDrawable(bitmapDrawable);
        videoView.setVideoURI(videoUri);
        videoView.requestFocus();
        videoView.seekTo(1);


        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Toast.makeText(getApplicationContext(), "Video Over", Toast.LENGTH_SHORT).show();
                // mp.release();
                //mp.reset();


            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.e(ERROR_MSG, what + " ");
                return false;
            }
        });
    }

    private void saveVideoToFile(String filePath) {
        File newfile;

        try {

            File currentFile = new File(filePath);
            File wallpaperDirectory = new File(Environment.getExternalStorageDirectory() + VIDEO_DIRECTORY);
            newfile = new File(wallpaperDirectory, Calendar.getInstance().getTimeInMillis() + ".mp4");

            if (!wallpaperDirectory.exists()) {
                wallpaperDirectory.mkdirs();
            }

            if (currentFile.exists()) {

                InputStream in = new FileInputStream(currentFile);
                OutputStream out = new FileOutputStream(newfile);

                // Copy the bits from instream to outstream
                byte[] buf = new byte[1024];
                int len;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
                Log.v("vii", "Video file saved successfully.");
            } else {
                Log.v("vii", "Video saving failed. Source file missing.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFilePath(Uri videoUri) {

        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = getContentResolver().query(videoUri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;

    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.vB_record) {
            //  if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
            checkForCameraPermission();
            /*else
           dispatchTakeVideoIntent();*/
        } else if (v.getId() == R.id.vB_selectFromGallery) {
            selectFromGallery();
        }
    }

    private void checkForCameraPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_VIDEO_CAPTURE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            dispatchTakeVideoIntent();
        }

    }

    private void selectFromGallery() {
        /*intent to browse video from gallery*/
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_VIDEO_SELECT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_VIDEO_CAPTURE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    dispatchTakeVideoIntent();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

}
