package com.example.rastreioaluno2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.MapView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;
import org.json.JSONException;

public class TrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextView trackingNameText, locationStatusText;  // Adiciona a referência ao TextView para status
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;
    private String trackingId;
    private Handler handler;
    private Runnable updateLocationRunnable;
    private MapView mapView;
    private GoogleMap googleMap;
    private LatLng studentLatLng, transportLatLng;
    private LatLng homeLatLng, schoolLatLng;
    private String homeAddress, schoolAddress;

    private static final String GEOCODE_URL = "https://maps.googleapis.com/maps/api/geocode/json";
    private static final String API_KEY = "AIzaSyBLmQmQfgd5Ka0wUQMADb9pylEm_M7jaio";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Inicializar Views
        trackingNameText = findViewById(R.id.tracking_name);
        locationStatusText = findViewById(R.id.location_status); // Referência ao TextView de status
        mapView = findViewById(R.id.map_view);
        ImageButton backButton = findViewById(R.id.back_button);

        // Back button functionality
        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(TrackingActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // Close the current activity
        });

        // Configurar o MapView
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Obter dados do Intent
        trackingId = getIntent().getStringExtra("TRACKING_ID");
        loadTrackingDetails();

        // Configurar Handler para atualizar localizações
        handler = new Handler();
        updateLocationRunnable = new Runnable() {
            @Override
            public void run() {
                updateLocations();
                handler.postDelayed(this, 5000); // Atualiza a cada 5 segundos
            }
        };
        handler.post(updateLocationRunnable); // Inicia o ciclo de atualizações
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        // Ativar controles de zoom no mapa
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setMapToolbarEnabled(true); // Habilita a barra de ferramentas do Google Maps (opcional)

        // Atualiza o mapa com as localizações
        updateMap();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && updateLocationRunnable != null) {
            handler.removeCallbacks(updateLocationRunnable); // Para as atualizações quando a atividade for destruída
        }
        mapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    private void loadTrackingDetails() {
        DatabaseReference trackingRef = FirebaseDatabase.getInstance().getReference("users")
                .child(mAuth.getCurrentUser().getUid())
                .child("trackings")
                .child(trackingId);

        trackingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Tracking tracking = dataSnapshot.getValue(Tracking.class);
                if (tracking != null) {
                    trackingNameText.setText(tracking.getTrackingName());
                    homeAddress = tracking.getHomeAddress();
                    schoolAddress = tracking.getSchoolAddress();

                    // Buscar localizações
                    fetchUserLocation(tracking.getStudentUid(), true);
                    fetchUserLocation(tracking.getTransportUid(), false);

                    // Geocodificar endereços
                    if (homeLatLng == null && schoolLatLng == null) {
                        geocodeAddress(homeAddress, true);
                        geocodeAddress(schoolAddress, false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(TrackingActivity.this, "Failed to load tracking details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLocations() {
        DatabaseReference trackingRef = FirebaseDatabase.getInstance().getReference("users")
                .child(mAuth.getCurrentUser().getUid())
                .child("trackings")
                .child(trackingId);

        trackingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Tracking tracking = dataSnapshot.getValue(Tracking.class);
                if (tracking != null) {
                    fetchUserLocation(tracking.getStudentUid(), true);
                    fetchUserLocation(tracking.getTransportUid(), false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(TrackingActivity.this, "Failed to update locations.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserLocation(String shortUserId, final boolean isStudent) {
        usersRef.orderByChild("shortUserId").equalTo(shortUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Double latitude = snapshot.child("location/latitude").getValue(Double.class);
                            Double longitude = snapshot.child("location/longitude").getValue(Double.class);

                            if (latitude != null && longitude != null) {
                                LatLng location = new LatLng(latitude, longitude);
                                if (isStudent) {
                                    studentLatLng = location;
                                } else {
                                    transportLatLng = location;
                                }
                                updateMap(); // Atualiza o mapa com as localizações mais recentes
                                updateLocationStatus(); // Atualiza o status da localização
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(TrackingActivity.this, "Failed to load location.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void geocodeAddress(final String address, final boolean isHomeAddress) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = GEOCODE_URL + "?address=" + address + "&key=" + API_KEY;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject result = response.getJSONArray("results").getJSONObject(0);
                            JSONObject location = result.getJSONObject("geometry").getJSONObject("location");
                            double lat = location.getDouble("lat");
                            double lng = location.getDouble("lng");

                            LatLng latLng = new LatLng(lat, lng);

                            if (isHomeAddress) {
                                homeLatLng = latLng;
                            } else {
                                schoolLatLng = latLng;
                            }
                            updateMap(); // Atualiza o mapa com as localizações mais recentes
                            updateLocationStatus(); // Atualiza o status da localização
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(TrackingActivity.this, "Failed to geocode address.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(TrackingActivity.this, "Failed to geocode address.", Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(jsonObjectRequest);
    }

    private void updateMap() {
        if (googleMap != null) {
            googleMap.clear(); // Limpa os marcadores existentes

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            boolean hasPoints = false;

            // Ícone personalizado para o estudante
            if (studentLatLng != null) {
                googleMap.addMarker(new MarkerOptions()
                        .position(studentLatLng)
                        .title("Localização do Estudante")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.student_icon))); // Ícone personalizado
                builder.include(studentLatLng);
                hasPoints = true;
            }

            // Ícone personalizado para o transporte
            if (transportLatLng != null) {
                googleMap.addMarker(new MarkerOptions()
                        .position(transportLatLng)
                        .title("Localização do Transporte")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.transport_icon))); // Ícone personalizado
                builder.include(transportLatLng);
                hasPoints = true;
            }

            // Ícone padrão para a casa
            if (homeLatLng != null) {
                googleMap.addMarker(new MarkerOptions()
                        .position(homeLatLng)
                        .title("Casa")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))); // Cor personalizada
                builder.include(homeLatLng);
                hasPoints = true;
            }

            // Ícone padrão para a escola
            if (schoolLatLng != null) {
                googleMap.addMarker(new MarkerOptions()
                        .position(schoolLatLng)
                        .title("Escola")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))); // Cor personalizada
                builder.include(schoolLatLng);
                hasPoints = true;
            }

            if (hasPoints) {
                LatLngBounds bounds = builder.build();
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
            }
        }
    }

    private void updateLocationStatus() {
        if (studentLatLng != null) {
            if (homeLatLng != null && isNear(studentLatLng, homeLatLng)) {
                locationStatusText.setText("Aluno está em casa");
            } else if (schoolLatLng != null && isNear(studentLatLng, schoolLatLng)) {
                locationStatusText.setText("Aluno está na escola");
            } else if (transportLatLng != null && isNear(studentLatLng, transportLatLng)) {
                locationStatusText.setText("Aluno está no transporte");
            }
        }
    }

    // Método para calcular proximidade entre dois pontos
    private boolean isNear(LatLng point1, LatLng point2) {
        double threshold = 0.005; // Aproximadamente 500 metros de distância
        return Math.abs(point1.latitude - point2.latitude) < threshold && Math.abs(point1.longitude - point2.longitude) < threshold;
    }
}
