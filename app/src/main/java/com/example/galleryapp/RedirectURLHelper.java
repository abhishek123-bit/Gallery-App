package com.example.galleryapp;

import android.os.AsyncTask;
import java.net.HttpURLConnection;
import java.net.URL;

public class RedirectURLHelper extends AsyncTask<String, Void, Void> {

    private OnCompleteListener listener;


    /**
     * @param listener OnComplete handler
     */
    public RedirectURLHelper(OnCompleteListener listener) {
        this.listener = listener;
    }


    @Override
    protected Void doInBackground(String... strings) {
        try {
            URL imageUrl = new URL(strings[0]);
            HttpURLConnection  httpURLConnection = (HttpURLConnection) imageUrl.openConnection();
            httpURLConnection.getResponseCode();

            httpURLConnection.disconnect();
            listener.fetchRedirectUrl(httpURLConnection.getURL().toString());
        } catch (Exception e) {
            listener.OnFail();
        }

        return null;
    }


    interface OnCompleteListener {
        void fetchRedirectUrl(String s);

        void OnFail();
    }
}
