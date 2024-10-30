package dev.gustavoesposar.reminder.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import dev.gustavoesposar.reminder.R;
import dev.gustavoesposar.reminder.model.RegisterRequest;
import dev.gustavoesposar.reminder.network.ApiClient;
import dev.gustavoesposar.reminder.network.ApiService;
import dev.gustavoesposar.reminder.utils.DateTextWatcher;
import dev.gustavoesposar.reminder.utils.CadastroValidator;
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

        bindViewElements();
        birth.addTextChangedListener(new DateTextWatcher(birth));
        registerButton.setOnClickListener(v -> enviarFormularioDeCadastro());
    }

    private void bindViewElements() {
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        passwordOne = findViewById(R.id.passwordOne);
        passwordTwo = findViewById(R.id.passwordTwo);
        birth = findViewById(R.id.birth);
        registerButton = findViewById(R.id.register_button);
    }

    public void loginActivity(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void enviarFormularioDeCadastro() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        try {
            RegisterRequest request = getBody();
            Call<Void> call = apiService.cadastrarUsuario(request);

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(CadastroActivity.this, LoginActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
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
        String nome = name.getText().toString();
        String email = this.email.getText().toString();
        String senhaUm = passwordOne.getText().toString();
        String senhaDois = passwordTwo.getText().toString();
        String birthdate = birth.getText().toString();

        CadastroValidator.validarNome(nome);
        CadastroValidator.validarEmail(email);
        CadastroValidator.validarSenha(senhaUm, senhaDois);
        CadastroValidator.validarData(birthdate);

        String[] dateParts = birthdate.split("/");
        String formattedDate = String.format("%s-%s-%s", dateParts[2], padLeft(dateParts[1]), padLeft(dateParts[0]));

        return new RegisterRequest(nome, email, senhaUm, senhaDois, formattedDate);
    }

    private String padLeft(String text) {
        return text.length() == 1 ? "0" + text : text;
    }
}
