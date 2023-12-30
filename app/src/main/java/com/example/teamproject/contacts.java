package com.example.teamproject;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class contacts extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_READ_CONTACTS = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_layout);

        // Check if the READ_CONTACTS permission is not granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_CONTACTS},
                    PERMISSION_REQUEST_READ_CONTACTS);
        }
        else {
            // Permission is already granted, proceed with getting contacts
            loadContacts();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_READ_CONTACTS) {
            // If the request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with getting contacts
                loadContacts();
            } else {
                Toast.makeText(contacts.this, "Sorry to hear that.", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(contacts.this, MainActivity.class);
                startActivity(intent);
            }
        }
    }

    private void loadContacts() {


        // Get contacts from the phone
        ArrayList<String> contactsList = getContacts();

        // Set up the ListView with contacts
        ListView listViewContacts = findViewById(R.id.listViewContacts);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactsList);
        listViewContacts.setAdapter(adapter);

        // Handle item click to open SMS app
        listViewContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);

                // Extract the phone number from the selected item (you might need to parse it properly)
                String phoneNumber = getPhoneNumber(selectedItem);

                // Open SMS app with predefined text
                Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneNumber));
                smsIntent.putExtra("sms_body", "Hey look what I found!!! It's a app that designed for helping stray animals. Here's the link: https://www.youtube.com/watch?v=0tOXxuLcaog");
                startActivity(smsIntent);
            }
        });
    }

    private ArrayList<String> getContacts() {
        ArrayList<String> contactsList = new ArrayList<>();

        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.Contacts.DISPLAY_NAME
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String contactName = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                contactsList.add(contactName + ": " + phoneNumber);
            }
            cursor.close();
        }

        return contactsList;
    }

    private String getPhoneNumber(String contactItem) {
        // Parse the phone number from the contact item (you might need to implement this based on your actual data structure)
        return contactItem.split(":")[1].trim();
    }
}