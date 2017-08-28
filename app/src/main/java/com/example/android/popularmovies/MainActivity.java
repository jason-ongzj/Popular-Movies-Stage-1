package com.example.android.popularmovies;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.android.popularmovies.R.id.textOutput;

public class MainActivity extends AppCompatActivity implements ImageDisplayAdapter.ImageDisplayAdapterOnClickHandler{

    private TextView mTextOutput;
    private ImageDisplayAdapter mImageDisplayAdapter;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextOutput = (TextView) findViewById(textOutput);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView_display);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);

        mImageDisplayAdapter = new ImageDisplayAdapter(this);
        mImageDisplayAdapter.setContext(this);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;
        mImageDisplayAdapter.setImageSize(width, height);

        mRecyclerView.setAdapter(mImageDisplayAdapter);

        mRecyclerView.setHasFixedSize(true);

        String apiKey = "***REMOVED***";

        new RetrieveFeedTask().execute(apiKey);
    }

    @Override
    public void onDisplayImageClicked() {
        // Set activity intent
        Class destinationActivity = DetailActivity.class;
        Intent intent = new Intent(this, destinationActivity);
        startActivity(intent);
    }

    class RetrieveFeedTask extends AsyncTask<String, Void, String[]> {
        private static final String TAG = "AsyncTask";
        public static final String REQUEST_METHOD = "GET";
        public static final int READ_TIMEOUT = 15000;
        public static final int CONNECTION_TIMEOUT = 15000;
        private Exception exception;

        protected void onPreExecute() {
            mTextOutput.setText("");
        }

        protected String[] doInBackground(String... apiKey) {
            Log.d(TAG, "doInBackground: ");
            HttpURLConnection urlConnection = null;
            String urlString = "https://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc?";

            try {
                URL url = new URL(urlString + "&api_key=" + apiKey[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                // Add test for internet connection later

                urlConnection.setRequestMethod(REQUEST_METHOD);
                urlConnection.setReadTimeout(READ_TIMEOUT);
                urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
                int response = urlConnection.getResponseCode();
                Log.d(TAG, "doInBackground: The response code was " + response);

                urlConnection.connect();

                InputStreamReader streamReader = new InputStreamReader(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(streamReader);
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();
                streamReader.close();

                String[] result = MovieDataBaseUtils.getImageFromJson(MainActivity.this, stringBuilder.toString());
                return result;

            } catch(IOException e) {
                e.printStackTrace();
            } catch(Exception e){
                e.printStackTrace();
            } finally {
                if(urlConnection != null){
                    urlConnection.disconnect();
                }
            }
            return null;
        }

        protected void onPostExecute(String[] imageData) {
            if (imageData != null){
                mImageDisplayAdapter.setImageData(imageData);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }
}