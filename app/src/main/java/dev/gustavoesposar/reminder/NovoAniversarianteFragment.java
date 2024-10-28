package dev.gustavoesposar.reminder;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

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
        birthdateInput.setOnClickListener(v -> openDatePicker());
    }

    private void openDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getActivity(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    birthdateInput.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void setupSubmitButton() {
        submitButton.setOnClickListener(v -> handleFormSubmission());
    }

    private void handleFormSubmission() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = sharedPreferences.getString("JWT_TOKEN", null);

        if (token != null) {
            String name = nameInput.getText().toString();
            String birthdate = birthdateInput.getText().toString();

            String[] dateParts = birthdate.split("/");
            String day = dateParts[0].length() == 1 ? "0" + dateParts[0] : dateParts[0];
            String month = dateParts[1].length() == 1 ? "0" + dateParts[1] : dateParts[1];
            String year = dateParts[2];
            String formattedDate = year + "-" + month + "-" + day;

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
            Toast.makeText(getContext(), "Token de autenticação ausente", Toast.LENGTH_SHORT).show();
        }
    }
}
