package com.stdio.webview;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.widget.ProgressBar;

import static com.stdio.webview.CustomWebChromeClient.cam_file_data;
import static com.stdio.webview.CustomWebChromeClient.file_data;
import static com.stdio.webview.CustomWebChromeClient.file_path;
import static com.stdio.webview.CustomWebChromeClient.file_req_code;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    public static final int REQUEST_CAMERA = 1;
    public static final int REQUEST_CHOOSE = 2;
    public static WebView mWebView;
    public static ProgressBar progressBar;
    private String URL_STRING = "https://vk.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progressBar);
        initView();
    }

    private void initView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE))
            { WebView.setWebContentsDebuggingEnabled(true); }
        }

        CookieManager.getInstance().setAcceptCookie(true);
        mWebView = findViewById(R.id.maim_web);
        mWebView.setWebViewClient(new MyWebViewClient(MainActivity.this));
        mWebView.setWebChromeClient(new CustomWebChromeClient(MainActivity.this));

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);//loads the WebView completely zoomed out
        mWebView.getSettings().setUseWideViewPort(true);//makes the Webview have a normal viewport (such as a normal desktop browser), while when false the webview will have a viewport constrained to its own dimensions (so if the webview is 50px*50px the viewport will be the same size)

        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setBuiltInZoomControls(true);//to remove the zoom buttons in webview
        mWebView.getSettings().setDisplayZoomControls(false);//to remove the zoom buttons in webview
        mWebView.loadUrl(URL_STRING);
    }

    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (Build.VERSION.SDK_INT >= 21) {
            Uri[] results = null;

            /*-- if file request cancelled; exited camera. we need to send null value to make future attempts workable --*/
            if (resultCode == Activity.RESULT_CANCELED) {
                if (requestCode == file_req_code) {
                    file_path.onReceiveValue(null);
                    return;
                }
            }

            /*-- continue if response is positive --*/
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == file_req_code) {
                    if (null == file_path) {
                        return;
                    }

                    ClipData clipData;
                    String stringData;
                    try {
                        clipData = intent.getClipData();
                        stringData = intent.getDataString();
                    } catch (Exception e) {
                        clipData = null;
                        stringData = null;
                    }

                    if (clipData == null && stringData == null && cam_file_data != null) {
                        results = new Uri[]{Uri.parse(cam_file_data)};
                    } else {
                        if (clipData != null) { // checking if multiple files selected or not
                            final int numSelectedFiles = clipData.getItemCount();
                            results = new Uri[numSelectedFiles];
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                results[i] = clipData.getItemAt(i).getUri();
                            }
                        } else {
                            results = new Uri[]{Uri.parse(stringData)};
                        }
                    }
                }
            }
            file_path.onReceiveValue(results);
            file_path = null;
        } else {
            if (requestCode == file_req_code) {
                if (null == file_data) return;
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                file_data.onReceiveValue(result);
                file_data = null;
            }
        }
    }
}
