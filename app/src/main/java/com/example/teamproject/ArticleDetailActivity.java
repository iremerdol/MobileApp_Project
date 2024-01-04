package com.example.teamproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ArticleDetailActivity extends Activity {

    private TextView textViewTitle;
    private TextView textViewComments;
    private int postId;

    private ImageButton buttonArticleDetailBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        textViewTitle = findViewById(R.id.textViewTitle);
        textViewComments = findViewById(R.id.textViewComments);
        buttonArticleDetailBack = findViewById(R.id.buttonArticleDetailBack);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        postId = intent.getIntExtra("postId", -1);

        textViewTitle.setText(title);

        fetchComments();

        buttonArticleDetailBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void fetchComments() {
        new Thread(() -> {
            try {
                URL url = new URL("https://my-json-server.typicode.com/Feel02/TrialData/comments");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                JSONArray jsonArray = new JSONArray(response.toString());
                String comment = "";

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getInt("postId") == postId) {
                        comment = jsonObject.getString("body");
                        break; // Break the loop once the matching comment is found
                    }
                }

                final String commentStr = comment;
                runOnUiThread(() -> textViewComments.setText(commentStr));

                reader.close();
                connection.disconnect();

            } catch (IOException | JSONException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ArticleDetailActivity.this, "Error fetching comment", Toast.LENGTH_LONG).show());
            }
        }).start();
    }

}
