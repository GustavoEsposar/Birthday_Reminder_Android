package dev.gustavoesposar.reminder.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import dev.gustavoesposar.reminder.model.LoginRequest;
import dev.gustavoesposar.reminder.model.LoginResponse;
import dev.gustavoesposar.reminder.R;
import dev.gustavoesposar.reminder.network.ApiClient;
import dev.gustavoesposar.reminder.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private ApiService apiService;
    private EditText emailEditText, passwordEditText;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        setupEdgeToEdgeView();
        initializeApiService();
        bindViewElements();
        setupLoginButtonListener();
    }

    private void setupEdgeToEdgeView() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeApiService() {
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    private void bindViewElements() {
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
    }

    private void setupLoginButtonListener() {
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            login(email, password);
        });
    }

    public void login(String email, String password) {
        LoginRequest loginRequest = new LoginRequest(email, password);
        Call<LoginResponse> call = apiService.login(loginRequest);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleSuccessfulLogin(response.body(), email);
                } else {
                    showToast("Senha ou email incorretos");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                showToast(t.getMessage());
            }
        });
    }

    private void handleSuccessfulLogin(LoginResponse responseBody, String email) {
        saveUserSession(responseBody, email);
        QrcodeFragment.saveQrCodeImageLocally(LoginActivity.this);

        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void saveUserSession(LoginResponse responseBody, String email) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("JWT_TOKEN", responseBody.getToken());
        editor.putString("EMAIL", email);
        editor.putString("QRCODE_URL", QrcodeFragment.getUrl(responseBody.getName(), responseBody.getBirth()));
        editor.apply();
    }

    private void showToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    public void cadastroActivity(View view) {
        Intent intent = new Intent(this, CadastroActivity.class);
        startActivity(intent);
    }
}
