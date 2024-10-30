package dev.gustavoesposar.reminder.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.HashMap;
import java.util.Map;

import dev.gustavoesposar.reminder.R;
import dev.gustavoesposar.reminder.network.ApiClient;
import dev.gustavoesposar.reminder.network.ApiService;
import dev.gustavoesposar.reminder.utils.CadastroValidator;
import dev.gustavoesposar.reminder.utils.DateTextWatcher;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NovoAniversarianteFragment extends Fragment {

    private EditText nameInput;
    private EditText birthdateInput;
    private Button submitButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_novoaniversario, container, false);

        initializeViews(view);
        setupBirthdateInput();
        setupSubmitButton();

        return view;
    }

    private void initializeViews(View view) {
        nameInput = view.findViewById(R.id.name);
        birthdateInput = view.findViewById(R.id.birthdate);
        submitButton = view.findViewById(R.id.submit_button);
    }

    private void setupBirthdateInput() {
        birthdateInput.addTextChangedListener(new DateTextWatcher(birthdateInput));
    }

    private void setupSubmitButton() {
        submitButton.setOnClickListener(v -> handleFormSubmission());
    }

    private void handleFormSubmission() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("JWT_TOKEN", null);

        if (token == null) {
            Toast.makeText(getContext(), "Token de autenticação ausente", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = nameInput.getText().toString();
        String birthdate = birthdateInput.getText().toString();
        try {
            CadastroValidator.validarNome(name);
            CadastroValidator.validarData(birthdate);
        } catch (IllegalArgumentException e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        String formattedDate = formatDate(birthdate);

        if (formattedDate != null) {
            Log.d("QRCode", "Formatted Date: " + formattedDate);

            ApiService apiService = ApiClient.getClient().create(ApiService.class);

            Map<String, String> body = new HashMap<>();
            body.put("name", name);
            body.put("date", formattedDate);

            Call<Void> call = apiService.addAniversariante("Bearer " + token, body);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), name.split(" ")[0] + " foi adicionado!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Erro ao adicionar no servidor", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(getContext(), "Falha na conexão com o servidor", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "Data de nascimento inválida", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatDate(String birthdate) {
        try {
            String[] dateParts = birthdate.split("/");
            String day = dateParts[0].length() == 1 ? "0" + dateParts[0] : dateParts[0];
            String month = dateParts[1].length() == 1 ? "0" + dateParts[1] : dateParts[1];
            String year = dateParts[2];
            return year + "-" + month + "-" + day;
        } catch (Exception e) {
            Log.e("NovoAniversariante", "Erro ao formatar a data", e);
            return null;
        }
    }
}
