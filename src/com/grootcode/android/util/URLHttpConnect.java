package com.grootcode.android.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Util for making GET and POST requests.
 * 
 * @author ankit dimania
 */
public class URLHttpConnect {
    private static final String TAG = LogUtils.makeLogTag(URLHttpConnect.class);

    static {
        // Per
        // http://android-developers.blogspot.com/2011/09/androids-http-clients.html
        if (!UIUtils.hasFroyo()) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    private String mUserAgent;

    private static URLHttpConnect sURLHttpConnect;

    /**
     * Initialize the global instance. Should call only once.
     */
    public static synchronized void init(Context context) {
        if (sURLHttpConnect == null) {
            sURLHttpConnect = new URLHttpConnect(context);
        }
    }

    /**
     * Returns the global {@link com.grootcode.android.util.URLHttpConnect} singleton object, creating one
     * if necessary.
     */
    public static URLHttpConnect getInstance() {
        return sURLHttpConnect;
    }

    private URLHttpConnect(Context context) {
        // e.g. <App Name> (<package>/<version>) (gzip)
        mUserAgent = NetUtils.getUserAgent(context) + " (gzip)";
    }

    /**
     * Make a HTTP GET request to the given URL and return the response in
     * String
     * 
     * @param urlString
     *            URL to hit the POST request
     * @return The response of the GET request in String.
     * @throws java.io.IOException
     */
    public String httpGet(String urlString) throws IOException {
        LogUtils.LOGD(TAG, "Requesting URL: " + urlString);
        URL url = new URL(urlString);
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("User-Agent", mUserAgent);

            urlConnection.connect();
            throwErrors(urlConnection);

            String response = readInputStream(urlConnection.getInputStream());
            return response;
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
    }

    /**
     * Make a HTTP POST request to the given URL and return the response in
     * String
     * 
     * @param urlString
     *            URL to hit the POST request
     * @param paramName
     *            Array of all parameter names
     * @param paramVal
     *            Array of all parameter values
     * @return The response of the POST request in String.
     * @throws java.net.MalformedURLException
     * @throws java.io.IOException
     */
    public String httpPost(String urlString, String[] paramName, String[] paramVal) throws MalformedURLException,
            IOException {
        LogUtils.LOGD(TAG, "Requesting URL: " + urlString);
        URL url = new URL(urlString);
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setAllowUserInteraction(false);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setRequestProperty("User-Agent", mUserAgent);

            // Create the form content
            Writer writer = null;
            try {
                writer = new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8");
                for (int i = 0; i < paramName.length; i++) {
                    writer.write(paramName[i]);
                    writer.write("=");
                    writer.write(URLEncoder.encode(paramVal[i], "UTF-8"));
                    writer.write("&");
                }
            } finally {
                if (writer != null)
                    writer.close();
            }

            // urlConnection.connect();
            throwErrors(urlConnection);

            String response = readInputStream(urlConnection.getInputStream());
            return response;
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
    }

    private void throwErrors(HttpURLConnection urlConnection) throws IOException {
        final int status = urlConnection.getResponseCode();
        if (status < 200 || status >= 300) {
            String errorMessage = urlConnection.getResponseMessage();

            String exceptionMessage = "Error response " + status + " " + urlConnection.getResponseMessage()
                    + (errorMessage == null ? "" : (": " + errorMessage)) + " for " + urlConnection.getURL();

            throw new IOException(exceptionMessage);
        }
    }

    private static String readInputStream(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String responseLine;
            StringBuilder responseBuilder = new StringBuilder();
            while ((responseLine = bufferedReader.readLine()) != null) {
                responseBuilder.append(responseLine).append('\n');
            }
            return responseBuilder.toString();
        } finally {
            if (bufferedReader != null)
                bufferedReader.close();
        }
    }
}
