package com.stdio.webview;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import static com.stdio.webview.MainActivity.TAG;

public class MyWebViewClient extends WebViewClient {

    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return handleUri(view, url);
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        return handleUri(view, url);
    }

    private boolean handleUri(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }

    public MyWebViewClient() {
        super();
    }


    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        Log.d(TAG, "URL地址:" + url);
        MainActivity.progressBar.setVisibility(View.VISIBLE);
        MainActivity.mWebView.setVisibility(View.GONE);
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        Log.i(TAG, "onPageFinished");
        CookieSyncManager.getInstance().sync();
        MainActivity.progressBar.setVisibility(View.GONE);
        MainActivity.mWebView.setVisibility(View.VISIBLE);
        super.onPageFinished(view, url);
    }

}