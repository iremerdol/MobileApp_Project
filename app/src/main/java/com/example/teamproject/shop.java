package com.example.teamproject;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class shop extends AppCompatActivity {
    private Long balance = 0L;
    private long giftTimes = 0L;
    private TextView textBalanceShop,textGiftCount;
    private ImageButton buttonShopBack;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.shop_layout);

        textBalanceShop = findViewById(R.id.textBalanceShop);
        buttonShopBack = findViewById(R.id.buttonShopBack);
        textGiftCount = findViewById(R.id.textGiftCount);

        setUpItemClickListener(R.id.item1Layout, 500,1);  // 500 is the price decrement for Item 1
        setUpItemClickListener(R.id.item2Layout, 1000,3);  // 1000 is the price decrement for Item 2
        setUpItemClickListener(R.id.item3Layout, 1500,5);  // 1500 is the price decrement for Item 3

        db.collection("users").document(mAuth.getUid().toString())
                .get().addOnCompleteListener(
                        task -> {
                            if(task.isSuccessful()){
                                DocumentSnapshot document = task.getResult();
                                balance = document.getLong("balance");
                                giftTimes = document.getLong("gifts");
                                updateBalance();
                            }
                            else{
                                balance = 0L;
                                updateBalance();
                            }
                        }
                );

        buttonShopBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(shop.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }



    private void setUpItemClickListener(int itemId, final int price, final int gift) {
        LinearLayout itemLayout = findViewById(itemId);
        itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(balance >= price){
                    itemLayout.setBackgroundColor(getResources().getColor(R.color.clickedColor));
                    view.postDelayed(new Runnable() {
                        @Override
                        public void run(){
                            itemLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                        }
                    }, 200);

                    balance -= price;
                    giftTimes += gift;
                    db.collection("users").document(mAuth.getUid().toString())
                            .get().addOnCompleteListener(
                                    task -> {
                                        if(task.isSuccessful()){
                                            DocumentSnapshot document = task.getResult();
                                            Map<String, Object> user = new HashMap<>();
                                            user.put("email", document.getString("email"));
                                            user.put("pass", document.getString("pass"));
                                            user.put("balance", balance);
                                            user.put("gifts", giftTimes);
                                            db.collection("users").document(mAuth.getUid().toString()).set(user);
                                        }
                                    });
                    celebratePurchase();
                    updateBalance();

                }
                else{
                    Toast.makeText(shop.this, "Insufficient STY coin :(", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void celebratePurchase() {}

    private void updateBalance() {
        textBalanceShop.setText("Balance: " + balance + " STY");
        if(giftTimes > 0){
            textGiftCount.setText("You gifted " + giftTimes + " can of food to stray animals. They are thanking you so much.");
        }
    }
}
