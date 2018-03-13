package com.ale.proserv.geoapp3.services;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewFragment;

/**
 * Created by vaymonin on 09/02/2018
 */

public class WebFragment extends WebViewFragment {

    private String url;

    public void setUrl(String url){
        this.url = url;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater,container,savedInstanceState);
        WebView webView = getWebView();
        if(url == null){
            url = "https://www.al-enterprise.com";
        }
        webView.setWebViewClient(new WebViewClient()
        {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                Log.i("Browser","Loading");
                view.loadUrl(url);
                return true;
            }
        });
        webView.loadUrl(url);
        webView.getSettings().setJavaScriptEnabled(true);
        return view;
    }
}
