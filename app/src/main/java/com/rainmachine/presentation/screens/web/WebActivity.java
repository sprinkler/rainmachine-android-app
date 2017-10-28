package com.rainmachine.presentation.screens.web;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.rainmachine.R;
import com.rainmachine.presentation.activities.SprinklerActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WebActivity extends SprinklerActivity {

    private static final String EXTRA_URL = "url";

    @BindView(R.id.web_view)
    WebView webView;
    @BindView(R.id.progress_view)
    View progressView;

    public static Intent getStartIntent(Context context, String url) {
        Intent intent = new Intent(context, WebActivity.class);
        intent.putExtra(EXTRA_URL, url);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!buildGraphAndInject()) {
            return;
        }
        setContentView(R.layout.activity_web);
        ButterKnife.bind(this);

        String url = getIntent().getStringExtra(EXTRA_URL);
        setup(url);
    }

    @Override
    public Object getModule() {
        return new WebModule();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setup(String url) {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebClient());
        webView.loadUrl(url);
    }

    private class WebClient extends WebViewClient {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            final Uri uri = request.getUrl();
            view.loadUrl(uri.toString());
            return false;
        }

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressView.setVisibility(View.GONE);
        }
    }
}
