package com.example.rastreioaluno2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Campos e Botões
    private EditText loginEmailEditText;
    private EditText loginPasswordEditText;
    private Button loginButton;
    private Button forgotPasswordButton;

    private EditText registerEmailEditText;
    private EditText registerPasswordEditText;
    private EditText registerConfirmPasswordEditText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicialize o Firebase App
        FirebaseApp.initializeApp(this);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference(); // Inicializando o Realtime Database

        // Inicializar os campos e botões para login
        loginEmailEditText = findViewById(R.id.login_email);
        loginPasswordEditText = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        forgotPasswordButton = findViewById(R.id.forgot_password_button);

        // Inicializar os campos e botões para registro
        registerEmailEditText = findViewById(R.id.register_email);
        registerPasswordEditText = findViewById(R.id.register_password);
        registerConfirmPasswordEditText = findViewById(R.id.register_confirm_password);
        registerButton = findViewById(R.id.register_button);

        // Verificar se o usuário já está logado
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Usuário já está logado, redirecionar para HomeActivity
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // Finaliza a MainActivity para que o usuário não possa voltar para ela.
        }

        // Configurar o ouvinte de clique do botão de login
        loginButton.setOnClickListener(v -> loginUser(
                loginEmailEditText.getText().toString(),
                loginPasswordEditText.getText().toString()
        ));

        // Configurar o ouvinte de clique do botão de registro
        registerButton.setOnClickListener(v -> {
            String email = registerEmailEditText.getText().toString();
            String password = registerPasswordEditText.getText().toString();
            String confirmPassword = registerConfirmPasswordEditText.getText().toString();
            registerUser(email, password, confirmPassword);
        });

        // Configurar o ouvinte de clique do botão de recuperação de senha
        forgotPasswordButton.setOnClickListener(v -> {
            String email = loginEmailEditText.getText().toString();
            if (email.isEmpty()) {
                Toast.makeText(MainActivity.this, "Por favor, insira seu e-mail.", Toast.LENGTH_LONG).show();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Exibir um Toast para informar o usuário que o e-mail foi enviado
                            Toast.makeText(MainActivity.this, "E-mail de recuperação de senha enviado. Verifique sua caixa de entrada.", Toast.LENGTH_LONG).show();
                        } else {
                            // Se falhar, exibir o erro
                            handleFirebaseAuthError(task.getException());
                        }
                    });
        });
    }

    private void registerUser(String email, String password, String confirmPassword) {
        // Verificar se as senhas coincidem
        if (!password.equals(confirmPassword)) {
            Toast.makeText(MainActivity.this, "As senhas não coincidem. Tente novamente.", Toast.LENGTH_LONG).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registro bem-sucedido, adicionar o usuário ao Realtime Database
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        String fullUid = firebaseUser.getUid();

                        // Criar objeto do usuário com o userType definido como "Pais"
                        User user = new User(fullUid, email, "Pais");

                        // Salvar o usuário no Realtime Database
                        mDatabase.child("users").child(fullUid).setValue(user)
                                .addOnCompleteListener(saveTask -> {
                                    if (saveTask.isSuccessful()) {
                                        // Redirecionar para HomeActivity
                                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                        finish(); // Finaliza a MainActivity para que o usuário não possa voltar para ela.
                                    } else {
                                        Toast.makeText(MainActivity.this, "Falha ao salvar dados do usuário: " + saveTask.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        // Falha no registro, capturar o erro e exibir mensagem ao usuário
                        handleFirebaseAuthError(task.getException());
                    }
                });
    }

    private void loginUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login bem-sucedido, agora verificar o userType
                        FirebaseUser user = mAuth.getCurrentUser();
                        String userId = user.getUid();

                        // Buscar o userType no Realtime Database
                        mDatabase.child("users").child(userId).child("info").child("userType")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String userType = dataSnapshot.getValue(String.class);
                                        if ("Pais".equals(userType)) {
                                            // Se o userType for "Pais", redirecionar para HomeActivity
                                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                            startActivity(intent);
                                            finish(); // Finaliza a MainActivity para que o usuário não possa voltar para ela.
                                        } else {
                                            // Se o userType não for "Pais", exibir mensagem e deslogar o usuário
                                            Toast.makeText(MainActivity.this, "Acesso negado. Apenas usuários 'Pais' podem fazer login.", Toast.LENGTH_LONG).show();
                                            mAuth.signOut();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        Toast.makeText(MainActivity.this, "Erro ao verificar o tipo de usuário: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        // Falha no login, capturar o erro e exibir mensagem ao usuário
                        handleFirebaseAuthError(task.getException());
                    }
                });
    }

    private void handleFirebaseAuthError(Exception exception) {
        if (exception instanceof FirebaseAuthException) {
            FirebaseAuthException authException = (FirebaseAuthException) exception;
            String errorCode = authException.getErrorCode();

            switch (errorCode) {
                case "ERROR_INVALID_EMAIL":
                    Toast.makeText(MainActivity.this, "O formato do e-mail está inválido.", Toast.LENGTH_LONG).show();
                    break;
                case "ERROR_EMAIL_ALREADY_IN_USE":
                    Toast.makeText(MainActivity.this, "Este e-mail já está em uso.", Toast.LENGTH_LONG).show();
                    break;
                case "ERROR_WEAK_PASSWORD":
                    Toast.makeText(MainActivity.this, "A senha é muito fraca. Por favor, escolha uma senha mais forte.", Toast.LENGTH_LONG).show();
                    break;
                case "ERROR_USER_NOT_FOUND":
                    Toast.makeText(MainActivity.this, "Usuário não encontrado. Verifique suas credenciais.", Toast.LENGTH_LONG).show();
                    break;
                case "ERROR_WRONG_PASSWORD":
                    Toast.makeText(MainActivity.this, "Senha incorreta. Tente novamente.", Toast.LENGTH_LONG).show();
                    break;
                case "ERROR_USER_DISABLED":
                    Toast.makeText(MainActivity.this, "Esta conta foi desativada.", Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(MainActivity.this, "Erro no login/registro: " + authException.getMessage(), Toast.LENGTH_LONG).show();
                    break;
            }
        } else {
            Toast.makeText(MainActivity.this, "Erro no login/registro: " + exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Classe interna para representar um usuário
    public static class User {
        public String uid;
        public String email;
        public String userType;

        public User() {
            // Construtor vazio necessário para o Firebase
        }

        public User(String uid, String email, String userType) {
            this.uid = uid;
            this.email = email;
            this.userType = userType;
        }
    }
}
