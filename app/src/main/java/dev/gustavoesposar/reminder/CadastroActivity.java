package dev.gustavoesposar.reminder;

import static java.security.AccessController.getContext;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CadastroActivity extends AppCompatActivity {
    private EditText name;
    private EditText email;
    private EditText passwordOne;
    private EditText passwordTwo;
    private EditText birth;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cadastro);

        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        passwordOne = findViewById(R.id.passwordOne);
        passwordTwo = findViewById(R.id.passwordTwo);
        birth = findViewById(R.id.birth);
        registerButton = findViewById(R.id.register_button);

        registerButton.setOnClickListener(v -> {
            enviarFormularioDeCadastro();
        });

        birth.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating = false;
            private final String mask = "##/##/####";  // Define a máscara desejada

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) return;

                String input = s.toString().replaceAll("[^\\d]", ""); // Remove qualquer caractere não numérico
                StringBuilder formatted = new StringBuilder();

                int index = 0;
                for (char m : mask.toCharArray()) {
                    if (m == '#' && index < input.length()) {
                        formatted.append(input.charAt(index));
                        index++;
                    } else if (m == '/' && index <= input.length()) {
                        formatted.append('/');
                    }
                }

                isUpdating = true;
                birth.setText(formatted.toString());
                birth.setSelection(formatted.length());  // Move o cursor para o final
                isUpdating = false;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    public void loginActivity(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void enviarFormularioDeCadastro() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        try {
            RegisterRequest request = getBody();
            Log.d("CadastroActivity", "ponto1 ");

            Call<Void> call = apiService.cadastrarUsuario(request);

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(CadastroActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(getApplicationContext(), "Falha na conexão com o servidor", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IllegalArgumentException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private RegisterRequest getBody() {
        String nome = this.name.getText().toString();
        String email = this.email.getText().toString();
        String senhaUm = this.passwordOne.getText().toString();
        String senhaDois = this.passwordTwo.getText().toString();

        // Chamadas de validação que agora lançam exceções
        validarNome(nome);
        validarEmail(email);
        validarSenha(senhaUm, senhaDois);
        validarData(this.birth.getText().toString());

        String[] dateParts = this.birth.getText().toString().split("/");
        String day = dateParts[0].length() == 1 ? "0" + dateParts[0] : dateParts[0];
        String month = dateParts[1].length() == 1 ? "0" + dateParts[1] : dateParts[1];
        String year = dateParts[2];
        String dataNascimento = year + "-" + month + "-" + day;

        return new RegisterRequest(nome, email, senhaUm, senhaDois, dataNascimento);
    }

    private void validarNome(String nome) throws IllegalArgumentException {
        if (nome.isEmpty()) {
            throw new IllegalArgumentException("O nome não pode estar vazio.");
        }
        if (nome.length() < 3) {
            throw new IllegalArgumentException("O nome deve ter pelo menos 3 caracteres.");
        }
    }

    private void validarEmail(String email) throws IllegalArgumentException {
        if (email.isEmpty()) {
            throw new IllegalArgumentException("O email não pode estar vazio.");
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            throw new IllegalArgumentException("Por favor, insira um email válido.");
        }
    }

    private void validarSenha(String senhaUm, String senhaDois) throws IllegalArgumentException {
        if (senhaUm.isEmpty() || senhaDois.isEmpty()) {
            throw new IllegalArgumentException("As senhas não podem estar vazias.");
        }
        if (!senhaUm.equals(senhaDois)) {
            throw new IllegalArgumentException("As senhas não são iguais.");
        }
        if (senhaUm.length() < 6) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 6 caracteres.");
        }
    }

    private void validarData(String data) throws IllegalArgumentException {
        if (data.isEmpty()) {
            throw new IllegalArgumentException("A data de nascimento não pode estar vazia.");
        }
    }

}
