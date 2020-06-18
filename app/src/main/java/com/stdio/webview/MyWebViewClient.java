package com.stdio.webview;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import static android.content.Context.MODE_PRIVATE;
import static com.stdio.webview.MainActivity.TAG;

public class MyWebViewClient extends WebViewClient {

    Activity context;

    public MyWebViewClient(Activity context) {
        this.context = context;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        Log.d(TAG, "onPageStarted: " + url);
        MainActivity.progressBar.setVisibility(View.VISIBLE);
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        Log.i(TAG, "onPageFinished");
        CookieSyncManager.getInstance().sync();
        MainActivity.progressBar.setVisibility(View.GONE);
        super.onPageFinished(view, url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url.startsWith("tel:")) {
            Intent intent = new Intent(Intent.ACTION_DIAL,
                    Uri.parse(url));
            context.startActivity(intent);
        } else if (url.startsWith("whatsapp://send?phone=")) {
            String url2 = "https://api.whatsapp.com/send?phone=" + url.replace("whatsapp://send?phone=", "");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url2));
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP)
                    .setPackage("com.whatsapp");
            try {
                context.startActivity(intent);
            }
            catch (ActivityNotFoundException e) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.whatsapp")));
                MainActivity.mWebView.goBack();
            }
        }
        else if (url.startsWith("https://api.whatsapp.com/send?phone=")) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP)
                    .setPackage("com.whatsapp");
            try {
                context.startActivity(intent);
            }
            catch (ActivityNotFoundException e) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.whatsapp")));
                MainActivity.mWebView.goBack();
            }
        }
        else if (url.startsWith("whatsapp://send?text=")) {
            Uri uri=Uri.parse(url);
            String msg = uri.getQueryParameter("text");
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, msg);
            sendIntent.setType("text/plain");
            sendIntent.setPackage("com.whatsapp");
            try {
                context.startActivity(sendIntent);
            }
            catch (ActivityNotFoundException e) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.whatsapp")));
                MainActivity.mWebView.goBack();
            }
        }
        else if (url.startsWith("viber:")) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(url));
            try {
                context.startActivity(intent);
            }
            catch (ActivityNotFoundException e) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.viber.voip")));
                MainActivity.mWebView.goBack();
            }
        }
        else if (url.startsWith("https://telegram.me")) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(url));
            try {
                context.startActivity(intent);
            }
            catch (ActivityNotFoundException e) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=org.telegram.messenger")));
                MainActivity.mWebView.goBack();
            }
        }
        else if (url.startsWith("http:") || url.startsWith("https:")) {
            view.loadUrl(url);
        }
        return true;
    }
}