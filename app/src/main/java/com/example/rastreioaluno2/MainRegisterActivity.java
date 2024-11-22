package com.example.rastreioaluno2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class MainRegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextInputEditText registerEmailEditText;
    private TextInputEditText registerPasswordEditText;
    private TextInputEditText registerConfirmPasswordEditText;
    private MaterialButton registerButton;
    private ImageButton backToLoginButton;

    // Views para o círculo de carregamento e overlay de bloqueio
    private View loadingOverlay;
    private View loadingSpinner;

    private static final String PREFS_NAME = "UserPrefs";
    private static final String PREF_EMAIL = "email";
    private static final String PREF_PASSWORD = "password";
    private static final String PREF_REMEMBER_ME = "remember_me";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_register);

        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();

        // Inicializando as views
        registerEmailEditText = findViewById(R.id.register_email);
        registerPasswordEditText = findViewById(R.id.register_password);
        registerConfirmPasswordEditText = findViewById(R.id.register_confirm_password);
        registerButton = findViewById(R.id.register_button);
        backToLoginButton = findViewById(R.id.back_to_login_button);
        loadingOverlay = findViewById(R.id.loading_overlay);
        loadingSpinner = findViewById(R.id.loading_spinner);

        registerButton.setOnClickListener(v -> {
            String email = registerEmailEditText.getText().toString();
            String password = registerPasswordEditText.getText().toString();
            String confirmPassword = registerConfirmPasswordEditText.getText().toString();

            if (validateInput(email, password, confirmPassword)) {
                showLoading(true);
                registerUser(email, password);
            }
        });

        backToLoginButton.setOnClickListener(v -> {
            // Retorna para a tela de login
            Intent intent = new Intent(MainRegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private boolean validateInput(String email, String password, String confirmPassword) {
        if (email.isEmpty()) {
            Toast.makeText(MainRegisterActivity.this, "Por favor, insira seu e-mail.", Toast.LENGTH_LONG).show();
            return false;
        }
        if (password.isEmpty()) {
            Toast.makeText(MainRegisterActivity.this, "Por favor, insira sua senha.", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(MainRegisterActivity.this, "As senhas não coincidem.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        // Registro bem-sucedido
                        FirebaseUser user = mAuth.getCurrentUser();
                        savePreferences(email, password);
                        startActivity(new Intent(MainRegisterActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        handleFirebaseAuthError(task.getException());
                    }
                });
    }

    private void savePreferences(String email, String password) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_EMAIL, email);
        editor.putString(PREF_PASSWORD, password);
        editor.putBoolean(PREF_REMEMBER_ME, true);
        editor.apply();
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
                case "ERROR_EMAIL_ALREADY_IN_USE":
                    message = "Esse e-mail já está em uso.";
                    break;
                case "ERROR_WEAK_PASSWORD":
                    message = "A senha é muito fraca.";
                    break;
                default:
                    message = "Erro: " + authException.getMessage();
                    break;
            }
            Toast.makeText(MainRegisterActivity.this, message, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainRegisterActivity.this, "Erro inesperado: " + exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            // Mostrar o overlay e o spinner imediatamente
            loadingOverlay.setVisibility(View.VISIBLE);
            loadingSpinner.setVisibility(View.VISIBLE);
        } else {
            // Ocultar após 1,5 segundos
            loadingOverlay.postDelayed(() -> {
                loadingOverlay.setVisibility(View.GONE);
                loadingSpinner.setVisibility(View.GONE);
            }, 1500); // Tempo mínimo de 1,5 segundos
        }
    }
}
