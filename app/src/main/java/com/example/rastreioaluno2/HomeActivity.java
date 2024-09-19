package com.example.rastreioaluno2;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private Button logoutButton;
    private FirebaseAuth mAuth;
    private DatabaseReference trackingsRef;
    private RecyclerView trackingRecyclerView;
    private TrackingAdapter trackingAdapter;
    private List<Tracking> trackingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Inicializar FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        trackingsRef = FirebaseDatabase.getInstance().getReference("users").child(mAuth.getCurrentUser().getUid()).child("trackings");

        // Inicializar RecyclerView e Adapter
        trackingRecyclerView = findViewById(R.id.tracking_recycler_view);
        trackingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        trackingList = new ArrayList<>();
        trackingAdapter = new TrackingAdapter(trackingList);
        trackingRecyclerView.setAdapter(trackingAdapter);

        // Carregar rastreios
        loadTrackings();

        // Inicializar FloatingActionButton
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CreateActivity.class);
            startActivity(intent);
        });

        // Inicializar botão de Logout
        logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> logout());
    }

    private void loadTrackings() {
        trackingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                trackingList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Tracking tracking = snapshot.getValue(Tracking.class);
                    if (tracking != null) {
                        tracking.setId(snapshot.getKey());
                        trackingList.add(tracking);
                    }
                }
                trackingAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomeActivity.this, "Failed to load trackings.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteTracking(String trackingId) {
        trackingsRef.child(trackingId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(HomeActivity.this, "Tracking deleted successfully.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(HomeActivity.this, "Failed to delete tracking.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Finaliza a atividade atual
    }

    private class TrackingAdapter extends RecyclerView.Adapter<TrackingAdapter.TrackingViewHolder> {

        private final List<Tracking> trackingList;

        TrackingAdapter(List<Tracking> trackingList) {
            this.trackingList = trackingList;
        }

        @NonNull
        @Override
        public TrackingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tracking, parent, false);
            return new TrackingViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull TrackingViewHolder holder, int position) {
            Tracking tracking = trackingList.get(position);
            holder.trackingNameText.setText(tracking.getTrackingName());

            // Handle item click to navigate to TrackingActivity
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, TrackingActivity.class);
                intent.putExtra("TRACKING_ID", tracking.getId()); // Passa o ID do rastreio para a TrackingActivity
                startActivity(intent);
            });

            // Handle edit button click
            holder.editButton.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, CreateActivity.class);
                intent.putExtra("TRACKING_ID", tracking.getId()); // Passa o ID do rastreio para a CreateActivity
                startActivity(intent);
            });

            // Handle delete button click with confirmation dialog
            holder.deleteButton.setOnClickListener(v -> {
                new AlertDialog.Builder(HomeActivity.this)
                        .setTitle("Confirmar exclusão")
                        .setMessage("Você tem certeza de que deseja excluir este rastreio?")
                        .setPositiveButton("Excluir", (dialog, which) -> deleteTracking(tracking.getId()))
                        .setNegativeButton("Cancelar", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return trackingList.size();
        }

        class TrackingViewHolder extends RecyclerView.ViewHolder {
            TextView trackingNameText;
            ImageButton editButton, deleteButton;

            TrackingViewHolder(View itemView) {
                super(itemView);
                trackingNameText = itemView.findViewById(R.id.tracking_name);
                editButton = itemView.findViewById(R.id.edit_button);
                deleteButton = itemView.findViewById(R.id.delete_button);
            }
        }
    }


    private static class Tracking {
        private String id;
        private String trackingName;
        private String schoolAddress;
        private String homeAddress;
        private String transportUid;
        private String studentUid;

        public Tracking() {
            // Default constructor required for calls to DataSnapshot.getValue(Tracking.class)
        }

        public Tracking(String trackingName, String schoolAddress, String homeAddress, String transportUid, String studentUid) {
            this.trackingName = trackingName;
            this.schoolAddress = schoolAddress;
            this.homeAddress = homeAddress;
            this.transportUid = transportUid;
            this.studentUid = studentUid;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTrackingName() {
            return trackingName;
        }

        public void setTrackingName(String trackingName) {
            this.trackingName = trackingName;
        }
    }
}
