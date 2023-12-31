package com.example.teamproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class shop extends AppCompatActivity {

    private int balance = 100;  // Initial balance, you can set it based on your requirement
    private TextView balanceTextView;

    private ImageButton buttonShopBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.shop_layout);

        // Initialize balance TextView
        balanceTextView = findViewById(R.id.balanceTextView);
        buttonShopBack = findViewById(R.id.buttonShopBack);

        // Set up click listeners for items
        setUpItemClickListener(R.id.item1Layout, 10);  // 10 is the price decrement for Item 1
        // Repeat the above line for Item 2 to Item 5

        buttonShopBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(shop.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }



    private void setUpItemClickListener(int itemId, final int price) {
        LinearLayout itemLayout = findViewById(itemId);
        itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Perform item purchase logic
                if (balance >= price) {
                    celebratePurchase();
                    balance -= price;
                    updateBalance();
                } else {
                    // Display insufficient funds message or handle accordingly
                }
            }
        });
    }

    private void celebratePurchase() {
        // Implement your celebration animation or effect here
        // For example, you can use a library like Lottie for animations
    }

    private void updateBalance() {
        balanceTextView.setText("Balance: $" + balance);
    }
}
