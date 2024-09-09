package com.example.rastreioaluno2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CreateActivity extends AppCompatActivity {

    private EditText trackingNameEditText, schoolAddressEditText, homeAddressEditText, transportUidEditText, studentUidEditText;
    private Spinner transportSpinner;
    private MaterialButton registerTrackingButton;
    private ImageButton backButton;
    private DatabaseReference usersRef;
    private List<String> transportUserNames;
    private List<String> transportUserShortIds;
    private ArrayAdapter<String> spinnerAdapter;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        trackingNameEditText = findViewById(R.id.tracking_name);
        schoolAddressEditText = findViewById(R.id.school_address);
        homeAddressEditText = findViewById(R.id.home_address);
        transportUidEditText = findViewById(R.id.transport_uid);
        studentUidEditText = findViewById(R.id.student_uid);
        transportSpinner = findViewById(R.id.transport_spinner);
        registerTrackingButton = findViewById(R.id.register_tracking_button);
        backButton = findViewById(R.id.back_button);

        // Initialize Firebase Authentication and Database reference
        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        transportUserNames = new ArrayList<>();
        transportUserShortIds = new ArrayList<>();

        // Populate the Spinner
        populateTransportSpinner();

        // Set click listener for the register button
        registerTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerTracking();
            }
        });

        // Set click listener for the back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateActivity.this, HomeActivity.class);
                startActivity(intent);
                finish(); // Finish this activity
            }
        });

        // Set Spinner item selected listener
        transportSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < transportUserShortIds.size()) {
                    transportUidEditText.setText(transportUserShortIds.get(position));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void populateTransportSpinner() {
        // Get transport users from the database and populate spinner
        usersRef.orderByChild("role").equalTo("transport").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                transportUserNames.clear();
                transportUserShortIds.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String shortId = snapshot.child("shortId").getValue(String.class);

                    if (name != null && shortId != null) {
                        transportUserNames.add(name);
                        transportUserShortIds.add(shortId);
                    }
                }

                spinnerAdapter = new ArrayAdapter<>(CreateActivity.this, android.R.layout.simple_spinner_item, transportUserNames);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                transportSpinner.setAdapter(spinnerAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CreateActivity.this, "Failed to load transport users.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerTracking() {
        String trackingName = trackingNameEditText.getText().toString().trim();
        String schoolAddress = schoolAddressEditText.getText().toString().trim();
        String homeAddress = homeAddressEditText.getText().toString().trim();
        String transportUid = transportUidEditText.getText().toString().trim();
        String studentUid = studentUidEditText.getText().toString().trim();

        if (trackingName.isEmpty() || schoolAddress.isEmpty() || homeAddress.isEmpty() || transportUid.isEmpty() || studentUid.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save tracking info to the database (example implementation)
        DatabaseReference trackingRef = FirebaseDatabase.getInstance().getReference("trackings");
        String id = trackingRef.push().getKey();
        Tracking tracking = new Tracking(trackingName, schoolAddress, homeAddress, transportUid, studentUid);
        trackingRef.child(id).setValue(tracking);

        Toast.makeText(this, "Tracking registered successfully.", Toast.LENGTH_SHORT).show();

        // Clear input fields
        trackingNameEditText.setText("");
        schoolAddressEditText.setText("");
        homeAddressEditText.setText("");
        transportUidEditText.setText("");
        studentUidEditText.setText("");
    }

    private static class Tracking {
        public String trackingName;
        public String schoolAddress;
        public String homeAddress;
        public String transportUid;
        public String studentUid;

        public Tracking(String trackingName, String schoolAddress, String homeAddress, String transportUid, String studentUid) {
            this.trackingName = trackingName;
            this.schoolAddress = schoolAddress;
            this.homeAddress = homeAddress;
            this.transportUid = transportUid;
            this.studentUid = studentUid;
        }
    }
}