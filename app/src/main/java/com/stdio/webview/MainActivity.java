package com.stdio.webview;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
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

import static com.stdio.webview.WebViewHelper.afterChosePic;
import static com.stdio.webview.WebViewHelper.cameraUri;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    public static final int REQUEST_CAMERA = 1;
    public static final int REQUEST_CHOOSE = 2;
    public static WebView mWebView;
    public static ProgressBar progressBar;

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
        mWebView = (WebView) findViewById(R.id.maim_web);
        mWebView.setWebViewClient(new MyWebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("tel:")) {
                    Intent intent = new Intent(Intent.ACTION_DIAL,
                            Uri.parse(url));
                    startActivity(intent);
                } else if (url.startsWith("whatsapp://send?phone=")) {
                    String url2 = "https://api.whatsapp.com/send?phone=" + url.replace("whatsapp://send?phone=", "");
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url2));
                    intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP)
                            .setPackage("com.whatsapp");
                    startActivity(intent);
                }
                else if (url.startsWith("whatsapp://send?text=")) {
                    Uri uri=Uri.parse(url);
                    String msg = uri.getQueryParameter("text");
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
                    sendIntent.setType("text/plain");
                    sendIntent.setPackage("com.whatsapp");
                    startActivity(sendIntent);
                }
                else if (url.startsWith("viber:")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(url));
                    startActivity(intent);
                }
                else if (url.startsWith("https://telegram.me/share")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(url));
                    startActivity(intent);
                }
                else if (url.startsWith("http:") || url.startsWith("https:")) {
                    view.loadUrl(url);
                }
                return true;
            }
        });
        mWebView.setWebChromeClient(new MyWebChromeClient(this, MainActivity.this));

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);//loads the WebView completely zoomed out
        mWebView.getSettings().setUseWideViewPort(true);//makes the Webview have a normal viewport (such as a normal desktop browser), while when false the webview will have a viewport constrained to its own dimensions (so if the webview is 50px*50px the viewport will be the same size)

        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setBuiltInZoomControls(true);//to remove the zoom buttons in webview
        mWebView.getSettings().setDisplayZoomControls(false);//to remove the zoom buttons in webview
        mWebView.loadUrl("https://vk.com");
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

        if (MyWebChromeClient.mUploadMessagesAboveL != null) {
            new WebViewHelper(this, MainActivity.this).onActivityResultAboveL(requestCode, resultCode, intent);
        }

        if (MyWebChromeClient.mUploadMessage == null) return;

        Uri uri = null;

        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            uri = cameraUri;
        }

        if (requestCode == REQUEST_CHOOSE && resultCode == RESULT_OK) {
            uri = afterChosePic(intent);
        }

        MyWebChromeClient.mUploadMessage.onReceiveValue(uri);
        MyWebChromeClient.mUploadMessage = null;
        super.onActivityResult(requestCode, resultCode, intent);
    }
}
