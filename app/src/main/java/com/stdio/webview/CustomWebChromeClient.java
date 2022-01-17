package com.stdio.webview;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomWebChromeClient extends WebChromeClient {

    private Activity context;
    private static String file_type     = "*/*";    // file types to be allowed for upload
    private boolean multiple_files = true;         // allowing multiple file upload
    public static String cam_file_data = null;        // for storing camera file information
    public static ValueCallback<Uri> file_data;       // data/header received after file selection
    public static ValueCallback<Uri[]> file_path;     // received file(s) temp. location
    public final static int file_req_code = 1;
    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    protected FrameLayout mFullscreenContainer;
    private int mOriginalOrientation;
    private int mOriginalSystemUiVisibility;

    public CustomWebChromeClient(Activity context) {
        this.context = context;
    }

    public Bitmap getDefaultVideoPoster()
    {
        if (mCustomView == null) {
            return null;
        }
        return BitmapFactory.decodeResource(context.getResources(), 2130837573);
    }

    public void onHideCustomView()
    {
        ((FrameLayout)context.getWindow().getDecorView()).removeView(this.mCustomView);
        this.mCustomView = null;
        context.getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
        context.setRequestedOrientation(this.mOriginalOrientation);
        this.mCustomViewCallback.onCustomViewHidden();
        this.mCustomViewCallback = null;
    }

    public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallback)
    {
        if (this.mCustomView != null)
        {
            onHideCustomView();
            return;
        }
        this.mCustomView = paramView;
        this.mOriginalSystemUiVisibility = context.getWindow().getDecorView().getSystemUiVisibility();
        this.mOriginalOrientation = context.getRequestedOrientation();
        this.mCustomViewCallback = paramCustomViewCallback;
        ((FrameLayout)context.getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
        context.getWindow().getDecorView().setSystemUiVisibility(3846 | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    /*-- handling input[type="file"] requests for android API 21+ --*/
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {

        if (file_permission() && Build.VERSION.SDK_INT >= 21) {
            file_path = filePathCallback;
            Intent takePictureIntent = null;
            Intent takeVideoIntent = null;

            boolean includeVideo = false;
            boolean includePhoto = false;

            /*-- checking the accept parameter to determine which intent(s) to include --*/
            paramCheck:
            for (String acceptTypes : fileChooserParams.getAcceptTypes()) {
                String[] splitTypes = acceptTypes.split(", ?+"); // although it's an array, it still seems to be the whole value; split it out into chunks so that we can detect multiple values
                for (String acceptType : splitTypes) {
                    switch (acceptType) {
                        case "*/*":
                            includePhoto = true;
                            includeVideo = true;
                            break paramCheck;
                        case "image/*":
                            includePhoto = true;
                            break;
                        case "video/*":
                            includeVideo = true;
                            break;
                    }
                }
            }

            if (fileChooserParams.getAcceptTypes().length == 0) {   //no `accept` parameter was specified, allow both photo and video
                includePhoto = true;
                includeVideo = true;
            }

            if (includePhoto) {
                takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = create_image();
                        takePictureIntent.putExtra("PhotoPath", cam_file_data);
                    } catch (IOException ex) {
                        Log.e("webViewLog", "Image file creation failed", ex);
                    }
                    if (photoFile != null) {
                        cam_file_data = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    } else {
                        cam_file_data = null;
                        takePictureIntent = null;
                    }
                }
            }

            if (includeVideo) {
                takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if (takeVideoIntent.resolveActivity(context.getPackageManager()) != null) {
                    File videoFile = null;
                    try {
                        videoFile = create_video();
                    } catch (IOException ex) {
                        Log.e("webViewLog", "Video file creation failed", ex);
                    }
                    if (videoFile != null) {
                        cam_file_data = "file:" + videoFile.getAbsolutePath();
                        takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(videoFile));
                    } else {
                        cam_file_data = null;
                        takeVideoIntent = null;
                    }
                }
            }

            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType(file_type);
            if (multiple_files) {
                contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            }

            Intent[] intentArray;
            if (takePictureIntent != null && takeVideoIntent != null) {
                intentArray = new Intent[]{takePictureIntent, takeVideoIntent};
            } else if (takePictureIntent != null) {
                intentArray = new Intent[]{takePictureIntent};
            } else if (takeVideoIntent != null) {
                intentArray = new Intent[]{takeVideoIntent};
            } else {
                intentArray = new Intent[0];
            }

            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "File chooser");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
            context.startActivityForResult(chooserIntent, file_req_code);
            return true;
        } else {
            return false;
        }
    }

    public boolean file_permission() {
        if (Build.VERSION.SDK_INT >= 23 && (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
            return false;
        } else {
            return true;
        }
    }

    /*-- creating new image file here --*/
    private File create_image() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "img_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    /*-- creating new video file here --*/
    private File create_video() throws IOException {
        @SuppressLint("SimpleDateFormat")
        String file_name = new SimpleDateFormat("yyyy_mm_ss").format(new Date());
        String new_name = "file_" + file_name + "_";
        File sd_directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(new_name, ".3gp", sd_directory);
    }
}
