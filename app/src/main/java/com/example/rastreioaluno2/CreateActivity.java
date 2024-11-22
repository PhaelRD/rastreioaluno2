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
    private String trackingId; // ID do rastreio a ser editado
    private boolean isEditing = false; // Flag para verificar se está em modo de edição

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        // Initialize UI elements
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

        // Check if editing an existing tracking
        Intent intent = getIntent();
        trackingId = intent.getStringExtra("TRACKING_ID");
        if (trackingId != null) {
            isEditing = true;
            loadTrackingData(trackingId);
            registerTrackingButton.setText("Editar");
        }

        // Set click listener for the register/update button
        registerTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditing) {
                    updateTracking();
                } else {
                    registerTracking();
                }
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
        usersRef.orderByChild("info/userType").equalTo("Transporte Escolar").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear previous data
                transportUserNames.clear();
                transportUserShortIds.clear();

                // Iterate through each user in the snapshot
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String name = snapshot.child("info").child("name").getValue(String.class);
                    String shortId = snapshot.child("shortUserId").getValue(String.class);

                    // Ensure both name and shortId are not null
                    if (name != null && shortId != null) {
                        transportUserNames.add(name);
                        transportUserShortIds.add(shortId);
                    }
                }

                // Create and set the adapter for the spinner
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

        // Get current user UID
        String userId = auth.getCurrentUser().getUid();

        // Reference to the user's "trackings" node
        DatabaseReference trackingRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("trackings");
        String id = trackingRef.push().getKey();
        Tracking tracking = new Tracking(trackingName, schoolAddress, homeAddress, transportUid, studentUid);
        trackingRef.child(id).setValue(tracking)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Tracking registered successfully.", Toast.LENGTH_SHORT).show();

                        // Clear input fields
                        trackingNameEditText.setText("");
                        schoolAddressEditText.setText("");
                        homeAddressEditText.setText("");
                        transportUidEditText.setText("");
                        studentUidEditText.setText("");

                        // Go back to HomeActivity
                        Intent intent = new Intent(CreateActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish(); // Close the current activity
                    } else {
                        Toast.makeText(this, "Failed to register tracking.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadTrackingData(String trackingId) {
        String userId = auth.getCurrentUser().getUid();
        DatabaseReference trackingRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("trackings").child(trackingId);

        trackingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Tracking tracking = dataSnapshot.getValue(Tracking.class);
                    if (tracking != null) {
                        trackingNameEditText.setText(tracking.trackingName);
                        schoolAddressEditText.setText(tracking.schoolAddress);
                        homeAddressEditText.setText(tracking.homeAddress);
                        transportUidEditText.setText(tracking.transportUid);
                        studentUidEditText.setText(tracking.studentUid);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CreateActivity.this, "Failed to load tracking data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTracking() {
        String trackingName = trackingNameEditText.getText().toString().trim();
        String schoolAddress = schoolAddressEditText.getText().toString().trim();
        String homeAddress = homeAddressEditText.getText().toString().trim();
        String transportUid = transportUidEditText.getText().toString().trim();
        String studentUid = studentUidEditText.getText().toString().trim();

        if (trackingName.isEmpty() || schoolAddress.isEmpty() || homeAddress.isEmpty() || transportUid.isEmpty() || studentUid.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference trackingRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("trackings").child(trackingId);
        Tracking tracking = new Tracking(trackingName, schoolAddress, homeAddress, transportUid, studentUid);

        trackingRef.setValue(tracking)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Tracking updated successfully.", Toast.LENGTH_SHORT).show();
                        finish(); // Close activity and return to previous screen
                    } else {
                        Toast.makeText(this, "Failed to update tracking.", Toast.LENGTH_SHORT).show();
                    }
                });
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

        public Tracking() {
            // Default constructor required for calls to DataSnapshot.getValue(Tracking.class)
        }
    }
}
