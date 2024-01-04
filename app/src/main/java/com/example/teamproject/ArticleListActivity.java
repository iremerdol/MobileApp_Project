package com.example.teamproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ArticleListActivity extends Activity {

    private ListView listView;
    private ArrayList<String> articles;

    private ImageButton buttonArticleListBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);

        buttonArticleListBack = findViewById(R.id.buttonArticleListBack);

        listView = findViewById(R.id.listView);
        articles = new ArrayList<>();

        Intent intent = getIntent();
        String jsonData = intent.getStringExtra("jsonData");

        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String title = jsonObject.getString("title");
                articles.add(title);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        listView.setOnItemClickListener((adapterView, view, position, l) -> {
            String title = articles.get(position);
            Intent detailIntent = new Intent(ArticleListActivity.this, ArticleDetailActivity.class);
            detailIntent.putExtra("title", title);
            detailIntent.putExtra("postId", position + 1); // Ensure this matches with your JSON data
            startActivity(detailIntent);
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, articles);
        listView.setAdapter(adapter);

        buttonArticleListBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
