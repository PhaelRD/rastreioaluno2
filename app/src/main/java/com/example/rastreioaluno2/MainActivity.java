package com.example.rastreioaluno2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextInputEditText loginEmailEditText;
    private TextInputEditText loginPasswordEditText;
    private MaterialButton loginButton;
    private MaterialButton forgotPasswordButton;
    private MaterialButton goToRegisterButton;
    private SwitchCompat rememberMeSwitch;
    private View loadingOverlay;
    private View loadingSpinner;

    private static final String PREFS_NAME = "UserPrefs";
    private static final String PREF_EMAIL = "email";
    private static final String PREF_PASSWORD = "password";
    private static final String PREF_REMEMBER_ME = "remember_me";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Inicializar as views
        loginEmailEditText = findViewById(R.id.login_email);
        loginPasswordEditText = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        forgotPasswordButton = findViewById(R.id.forgot_password_button);
        goToRegisterButton = findViewById(R.id.go_to_register_button);
        rememberMeSwitch = findViewById(R.id.remember_me_switch);
        loadingOverlay = findViewById(R.id.loading_overlay);
        loadingSpinner = findViewById(R.id.loading_spinner);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
        }

        // Carregar preferências salvas
        loadPreferences();

        loginButton.setOnClickListener(v -> {
            showLoading(true);
            loginUser(
                    loginEmailEditText.getText().toString(),
                    loginPasswordEditText.getText().toString()
            );
        });

        forgotPasswordButton.setOnClickListener(v -> {
            String email = loginEmailEditText.getText().toString();
            if (email.isEmpty()) {
                Toast.makeText(MainActivity.this, "Por favor, insira seu e-mail.", Toast.LENGTH_LONG).show();
                return;
            }

            showLoading(true);
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(this, task -> {
                        showLoading(false);
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "E-mail de recuperação de senha enviado.", Toast.LENGTH_LONG).show();
                        } else {
                            handleFirebaseAuthError(task.getException());
                        }
                    });
        });

        goToRegisterButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainRegisterActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void loginUser(String email, String password) {
        if (email.isEmpty()) {
            Toast.makeText(MainActivity.this, "Por favor, insira seu e-mail.", Toast.LENGTH_LONG).show();
            showLoading(false);
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(MainActivity.this, "Por favor, insira sua senha.", Toast.LENGTH_LONG).show();
            showLoading(false);
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        String userId = user.getUid();

                        mDatabase.child("users").child(userId).child("userType")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String userType = dataSnapshot.getValue(String.class);
                                        if ("Pais".equals(userType)) {
                                            if (rememberMeSwitch.isChecked()) {
                                                savePreferences(email, password, true);
                                            } else {
                                                savePreferences("", "", false);
                                            }
                                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                            finish();
                                        } else {
                                            Toast.makeText(MainActivity.this, "Acesso negado.", Toast.LENGTH_LONG).show();
                                            mAuth.signOut();
                                        }
                                        showLoading(false);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Toast.makeText(MainActivity.this, "Erro: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                        showLoading(false);
                                    }
                                });
                    } else {
                        handleFirebaseAuthError(task.getException());
                        showLoading(false);
                    }
                });
    }

    private void savePreferences(String email, String password, boolean rememberMe) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_EMAIL, email);
        editor.putString(PREF_PASSWORD, password);
        editor.putBoolean(PREF_REMEMBER_ME, rememberMe);
        editor.apply();
    }

    private void loadPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean rememberMe = sharedPreferences.getBoolean(PREF_REMEMBER_ME, false);

        if (rememberMe) {
            String email = sharedPreferences.getString(PREF_EMAIL, "");
            String password = sharedPreferences.getString(PREF_PASSWORD, "");
            loginEmailEditText.setText(email);
            loginPasswordEditText.setText(password);
            rememberMeSwitch.setChecked(true);
        }
    }

    private void handleFirebaseAuthError(Exception exception) {
        if (exception instanceof FirebaseAuthException) {
            FirebaseAuthException authException = (FirebaseAuthException) exception;
            String errorCode = authException.getErrorCode();
            String message;

            switch (errorCode) {
                case "ERROR_INVALID_EMAIL":
                    message = "O formato do e-mail está inválido.";
                    break;
                case "ERROR_USER_NOT_FOUND":
                    message = "Usuário não encontrado.";
                    break;
                case "ERROR_WRONG_PASSWORD":
                    message = "Senha incorreta.";
                    break;
                default:
                    message = "Erro: " + authException.getMessage();
                    break;
            }
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainActivity.this, "Erro inesperado: " + exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            loadingOverlay.setVisibility(View.VISIBLE);
            loadingSpinner.setVisibility(View.VISIBLE);
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                loadingOverlay.setVisibility(View.GONE);
                loadingSpinner.setVisibility(View.GONE);
            }, 1500);
        }
    }
}
