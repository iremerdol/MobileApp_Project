package com.example.teamproject;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ArticleAdapter extends IntentService {

    public ArticleAdapter() {
        super("DataService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // This method will be executed on a separate thread
        String urlStr = "https://my-json-server.typicode.com/Feel02/TrialData/posts"; // Replace with your API endpoint

        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set up the connection
            connection.setRequestMethod("GET");

            // Read the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            // Parse the JSON data (You may use a library like Gson for better parsing)
            JSONArray jsonArray = new JSONArray(response.toString());
            String jsonData = jsonArray.toString();

            Log.d("DataService", "JSON Data: " + jsonData);

            // Now, you can broadcast the obtained data to the MainActivity
            Intent newIntent = new Intent(this, ArticleListActivity.class);
            newIntent.putExtra("jsonData", jsonData);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Add this line
            startActivity(newIntent);

            // Close resources
            reader.close();
            connection.disconnect();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}

