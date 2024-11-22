package com.example.rastreioaluno2;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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

    private FirebaseAuth mAuth;
    private DatabaseReference trackingsRef;
    private RecyclerView trackingRecyclerView;
    private TrackingAdapter trackingAdapter;
    private List<Tracking> trackingList;
    private List<Tracking> filteredList; // Lista filtrada
    private FloatingActionButton fab;
    private ImageButton logoutButton;
    private EditText searchEditText; // Campo de pesquisa

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initializeFirebase();
        initializeUI();
        initializeRecyclerView();
        initializeSwipeToDelete();
        initializeFab();
        initializeLogoutButton();
        initializeSearch();
        loadTrackings();
    }

    // Inicializa o Firebase e as referências necessárias
    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        trackingsRef = FirebaseDatabase.getInstance().getReference("users")
                .child(mAuth.getCurrentUser().getUid()).child("trackings");
    }

    // Inicializa os componentes da interface
    private void initializeUI() {
        trackingRecyclerView = findViewById(R.id.tracking_recycler_view);
        logoutButton = findViewById(R.id.logout_button);
        searchEditText = findViewById(R.id.search_edit_text); // Inicializando o campo de pesquisa
    }

    // Configura o RecyclerView
    private void initializeRecyclerView() {
        trackingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        trackingList = new ArrayList<>();
        filteredList = new ArrayList<>(); // Inicializando a lista filtrada
        trackingAdapter = new TrackingAdapter(filteredList);
        trackingRecyclerView.setAdapter(trackingAdapter);
    }

    // Configura o Floating Action Button (FAB) para criar novos rastreios
    private void initializeFab() {
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CreateActivity.class);
            startActivity(intent);
        });
    }

    // Configura o botão de logout
    private void initializeLogoutButton() {
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut(); // Deslogar o usuário
            Intent intent = new Intent(HomeActivity.this, MainActivity.class); // Redirecionar para a MainActivity
            startActivity(intent);
            finish(); // Finalizar a HomeActivity para evitar retorno
        });
    }

    // Configura o campo de pesquisa
    private void initializeSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                filterTrackings(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    // Filtra a lista de rastreios com base no texto da pesquisa
    private void filterTrackings(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(trackingList);
        } else {
            for (Tracking tracking : trackingList) {
                if (tracking.getTrackingName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(tracking);
                }
            }
        }
        trackingAdapter.notifyDataSetChanged();
        resetSwipeBackgroundAndIcons(); // Resetando o estilo dos itens após a pesquisa
    }

    // Método para resetar o fundo e ícones dos itens
    private void resetSwipeBackgroundAndIcons() {
        if (trackingRecyclerView != null) {
            // Percorre todos os itens do RecyclerView
            for (int i = 0; i < trackingRecyclerView.getChildCount(); i++) {
                View itemView = trackingRecyclerView.getChildAt(i);

                // Cria o fundo padrão (sem alteração)
                GradientDrawable backgroundDrawable = createRoundedBackground(Color.parseColor("#000000"));
                itemView.setBackground(backgroundDrawable);

                // Oculta os ícones de exclusão e edição
                itemView.findViewById(R.id.delete_icon).setVisibility(View.GONE);
                itemView.findViewById(R.id.edit_icon).setVisibility(View.GONE);
            }
        }
    }


    // Carrega os rastreios do Firebase
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
                filterTrackings(searchEditText.getText().toString()); // Refiltra com a pesquisa atual
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HomeActivity.this, "Failed to load trackings.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Configura a funcionalidade de deslizar para editar/excluir itens
    private void initializeSwipeToDelete() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Tracking tracking = filteredList.get(position); // Usa a lista filtrada

                if (direction == ItemTouchHelper.LEFT) {
                    // Deslizar para a esquerda - Edição
                    Intent intent = new Intent(HomeActivity.this, CreateActivity.class);
                    intent.putExtra("TRACKING_ID", tracking.getId());
                    startActivity(intent);
                } else if (direction == ItemTouchHelper.RIGHT) {
                    // Deslizar para a direita - Exclusão
                    new MaterialAlertDialogBuilder(HomeActivity.this)
                            .setTitle("Confirmar exclusão")
                            .setMessage("Você tem certeza que deseja excluir este rastreio?")
                            .setPositiveButton("Excluir", (dialog, which) -> deleteTracking(tracking.getId(), position))
                            .setNegativeButton("Cancelar", (dialog, which) -> trackingAdapter.notifyItemChanged(position))
                            .show();
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;

                GradientDrawable backgroundDrawable = createRoundedBackground(Color.parseColor("#000000"));
                float swipePercentage = Math.abs(dX) / itemView.getWidth();

                if (dX > 0) {
                    backgroundDrawable.setColor(Color.parseColor("#FF0000"));
                    itemView.setBackground(backgroundDrawable);
                    ImageView deleteIcon = itemView.findViewById(R.id.delete_icon);
                    deleteIcon.setVisibility(View.VISIBLE);
                    deleteIcon.setAlpha(swipePercentage >= 0.1f ? 1.0f : swipePercentage / 0.1f);
                } else if (dX < 0) {
                    backgroundDrawable.setColor(Color.parseColor("#008CFF"));
                    itemView.setBackground(backgroundDrawable);
                    ImageView editIcon = itemView.findViewById(R.id.edit_icon);
                    editIcon.setVisibility(View.VISIBLE);
                    editIcon.setAlpha(swipePercentage >= 0.1f ? 1.0f : swipePercentage / 0.1f);
                } else {
                    backgroundDrawable.setColor(Color.parseColor("#000000"));
                    itemView.setBackground(backgroundDrawable);
                    itemView.findViewById(R.id.delete_icon).setVisibility(View.GONE);
                    itemView.findViewById(R.id.edit_icon).setVisibility(View.GONE);
                }
            }
        });
        itemTouchHelper.attachToRecyclerView(trackingRecyclerView);
    }

    // Método para criar o fundo arredondado
    private GradientDrawable createRoundedBackground(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(8 * getResources().getDisplayMetrics().density);
        return drawable;
    }

    // Método para excluir um rastreio
    private void deleteTracking(String trackingId, int position) {
        trackingsRef.child(trackingId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                trackingList.removeIf(tracking -> tracking.getId().equals(trackingId)); // Remove da lista original
                filteredList.removeIf(tracking -> tracking.getId().equals(trackingId)); // Remove da lista filtrada
                trackingAdapter.notifyItemRemoved(position);
                Toast.makeText(HomeActivity.this, "Tracking deleted successfully.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(HomeActivity.this, "Failed to delete tracking.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Adapter do RecyclerView
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

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, TrackingActivity.class);
                intent.putExtra("TRACKING_ID", tracking.getId());
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return trackingList.size();
        }

        class TrackingViewHolder extends RecyclerView.ViewHolder {
            TextView trackingNameText;

            TrackingViewHolder(View itemView) {
                super(itemView);
                trackingNameText = itemView.findViewById(R.id.tracking_name);
            }
        }
    }

    // Classe Tracking
    private static class Tracking {
        private String id;
        private String trackingName;

        public Tracking() {
            // Construtor padrão necessário para a leitura do Firebase
        }

        public Tracking(String trackingName) {
            this.trackingName = trackingName;
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
